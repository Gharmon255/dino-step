package com.gharmon255.dinostep.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gharmon255.dinostep.cloud.CloudAccountUiState
import com.gharmon255.dinostep.cloud.CloudSaveSyncEngine
import com.gharmon255.dinostep.data.GameSnapshot
import com.gharmon255.dinostep.data.AppExperiencePreferences
import com.gharmon255.dinostep.data.DeveloperPreferences
import com.gharmon255.dinostep.data.repository.GameRepository
import com.gharmon255.dinostep.health.DailyActivityPenalty
import com.gharmon255.dinostep.health.DayRolloverEvaluator
import com.gharmon255.dinostep.health.StepTimeUtils
import com.gharmon255.dinostep.health.HealthConnectRepository
import com.gharmon255.dinostep.health.HealthConnectUiStatus
import com.gharmon255.dinostep.health.HealthStepSyncEngine
import com.gharmon255.dinostep.health.StepProgression
import com.gharmon255.dinostep.model.CreatureNickname
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.CreatureFacts
import com.gharmon255.dinostep.model.CreatureVisualMapper
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.EggRewardRoller
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.model.PlayerStats
import com.gharmon255.dinostep.model.Rarity
import com.gharmon255.dinostep.notifications.StageMilestoneNotifier
import com.gharmon255.dinostep.ui.collection.CollectionRoster
import com.gharmon255.dinostep.shared.wear.WearSyncEventType
import com.gharmon255.dinostep.garmin.GarminCompanionPublisher
import com.gharmon255.dinostep.wear.WearCreaturePayloadMapper
import com.gharmon255.dinostep.wear.WearDataLayerPublisher
import com.gharmon255.dinostep.wear.WearSyncDebugState
import com.gharmon255.dinostep.wear.WearSyncPublishResult
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: GameRepository,
    private val developerPreferences: DeveloperPreferences,
    private val appExperiencePreferences: AppExperiencePreferences,
    private val healthConnectRepository: HealthConnectRepository,
    private val healthStepSyncEngine: HealthStepSyncEngine,
    private val wearDataLayerPublisher: WearDataLayerPublisher,
    private val garminCompanionPublisher: GarminCompanionPublisher,
    private val stageMilestoneNotifier: StageMilestoneNotifier,
    private val cloudSaveSyncEngine: CloudSaveSyncEngine,
) : ViewModel() {
    private val testingEggFactory = TestingEggFactory(repository)
    private var lastAutomaticHealthSyncAttemptMillis: Long? = null
    var isReady by mutableStateOf(false)
        private set

    var activeCreature by mutableStateOf(repository.createMysteryCommonEgg())
        private set

    var collection by mutableStateOf<List<CompletedCreature>>(emptyList())
        private set

    var playerStats by mutableStateOf(PlayerStats())
        private set

    var healthConnectStatus by mutableStateOf<HealthConnectUiStatus>(HealthConnectUiStatus.Unavailable)
        private set

    var syncStatusMessage by mutableStateOf<String?>(null)
        private set

    var isSyncing by mutableStateOf(false)
        private set

    var wearSyncDebug by mutableStateOf(WearSyncDebugState())
        private set

    var eggRewardDebug by mutableStateOf(EggRewardDebugState())
        private set

    var nextEggTestSpecies by mutableStateOf(NextEggTestSpecies.RANDOM)
        private set

    var lastSyncTimeMillis by mutableStateOf<Long?>(null)
        private set

    var pendingDiscovery by mutableStateOf<DiscoveryCelebration?>(null)
        private set

    var showOnboarding by mutableStateOf(false)
        private set

    var showWhatsNew by mutableStateOf(false)
        private set

    var inactivityPenaltyAlert by mutableStateOf<String?>(null)
        private set

    val cloudAccountUiState: StateFlow<CloudAccountUiState> = cloudSaveSyncEngine.uiState

    fun clearPendingDiscovery() {
        pendingDiscovery = null
    }

    val todaySteps: Int
        get() = lastSyncedStepTotal

    val lifetimeSteps: Int
        get() = playerStats.lifetimeStepsApplied

    val dexDiscovered: Int
        get() = CollectionRoster.buildSummary(collection).uniqueSpeciesCollected

    val dexTotal: Int
        get() = CreatureCatalog.all.size

    val readStepsPermissions: Set<String>
        get() = healthConnectRepository.readStepsPermissions

    val totalFakeStepsAdded: Int
        get() = playerStats.totalFakeStepsAdded

    val eggsHatched: Int
        get() = playerStats.eggsHatched

    val completedCount: Int
        get() = playerStats.creaturesCompleted

    val lastSyncedStepTotal: Int
        get() = playerStats.lastSyncedStepTotal

    val steps: Int
        get() = activeCreature.steps

    val stage: GrowthStage
        get() = activeCreature.stage

    val displayName: String
        get() = activeCreature.displayName

    val eggRarity: EggRarity
        get() = activeCreature.eggRarity

    val hatchedCreatureRarity: Rarity?
        get() = activeCreature.creature.rarity.takeIf { isRevealed }

    val creatureEmoji: String
        get() = CreatureVisualMapper.visualForActiveCreature(activeCreature).placeholderEmoji

    val isRevealed: Boolean
        get() = activeCreature.isRevealed

    val nextMilestone: Int?
        get() = activeCreature.nextMilestone

    val progressPercent: Float
        get() = activeCreature.progressPercent

    val isAdult: Boolean
        get() = activeCreature.isAdult

    val activeCreatureState: ActiveCreatureState
        get() = activeCreature

    val duplicateTradeOffer: DuplicateTradeOffer?
        get() = if (!isReady) {
            null
        } else {
            DuplicateTradeLogic.offer(activeCreature, collection)
        }

    init {
        viewModelScope.launch {
            val snapshot = repository.loadOrCreateGame()
            activeCreature = snapshot.activeCreature.normalized()
            collection = snapshot.collection
            playerStats = snapshot.playerStats
            nextEggTestSpecies = developerPreferences.getNextEggTestSpecies()
            refreshHealthConnectStatus()
            refreshExperiencePresentation()
            cloudSaveSyncEngine.refreshSessionOnLaunch(snapshot) { applied ->
                applyGameSnapshot(applied)
            }
            isReady = true
            publishActiveCreatureToWatch(WearSyncEventType.NONE)
            syncHealthSteps(manual = false)
        }
    }

    fun onAppForeground() {
        if (!isReady) {
            return
        }
        syncHealthSteps(manual = false)
        republishWearStateOnForeground()
    }

    fun forceWatchSync() {
        if (!DevTools.isEnabled || !isReady) {
            return
        }
        publishActiveCreatureToWatch(WearSyncEventType.NONE)
    }

    /** Re-publishes current creature to Wear when the phone app returns to foreground. */
    fun republishWearStateOnForeground() {
        if (!isReady) {
            return
        }
        publishActiveCreatureToWatch(WearSyncEventType.NONE)
    }

    fun clearCollectionForTesting() {
        if (!DevTools.isEnabled || !isReady) {
            return
        }

        viewModelScope.launch {
            repository.clearCollection()
            collection = emptyList()
            playerStats = playerStats.copy(creaturesCompleted = 0)
        }
    }

    fun resetGameForTesting() {
        if (!DevTools.isEnabled || !isReady) {
            return
        }

        viewModelScope.launch {
            val snapshot = repository.resetGameForTesting()
            activeCreature = snapshot.activeCreature
            collection = snapshot.collection
            playerStats = snapshot.playerStats
            eggRewardDebug = EggRewardDebugState()
            publishActiveCreatureToWatch(WearSyncEventType.NONE)
        }
    }

    fun needsReplaceConfirmationForNewEgg(): Boolean {
        return steps > 0 || isRevealed
    }

    fun updateNextEggTestSpecies(selection: NextEggTestSpecies) {
        nextEggTestSpecies = selection
        developerPreferences.setNextEggTestSpecies(selection)
    }

    fun getCurrentTestSpeciesOverride(): String? = nextEggTestSpecies.testSpeciesOverrideId()

    fun giveRandomEggForTesting() {
        if (!DevTools.isEnabled || !isReady) {
            return
        }
        val grant = testingEggFactory.grantWeightedRandomEgg()
        applyTestingEgg(egg = grant.egg, roll = grant.roll)
    }

    fun giveEggForTesting(eggRarity: EggRarity) {
        if (!DevTools.isEnabled || !isReady) {
            return
        }
        applyTestingEgg(
            egg = testingEggFactory.grantRandomEggForRarity(eggRarity),
            eggRarity = eggRarity,
        )
    }

    /** Only developer action that may apply [getCurrentTestSpeciesOverride]. */
    fun forceTestEggForTesting() {
        if (!DevTools.isEnabled || !isReady) {
            return
        }
        when (val grant = testingEggFactory.grantForceButtonEgg(getCurrentTestSpeciesOverride())) {
            is TestingEggFactory.ForceButtonGrant.ForcedSpecies ->
                applyTestingEgg(egg = grant.egg)
            is TestingEggFactory.ForceButtonGrant.WeightedRandom ->
                applyTestingEgg(egg = grant.grant.egg, roll = grant.grant.roll)
        }
    }

    private fun applyTestingEgg(
        egg: ActiveCreatureState,
        roll: EggRewardRoller.RollResult? = null,
        eggRarity: EggRarity? = null,
    ) {
        activeCreature = egg
        eggRewardDebug = when {
            roll != null -> EggRewardDebugState(
                lastRewardedEggRarity = roll.eggRarity,
                lastRewardRollValue = roll.rollValue,
            )
            eggRarity != null -> eggRewardDebug.copy(lastRewardedEggRarity = eggRarity)
            else -> eggRewardDebug.copy(lastRewardedEggRarity = egg.eggRarity)
        }
        viewModelScope.launch {
            repository.saveActiveCreature(activeCreature)
            publishActiveCreatureToWatch(WearSyncEventType.NONE)
        }
    }

    fun refreshHealthConnectStatus() {
        viewModelScope.launch {
            healthConnectStatus = runCatching {
                healthConnectRepository.resolveUiStatus()
            }.getOrElse { error ->
                HealthConnectUiStatus.Error(
                    error.localizedMessage ?: "Unable to check Health Connect status",
                )
            }
        }
    }

    fun onHealthPermissionsResult(granted: Set<String>) {
        if (granted.containsAll(readStepsPermissions)) {
            healthConnectStatus = HealthConnectUiStatus.Ready
            syncHealthSteps(manual = false)
        } else {
            healthConnectStatus = HealthConnectUiStatus.PermissionRequired
        }
    }

    fun addSteps(amount: Int) {
        if (!DevTools.isEnabled || !isReady) {
            return
        }
        applyStepsToCreature(amount, countAsFake = true)
    }

    fun syncHealthSteps(manual: Boolean = false) {
        if (!isReady || isSyncing) {
            return
        }

        val now = System.currentTimeMillis()
        if (!manual) {
            val lastAttempt = lastAutomaticHealthSyncAttemptMillis
            if (lastAttempt != null && now - lastAttempt < AUTOMATIC_SYNC_DEBOUNCE_MILLIS) {
                return
            }
            lastAutomaticHealthSyncAttemptMillis = now
        }

        viewModelScope.launch {
            isSyncing = true
            syncStatusMessage = null

            val previousCreature = activeCreature
            try {
                val outcome = healthStepSyncEngine.sync()
                healthConnectStatus = outcome.status
                syncStatusMessage = outcome.message

                outcome.activeCreature?.let { activeCreature = it.normalized() }
                outcome.playerStats?.let { playerStats = it }

                if (outcome.status is HealthConnectUiStatus.Ready) {
                    lastSyncTimeMillis = System.currentTimeMillis()
                }
                if (outcome.appliedDelta > 0) {
                    stageMilestoneNotifier.notifyIfNeeded(previousCreature, activeCreature)
                }
                if (outcome.inactivityPenaltyApplied) {
                    inactivityPenaltyAlert = inactivityPenaltyMessage()
                }
                maybeCelebrateDiscovery(previousCreature, activeCreature)
            } finally {
                isSyncing = false
            }
        }
    }

    fun claimReward() {
        claimRandomReward()
    }

    fun claimRandomReward() {
        if (!isReady || !activeCreature.isAdult) {
            return
        }

        val completedCreatureState = activeCreature
        val completedSpeciesId = completedCreatureState.creature.id
        val collectedSpeciesIds = collection.map { it.creature.id }.toSet()
        val completed = CompletedCreature(
            creature = completedCreatureState.creature,
            stepsCompleted = completedCreatureState.steps,
            completedAt = System.currentTimeMillis(),
            nickname = completedCreatureState.nickname,
        )

        collection = collection + completed
        playerStats = playerStats.copy(
            creaturesCompleted = playerStats.creaturesCompleted + 1,
        )
        val rewardRoll = EggRewardRoller.rollWeighted()
        activeCreature = repository.createRandomEggWithRarity(
            eggRarity = rewardRoll.eggRarity,
            excludeSpeciesIds = setOf(completedSpeciesId),
            collectedSpeciesIds = collectedSpeciesIds,
        )
        eggRewardDebug = EggRewardDebugState(
            lastRewardedEggRarity = rewardRoll.eggRarity,
            lastRewardRollValue = rewardRoll.rollValue,
        )

        viewModelScope.launch {
            val completedResult = wearDataLayerPublisher.publishActiveCreature(
                activeCreature = completedCreatureState,
                eventType = WearSyncEventType.COMPLETED,
            )
            updateWearSyncDebug(completedResult)
            repository.saveCompletedCreature(completed)
            repository.savePlayerStats(playerStats)
            repository.saveActiveCreature(activeCreature)
            cloudSaveSyncEngine.schedulePush(currentSnapshot())
            publishActiveCreatureToWatch(WearSyncEventType.NONE)
        }
    }

    fun tradeDuplicatesForTierUpEgg() {
        if (!isReady || !activeCreature.isAdult) {
            return
        }

        val offer = duplicateTradeOffer ?: return
        val removal = DuplicateTradeLogic.removeOneCompleted(offer.speciesId, collection) ?: return
        val removed = removal.second

        collection = removal.first
        activeCreature = repository.createRandomEggWithRarity(
            eggRarity = offer.rewardEggRarity,
            excludeSpeciesIds = setOf(offer.speciesId),
            collectedSpeciesIds = collection.map { it.creature.id }.toSet(),
        )
        eggRewardDebug = EggRewardDebugState(
            lastRewardedEggRarity = offer.rewardEggRarity,
            lastRewardRollValue = null,
        )

        viewModelScope.launch {
            repository.deleteCompletedCreature(removed.id)
            repository.saveActiveCreature(activeCreature)
            publishActiveCreatureToWatch(WearSyncEventType.NONE)
        }
    }

    fun setActiveCreatureNickname(rawNickname: String?) {
        if (!isReady || !activeCreature.isRevealed) {
            return
        }

        val nickname = CreatureNickname.normalize(rawNickname)
        if (nickname == activeCreature.nickname) {
            return
        }

        activeCreature = activeCreature.copy(nickname = nickname)
        viewModelScope.launch {
            repository.saveActiveCreature(activeCreature)
            publishActiveCreatureToWatch(WearSyncEventType.NONE)
        }
    }

    fun updateCompletedCreatureNickname(creatureId: Long, rawNickname: String?) {
        if (!isReady || creatureId <= 0L) {
            return
        }

        val nickname = CreatureNickname.normalize(rawNickname)
        val index = collection.indexOfFirst { it.id == creatureId }
        if (index < 0) {
            return
        }

        val existing = collection[index]
        if (existing.nickname == nickname) {
            return
        }

        val updated = existing.copy(nickname = nickname)
        collection = collection.toMutableList().also { it[index] = updated }
        viewModelScope.launch {
            repository.updateCompletedCreatureNickname(creatureId, nickname)
        }
    }

    fun completedCreaturesForSpecies(speciesId: String): List<CompletedCreature> {
        return collection
            .filter { it.creature.id == speciesId }
            .sortedByDescending { it.completedAt }
    }

    private fun applyStepsToCreature(amount: Int, countAsFake: Boolean) {
        if (amount <= 0) {
            return
        }

        val previous = activeCreature
        val progression = StepProgression.applySteps(
            activeCreature = activeCreature,
            playerStats = playerStats,
            amount = amount,
            countAsFake = countAsFake,
        )
        activeCreature = progression.activeCreature
        playerStats = progression.playerStats

        val eventType = WearCreaturePayloadMapper.detectEventType(
            previous = previous,
            current = activeCreature,
        )
        stageMilestoneNotifier.notifyIfNeeded(previous, activeCreature)
        maybeCelebrateDiscovery(previous, activeCreature)
        persistActiveAndStats()
        publishActiveCreatureToWatch(eventType)
    }

    private fun maybeCelebrateDiscovery(
        previous: ActiveCreatureState,
        current: ActiveCreatureState,
    ) {
        if (!previous.isRevealed && current.isRevealed) {
            pendingDiscovery = DiscoveryCelebration(
                speciesId = current.creature.id,
                speciesName = current.creature.name,
                funFact = CreatureFacts.forSpecies(current.creature.id),
            )
        }
    }

    private fun publishActiveCreatureToWatch(eventType: WearSyncEventType) {
        if (!isReady) {
            return
        }

        val creature = activeCreature
        viewModelScope.launch {
            val result = wearDataLayerPublisher.publishActiveCreature(creature, eventType)
            updateWearSyncDebug(result)
            garminCompanionPublisher.publishActiveCreature(creature, eventType)
        }
    }

    private fun updateWearSyncDebug(result: WearSyncPublishResult) {
        wearSyncDebug = WearSyncDebugState(
            connectedNodeCount = result.connectedNodeCount,
            lastAttemptTimeMillis = System.currentTimeMillis(),
            lastStatusMessage = result.statusMessage,
            lastPayloadCreatureId = result.payloadCreatureId,
            lastPayloadDisplayName = result.payloadDisplayName,
            lastPayloadStage = result.payloadStage,
            lastPayloadProgressPercent = result.payloadProgressPercent,
            lastPayloadEggRarity = result.payloadEggRarity,
            lastPayloadCreatureRarity = result.payloadCreatureRarity,
            lastPayloadIsAssetBacked = result.payloadIsAssetBacked,
            lastPayloadStageDrawableKey = result.payloadStageDrawableKey,
            lastPayloadSteps = result.payloadSteps,
            lastPayloadStepsUntilNext = result.payloadStepsUntilNext,
            lastPayloadNextStageLabel = result.payloadNextStageLabel,
            lastPayloadSummary = result.payloadSummary,
        )
    }

    private fun persistActiveAndStats() {
        val creature = activeCreature
        val stats = playerStats
        viewModelScope.launch {
            repository.saveActiveCreature(creature)
            repository.savePlayerStats(stats)
            if (isReady) {
                cloudSaveSyncEngine.schedulePush(currentSnapshot())
            }
        }
    }

    fun completeOnboarding() {
        appExperiencePreferences.setOnboardingCompleted()
        appExperiencePreferences.setLastSeenWhatsNewVersion(AppExperiencePreferences.CURRENT_WHATS_NEW_VERSION)
        showOnboarding = false
        refreshExperiencePresentation()
    }

    fun dismissWhatsNew() {
        appExperiencePreferences.setLastSeenWhatsNewVersion(AppExperiencePreferences.CURRENT_WHATS_NEW_VERSION)
        showWhatsNew = false
    }

    fun dismissInactivityPenaltyAlert() {
        inactivityPenaltyAlert = null
    }

    fun signInWithGoogleIdToken(idToken: String) {
        if (!isReady) {
            return
        }
        viewModelScope.launch {
            cloudSaveSyncEngine.handleSignInWithGoogleIdToken(idToken, currentSnapshot())
        }
    }

    fun signOutCloudAccount() {
        cloudSaveSyncEngine.signOut()
    }

    fun keepLocalCloudSave() {
        if (!isReady) {
            return
        }
        cloudSaveSyncEngine.resolveConflictKeepLocal(currentSnapshot())
    }

    fun useCloudSave() {
        if (!isReady) {
            return
        }
        viewModelScope.launch {
            val applied = cloudSaveSyncEngine.resolveConflictUseCloud()
            if (applied != null) {
                applyGameSnapshot(applied)
            }
        }
    }

    fun dismissCloudSaveConflict() {
        cloudSaveSyncEngine.dismissConflict()
    }

    fun exportLocalSaveJson(): String {
        return cloudSaveSyncEngine.exportLocalJson(currentSnapshot())
    }

    private fun currentSnapshot(): GameSnapshot {
        return GameSnapshot(
            activeCreature = activeCreature,
            collection = collection,
            playerStats = playerStats,
        )
    }

    private fun applyGameSnapshot(snapshot: GameSnapshot) {
        activeCreature = snapshot.activeCreature.normalized()
        collection = snapshot.collection
        playerStats = snapshot.playerStats
        publishActiveCreatureToWatch(WearSyncEventType.NONE)
    }

    /** DEBUG: Pretend yesterday had [yesterdaySteps] and run the real day-rollover penalty check. */
    fun simulateInactiveDayForTesting(yesterdaySteps: Int = 0) {
        if (!DevTools.isEnabled || !isReady) {
            return
        }

        viewModelScope.launch {
            val yesterdayStart = StepTimeUtils.startOfYesterdayMillis()
            appExperiencePreferences.setLastActivityEvaluationDayStartMillis(yesterdayStart)
            playerStats = playerStats.copy(
                lastSyncDayStartMillis = yesterdayStart,
                lastSyncedStepTotal = yesterdaySteps.coerceAtLeast(0),
            )
            repository.savePlayerStats(playerStats)

            val rollover = DayRolloverEvaluator.evaluateIfNeeded(
                experience = appExperiencePreferences,
                activeCreature = activeCreature,
                playerStats = playerStats,
                fetchYesterdaySteps = { yesterdaySteps.coerceAtLeast(0) },
            )
            activeCreature = rollover.activeCreature.normalized()

            if (rollover.penalty != null) {
                repository.saveActiveCreature(activeCreature)
                inactivityPenaltyAlert = inactivityPenaltyMessage()
                publishActiveCreatureToWatch(WearSyncEventType.NONE)
                syncStatusMessage =
                    "DEBUG: Inactivity penalty applied (yesterday=$yesterdaySteps steps)."
            } else {
                syncStatusMessage =
                    "DEBUG: No penalty for yesterday=$yesterdaySteps steps " +
                        "(need < ${DailyActivityPenalty.MINIMUM_DAILY_STEPS}, or already at minimum egg)."
            }
        }
    }

    private fun refreshExperiencePresentation() {
        showOnboarding = !appExperiencePreferences.hasCompletedOnboarding()
        showWhatsNew = !showOnboarding &&
            appExperiencePreferences.lastSeenWhatsNewVersion() < AppExperiencePreferences.CURRENT_WHATS_NEW_VERSION
    }

    private fun inactivityPenaltyMessage(): String =
        "You walked fewer than ${DailyActivityPenalty.MINIMUM_DAILY_STEPS} steps yesterday. " +
            "Your dino is back in an egg with ${DailyActivityPenalty.PENALTY_REMAINING_STEPS} steps of progress. " +
            "Keep walking every day!"

    companion object {
        private const val AUTOMATIC_SYNC_DEBOUNCE_MILLIS = 120_000L
    }
}
