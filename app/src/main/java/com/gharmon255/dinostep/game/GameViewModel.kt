package com.gharmon255.dinostep.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gharmon255.dinostep.data.DeveloperPreferences
import com.gharmon255.dinostep.data.repository.GameRepository
import com.gharmon255.dinostep.health.HealthConnectRepository
import com.gharmon255.dinostep.health.HealthConnectUiStatus
import com.gharmon255.dinostep.health.StepSyncCalculator
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureVisualMapper
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.EggRewardRoller
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.model.PlayerStats
import com.gharmon255.dinostep.model.Rarity
import com.gharmon255.dinostep.shared.wear.WearSyncEventType
import com.gharmon255.dinostep.garmin.GarminCompanionPublisher
import com.gharmon255.dinostep.wear.WearCreaturePayloadMapper
import com.gharmon255.dinostep.wear.WearDataLayerPublisher
import com.gharmon255.dinostep.wear.WearSyncDebugState
import com.gharmon255.dinostep.wear.WearSyncPublishResult
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: GameRepository,
    private val developerPreferences: DeveloperPreferences,
    private val healthConnectRepository: HealthConnectRepository,
    private val wearDataLayerPublisher: WearDataLayerPublisher,
    private val garminCompanionPublisher: GarminCompanionPublisher,
) : ViewModel() {
    private val testingEggFactory = TestingEggFactory(repository)
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
            isReady = true
            publishActiveCreatureToWatch(WearSyncEventType.NONE)
        }
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

    fun syncHealthSteps() {
        if (!isReady || isSyncing) {
            return
        }

        viewModelScope.launch {
            isSyncing = true
            syncStatusMessage = null

            try {
                val status = healthConnectRepository.resolveUiStatus()
                healthConnectStatus = status

                if (status !is HealthConnectUiStatus.Ready) {
                    syncStatusMessage = status.message
                    return@launch
                }

                val todaySteps = healthConnectRepository.readTodayStepTotal().getOrElse { error ->
                    healthConnectStatus = HealthConnectUiStatus.Error(
                        error.localizedMessage ?: "Failed to read steps from Health Connect",
                    )
                    syncStatusMessage = healthConnectStatus.message
                    return@launch
                }

                val syncResult = StepSyncCalculator.calculate(
                    playerStats = playerStats,
                    currentHealthConnectTodaySteps = todaySteps,
                )

                if (syncResult.delta > 0) {
                    applyStepsToCreature(syncResult.delta, countAsFake = false)
                    playerStats = syncResult.updatedStats
                    repository.savePlayerStats(playerStats)
                    syncStatusMessage =
                        "Synced ${syncResult.delta} steps (Health Connect today: ${syncResult.currentHealthConnectSteps})"
                } else {
                    syncStatusMessage = "No new steps to sync (Health Connect today: ${syncResult.currentHealthConnectSteps})"
                }
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
        val completed = CompletedCreature(
            creature = completedCreatureState.creature,
            stepsCompleted = completedCreatureState.steps,
            completedAt = System.currentTimeMillis(),
        )

        collection = collection + completed
        playerStats = playerStats.copy(
            creaturesCompleted = playerStats.creaturesCompleted + 1,
        )
        // Normal gameplay — never apply test species override (Sprint 3).
        val rewardRoll = EggRewardRoller.rollWeighted()
        activeCreature = repository.createRandomEggWithRarity(rewardRoll.eggRarity)
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
        activeCreature = repository.createRandomEggWithRarity(offer.rewardEggRarity)
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

    private fun applyStepsToCreature(amount: Int, countAsFake: Boolean) {
        if (amount <= 0) {
            return
        }

        val previous = activeCreature
        val wasRevealed = activeCreature.isRevealed
        val newSteps = activeCreature.steps + amount
        val nowRevealed = wasRevealed || newSteps >= activeCreature.creature.hatchStep
        val eggsHatchedDelta = if (!wasRevealed && nowRevealed) 1 else 0

        activeCreature = activeCreature.copy(
            steps = newSteps,
            isRevealed = nowRevealed,
        )

        playerStats = playerStats.copy(
            totalFakeStepsAdded = if (countAsFake) {
                playerStats.totalFakeStepsAdded + amount
            } else {
                playerStats.totalFakeStepsAdded
            },
            eggsHatched = playerStats.eggsHatched + eggsHatchedDelta,
        )

        val eventType = WearCreaturePayloadMapper.detectEventType(
            previous = previous,
            current = activeCreature,
        )
        persistActiveAndStats()
        publishActiveCreatureToWatch(eventType)
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
        }
    }
}
