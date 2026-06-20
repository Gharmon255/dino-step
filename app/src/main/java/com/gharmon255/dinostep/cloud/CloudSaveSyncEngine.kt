package com.gharmon255.dinostep.cloud

import com.gharmon255.dinostep.data.GameSnapshot
import com.gharmon255.dinostep.data.repository.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CloudSaveSyncEngine(
    private val scope: CoroutineScope,
    private val config: SupabaseConfig,
    private val httpClient: SupabaseHttpClient,
    private val authRepository: CloudAuthRepository,
    private val syncPreferences: CloudSyncPreferences,
    private val gameRepository: GameRepository,
) {
    private val _uiState = MutableStateFlow(buildInitialUiState())
    val uiState: StateFlow<CloudAccountUiState> = _uiState.asStateFlow()

    private var debounceJob: Job? = null

    fun refreshSessionOnLaunch(
        localSnapshot: GameSnapshot,
        onCloudSaveApplied: (GameSnapshot) -> Unit = {},
    ) {
        if (!config.isConfigured) {
            return
        }
        scope.launch {
            val session = resolveSession() ?: run {
                updateSignedInState(null)
                return@launch
            }
            val cloudRow = try {
                httpClient.fetchGameSave(session)
            } catch (_: Exception) {
                updateSignedInState(session)
                return@launch
            }
            when {
                cloudRow == null -> updateSignedInState(session)
                CloudSaveMapper.isLocalEmpty(localSnapshot) -> {
                    applyCloudSnapshot(cloudRow.save)
                    CloudSaveMapper.toSnapshot(cloudRow.save)?.let(onCloudSaveApplied)
                    updateSignedInState(session)
                }
                else -> updateSignedInState(session)
            }
        }
    }

    suspend fun handleSignInWithGoogleIdToken(idToken: String, localSnapshot: GameSnapshot) {
        if (!config.isConfigured) {
            return
        }
        _uiState.value = _uiState.value.copy(syncStatus = CloudSyncStatus.Syncing, lastError = null)
        try {
            val session = authRepository.signInWithGoogleIdToken(idToken)
            reconcileAfterSignIn(session, localSnapshot)
        } catch (error: Exception) {
            _uiState.value = _uiState.value.copy(
                syncStatus = CloudSyncStatus.Error,
                lastError = error.message ?: "Sign-in failed",
            )
        }
    }

    fun refreshSignedInState() {
        if (!config.isConfigured) {
            return
        }
        scope.launch {
            updateSignedInState(resolveSession())
        }
    }

    private suspend fun resolveSession(): CloudSession? {
        return authRepository.restoreSession() ?: authRepository.trySilentGoogleSignIn()
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.value = buildInitialUiState().copy(syncStatus = CloudSyncStatus.SignedOut)
    }

    fun schedulePush(localSnapshot: GameSnapshot) {
        if (!config.isConfigured) {
            return
        }
        val session = authRepository.currentSession() ?: return
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(DEBOUNCE_MS)
            pushSnapshot(session, localSnapshot)
        }
    }

    fun exportLocalJson(localSnapshot: GameSnapshot): String {
        val revision = syncPreferences.nextRevision()
        val cloud = CloudSaveMapper.toCloud(
            snapshot = localSnapshot,
            revision = revision,
            updatedAt = CloudSaveJson.nowIso(),
        )
        return CloudSaveJson.encode(cloud).toString(2)
    }

    fun resolveConflictKeepLocal(localSnapshot: GameSnapshot) {
        val conflict = _uiState.value.pendingConflict ?: return
        if (conflict !is CloudSaveConflict.LocalVsCloud) {
            return
        }
        _uiState.value = _uiState.value.copy(pendingConflict = null)
        val session = authRepository.currentSession() ?: return
        scope.launch {
            pushSnapshot(session, localSnapshot)
        }
    }

    suspend fun resolveConflictUseCloud(): GameSnapshot? {
        val conflict = _uiState.value.pendingConflict as? CloudSaveConflict.LocalVsCloud ?: return null
        _uiState.value = _uiState.value.copy(pendingConflict = null)
        val snapshot = CloudSaveMapper.toSnapshot(conflict.cloud) ?: return null
        gameRepository.replaceGameSnapshot(snapshot)
        syncPreferences.localRevision = conflict.cloud.revision
        syncPreferences.lastBackedUpAtMillis = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(
            syncStatus = CloudSyncStatus.BackedUp,
            lastBackedUpAtMillis = syncPreferences.lastBackedUpAtMillis,
        )
        return snapshot
    }

    fun dismissConflict() {
        _uiState.value = _uiState.value.copy(pendingConflict = null)
    }

    private suspend fun reconcileAfterSignIn(session: CloudSession, localSnapshot: GameSnapshot) {
        val cloudRow = httpClient.fetchGameSave(session)
        when {
            cloudRow == null -> {
                pushSnapshot(session, localSnapshot)
                updateSignedInState(session)
            }
            CloudSaveMapper.isLocalEmpty(localSnapshot) -> {
                applyCloudSnapshot(cloudRow.save)
                updateSignedInState(session)
            }
            cloudRow.save.revision == syncPreferences.localRevision -> {
                updateSignedInState(session)
            }
            else -> {
                val localCloud = CloudSaveMapper.toCloud(
                    snapshot = localSnapshot,
                    revision = syncPreferences.localRevision,
                    updatedAt = CloudSaveJson.nowIso(),
                )
                _uiState.value = _uiState.value.copy(
                    syncStatus = CloudSyncStatus.BackedUp,
                    signedInUserId = session.userId,
                    signedInEmail = session.email,
                    signedInProvider = session.provider,
                    pendingConflict = CloudSaveConflict.LocalVsCloud(
                        local = localCloud,
                        cloud = cloudRow.save,
                    ),
                )
            }
        }
    }

    private suspend fun pushSnapshot(
        session: CloudSession,
        localSnapshot: GameSnapshot,
    ) {
        _uiState.value = _uiState.value.copy(syncStatus = CloudSyncStatus.Syncing, lastError = null)
        try {
            val revision = syncPreferences.nextRevision()
            val cloud = CloudSaveMapper.toCloud(
                snapshot = localSnapshot,
                revision = revision,
                updatedAt = CloudSaveJson.nowIso(),
            )
            httpClient.upsertGameSave(
                session = session,
                row = CloudSaveRow(
                    userId = session.userId,
                    schemaVersion = cloud.schemaVersion,
                    revision = cloud.revision,
                    save = cloud,
                    updatedAt = cloud.updatedAt,
                ),
            )
            syncPreferences.lastBackedUpAtMillis = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(
                syncStatus = CloudSyncStatus.BackedUp,
                lastBackedUpAtMillis = syncPreferences.lastBackedUpAtMillis,
                lastError = null,
            )
        } catch (error: Exception) {
            _uiState.value = _uiState.value.copy(
                syncStatus = CloudSyncStatus.Error,
                lastError = error.message ?: "Backup failed",
            )
        }
    }

    private suspend fun applyCloudSnapshot(cloud: CloudGameSave) {
        val snapshot = CloudSaveMapper.toSnapshot(cloud) ?: return
        gameRepository.replaceGameSnapshot(snapshot)
        syncPreferences.localRevision = cloud.revision
        syncPreferences.lastBackedUpAtMillis = System.currentTimeMillis()
    }

    private fun updateSignedInState(session: CloudSession?) {
        if (session == null) {
            _uiState.value = buildInitialUiState().copy(syncStatus = CloudSyncStatus.SignedOut)
            return
        }
        _uiState.value = _uiState.value.copy(
            syncStatus = CloudSyncStatus.BackedUp,
            signedInUserId = session.userId,
            signedInEmail = session.email,
            signedInProvider = session.provider,
            lastBackedUpAtMillis = syncPreferences.lastBackedUpAtMillis,
            lastError = null,
        )
    }

    private fun buildInitialUiState(): CloudAccountUiState {
        return CloudAccountUiState(
            isConfigured = config.isConfigured,
            syncStatus = if (config.isConfigured) CloudSyncStatus.SignedOut else CloudSyncStatus.Unavailable,
            lastBackedUpAtMillis = syncPreferences.lastBackedUpAtMillis,
        )
    }

    companion object {
        private const val DEBOUNCE_MS = 3_000L
    }
}
