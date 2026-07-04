package com.gharmon255.dinostep.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gharmon255.dinostep.battle.BattleOutcomeText
import com.gharmon255.dinostep.battle.BattleRecord
import com.gharmon255.dinostep.battle.BattleFeatures
import com.gharmon255.dinostep.battle.BattleRepository
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
import com.gharmon255.dinostep.promo.PromoCatalog
import com.gharmon255.dinostep.promo.PromoRedemptionCodec
import com.gharmon255.dinostep.promo.PromoRepository
import com.gharmon255.dinostep.ui.collection.CollectionRoster
import com.gharmon255.dinostep.shared.wear.WearSyncEventType
import com.gharmon255.dinostep.garmin.GarminCompanionPublisher
import com.gharmon255.dinostep.wear.WearCreaturePayloadMapper
import com.gharmon255.dinostep.wear.WearDataLayerPublisher
import com.gharmon255.dinostep.wear.WearSyncDebugState
import com.gharmon255.dinostep.wear.WearSyncPublishResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val battleRepository: BattleRepository? = null,
    private val promoRepository: PromoRepository? = null,
) : ViewModel() {
    private val testingEggFactory = TestingEggFactory(repository)
    private var battlePollJob: Job? = null
    private var lastAutomaticHealthSyncAttemptMillis: Long? = null
    private var lastHomeVisibleHealthSyncAttemptMillis: Long? = null
    private var pendingManualHealthSync = false
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

    var selectedBattleFighter by mutableStateOf<CompletedCreature?>(null)
        private set

    var latestBattle by mutableStateOf<BattleRecord?>(null)
        private set

    var battleHistory by mutableStateOf<List<BattleRecord>>(emptyList())
        private set

    var battleInviteCode by mutableStateOf<String?>(null)
        private set

    var activeBattleChallengeId by mutableStateOf<String?>(null)
        private set

    var isBattleLoading by mutableStateOf(false)
        private set

    var battleStatusMessage by mutableStateOf<String?>(null)
        private set

    var promoStatusMessage by mutableStateOf<String?>(null)
        private set

    var isPromoLoading by mutableStateOf(false)
        private set

    val pendingPromoRewardRarity: EggRarity?
        get() = playerStats.pendingRewardEggRarity?.let { EggRarity.fromRaw(it) }

    val redeemedPromoCodes: Set<String>
        get() = PromoRedemptionCodec.parse(playerStats.redeemedPromoCodes)

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
        cloudSaveSyncEngine.refreshSignedInState()
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

    fun onHomeScreenVisible() {
        if (!isReady || isSyncing) {
            return
        }
        val now = System.currentTimeMillis()
        val lastAttempt = lastHomeVisibleHealthSyncAttemptMillis
        if (lastAttempt != null && now - lastAttempt < HOME_VISIBLE_SYNC_DEBOUNCE_MILLIS) {
            return
        }
        lastHomeVisibleHealthSyncAttemptMillis = now
        syncHealthSteps(manual = false)
    }

    fun syncHealthSteps(manual: Boolean = false) {
        if (!isReady) {
            return
        }
        if (isSyncing) {
            if (manual) {
                pendingManualHealthSync = true
            }
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
            if (manual) {
                syncStatusMessage = null
            }

            val previousCreature = activeCreature
            try {
                var outcome = healthStepSyncEngine.sync()
                if (manual && outcome.appliedDelta == 0 && outcome.status is HealthConnectUiStatus.Ready) {
                    delay(MANUAL_SYNC_HEALTH_CONNECT_RETRY_DELAY_MILLIS)
                    outcome = healthStepSyncEngine.sync()
                }
                applyHealthSyncOutcome(outcome, previousCreature, manual)
            } finally {
                isSyncing = false
                if (pendingManualHealthSync) {
                    pendingManualHealthSync = false
                    syncHealthSteps(manual = true)
                }
            }
        }
    }

    private fun applyHealthSyncOutcome(
        outcome: com.gharmon255.dinostep.health.HealthStepSyncOutcome,
        previousCreature: ActiveCreatureState,
        manual: Boolean,
    ) {
        healthConnectStatus = outcome.status
        syncStatusMessage = outcome.message

        outcome.activeCreature?.let { activeCreature = it.normalized() }
        outcome.playerStats?.let { playerStats = it }
        outcome.collection?.let { collection = it }

        if (outcome.appliedDelta > 0 && outcome.status is HealthConnectUiStatus.Ready) {
            lastSyncTimeMillis = System.currentTimeMillis()
        } else if (manual && outcome.appliedDelta == 0) {
            syncStatusMessage = outcome.message
        }
        if (outcome.appliedDelta > 0) {
            stageMilestoneNotifier.notifyIfNeeded(previousCreature, activeCreature)
        }
        if (outcome.inactivityPenaltyApplied) {
            inactivityPenaltyAlert = inactivityPenaltyMessage()
        }
        maybeCelebrateDiscovery(previousCreature, activeCreature)
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
        val completed = ExProgression.newCompletedCreature(
            creature = completedCreatureState.creature,
            stepsCompleted = completedCreatureState.steps,
            completedAt = System.currentTimeMillis(),
            eggRarityAtHatch = completedCreatureState.eggRarity,
            nickname = completedCreatureState.nickname,
        )

        collection = collection + completed
        val rewardRoll = consumePendingRewardRoll()
        playerStats = playerStats.copy(
            creaturesCompleted = playerStats.creaturesCompleted + 1,
        )
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
        if (collection.isNotEmpty()) {
            val updatedCollection = ExProgression.applyDrip(collection, amount)
            if (updatedCollection != collection) {
                collection = updatedCollection
                viewModelScope.launch {
                    repository.saveCollection(updatedCollection)
                }
            }
        }

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

    fun refreshPromoRedemptionStatus() {
        val repo = promoRepository ?: return
        viewModelScope.launch {
            runCatching {
                repo.syncRedemptionStatus(playerStats)
            }.onSuccess { synced ->
                if (synced.redeemedPromoCodes != playerStats.redeemedPromoCodes) {
                    playerStats = synced
                    repository.savePlayerStats(playerStats)
                }
            }
        }
    }

    fun redeemPromoCode(code: String) {
        val repo = promoRepository ?: return
        if (!isReady || isPromoLoading) return
        viewModelScope.launch {
            isPromoLoading = true
            promoStatusMessage = null
            try {
                val result = repo.redeemCode(code, playerStats)
                playerStats = result.playerStats
                promoStatusMessage = result.message
                repository.savePlayerStats(playerStats)
                cloudSaveSyncEngine.schedulePush(currentSnapshot())
            } catch (error: Exception) {
                promoStatusMessage = error.message ?: "Could not redeem code"
            } finally {
                isPromoLoading = false
            }
        }
    }

    fun isPromoCodeRedeemed(code: String): Boolean {
        return PromoRedemptionCodec.hasRedeemed(playerStats.redeemedPromoCodes, code)
    }

    private fun consumePendingRewardRoll(): EggRewardRoller.RollResult {
        val pending = playerStats.pendingRewardEggRarity?.let { EggRarity.fromRaw(it) }
        if (pending != null) {
            playerStats = playerStats.copy(pendingRewardEggRarity = null)
            return EggRewardRoller.RollResult(eggRarity = pending, rollValue = -1)
        }
        return EggRewardRoller.rollWeighted()
    }

    fun selectBattleFighter(fighter: CompletedCreature) {
        if (!BattleFeatures.enabled) return
        selectedBattleFighter = fighter
    }

    fun findQuickMatch() {
        if (!BattleFeatures.enabled) return
        val fighter = selectedBattleFighter ?: return
        val repository = battleRepository ?: return
        viewModelScope.launch {
            isBattleLoading = true
            resetActiveBattlePresentation()
            try {
                cloudSaveSyncEngine.schedulePush(currentSnapshot())
                val battle = repository.findQuickMatch(fighterCloudId(fighter))
                latestBattle = battle
                battleStatusMessage = battle?.let { battleOutcomeHeadline(it) }
                refreshBattleHistory()
            } catch (error: Exception) {
                battleStatusMessage = error.localizedMessage ?: "Quick match failed"
            } finally {
                isBattleLoading = false
            }
        }
    }

    fun createFriendChallenge() {
        if (!BattleFeatures.enabled) return
        val repository = battleRepository ?: return
        viewModelScope.launch {
            isBattleLoading = true
            resetActiveBattlePresentation()
            try {
                val result = repository.createChallenge()
                if (result != null) {
                    val (challenge, inviteCode) = result
                    battleInviteCode = inviteCode
                    activeBattleChallengeId = challenge.id
                    battleStatusMessage = "Share this battle code: $inviteCode"
                    startPollingForOpponentJoin(challenge.id)
                }
            } catch (error: Exception) {
                battleStatusMessage = error.localizedMessage ?: "Could not create challenge"
            } finally {
                isBattleLoading = false
            }
        }
    }

    fun acceptFriendChallenge(inviteCode: String) {
        if (!BattleFeatures.enabled) return
        val trimmed = inviteCode.trim()
        if (trimmed.isBlank()) {
            battleStatusMessage = "Enter your friend's invite code first."
            return
        }
        val fighter = selectedBattleFighter
        if (fighter == null) {
            battleStatusMessage = "Select a fighter above, then tap Accept."
            return
        }
        val repository = battleRepository ?: return
        viewModelScope.launch {
            isBattleLoading = true
            resetActiveBattlePresentation()
            try {
                cloudSaveSyncEngine.schedulePush(currentSnapshot())
                val challenge = repository.acceptChallengeByInvite(trimmed)
                if (challenge != null) {
                    activeBattleChallengeId = challenge.id
                    battleStatusMessage = "Opponent joined — lock in your fighter when ready."
                    submitBattlePick(challenge.id, fighter)
                } else {
                    battleStatusMessage = "Could not accept — sign in from Stats and try again."
                }
            } catch (error: Exception) {
                battleStatusMessage = error.localizedMessage ?: "Could not accept challenge"
            } finally {
                isBattleLoading = false
            }
        }
    }

    fun submitBattlePick(challengeId: String) {
        if (!BattleFeatures.enabled) return
        val fighter = selectedBattleFighter ?: return
        submitBattlePick(challengeId, fighter)
    }

    private fun submitBattlePick(challengeId: String, fighter: CompletedCreature) {
        val repository = battleRepository ?: return
        viewModelScope.launch {
            isBattleLoading = true
            battleStatusMessage = null
            try {
                cloudSaveSyncEngine.schedulePush(currentSnapshot())
                val (challenge, battle) = repository.submitPick(challengeId, fighterCloudId(fighter))
                if (battle != null) {
                    applyCompletedBattle(battle)
                    battlePollJob?.cancel()
                } else {
                    activeBattleChallengeId = challenge.id
                    battleStatusMessage = "Fighter locked in — waiting for opponent..."
                    startPollingForBattleReveal(challengeId)
                }
                refreshBattleHistory()
            } catch (error: Exception) {
                battleStatusMessage = error.localizedMessage ?: "Could not submit pick"
            } finally {
                isBattleLoading = false
            }
        }
    }

    fun refreshBattleHistory() {
        if (!BattleFeatures.enabled) return
        val repository = battleRepository ?: return
        viewModelScope.launch {
            runCatching {
                battleHistory = repository.listBattles()
            }
        }
    }

    fun resumeBattlePollingIfNeeded() {
        if (!BattleFeatures.enabled) return
        val challengeId = activeBattleChallengeId ?: return
        val message = battleStatusMessage.orEmpty()
        if (message.contains("waiting", ignoreCase = true)) {
            startPollingForBattleReveal(challengeId)
        } else if (battleInviteCode != null) {
            startPollingForOpponentJoin(challengeId)
        }
    }

    fun battleOutcomeHeadline(battle: BattleRecord): String {
        return BattleOutcomeText.headline(
            battle = battle,
            currentUserId = cloudAccountUiState.value.signedInUserId,
        )
    }

    private fun resetActiveBattlePresentation() {
        battlePollJob?.cancel()
        battleStatusMessage = null
        latestBattle = null
        battleInviteCode = null
        activeBattleChallengeId = null
    }

    private fun applyCompletedBattle(battle: BattleRecord) {
        latestBattle = battle
        battleStatusMessage = battleOutcomeHeadline(battle)
        battleInviteCode = null
        activeBattleChallengeId = null
    }

    private fun startPollingForBattleReveal(challengeId: String) {
        val repository = battleRepository ?: return
        battlePollJob?.cancel()
        battlePollJob = viewModelScope.launch {
            repeat(45) {
                delay(2_000)
                val challenge = runCatching { repository.getChallenge(challengeId) }.getOrNull() ?: return@repeat
                if (challenge.status == "complete") {
                    val battleId = challenge.battleId ?: return@launch
                    repeat(5) {
                        val battle = runCatching { repository.getBattle(battleId) }.getOrNull()
                        if (battle != null) {
                            applyCompletedBattle(battle)
                            refreshBattleHistory()
                            return@launch
                        }
                        delay(1_000)
                    }
                    return@launch
                }
            }
        }
    }

    private fun startPollingForOpponentJoin(challengeId: String) {
        val repository = battleRepository ?: return
        battlePollJob?.cancel()
        battlePollJob = viewModelScope.launch {
            repeat(45) {
                delay(2_000)
                val challenge = runCatching { repository.getChallenge(challengeId) }.getOrNull() ?: return@repeat
                if (challenge.opponentId != null) {
                    battleStatusMessage = "Opponent joined — lock in your fighter!"
                    return@launch
                }
                if (challenge.status != "pending") return@launch
            }
        }
    }

    private fun fighterCloudId(fighter: CompletedCreature): String {
        return fighter.id.takeIf { it > 0 }?.toString()
            ?: error("Save your game before battling so fighters have stable IDs")
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
        private const val HOME_VISIBLE_SYNC_DEBOUNCE_MILLIS = 15_000L
        private const val MANUAL_SYNC_HEALTH_CONNECT_RETRY_DELAY_MILLIS = 2_000L
    }
}
