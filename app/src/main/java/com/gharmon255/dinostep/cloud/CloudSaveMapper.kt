package com.gharmon255.dinostep.cloud

import com.gharmon255.dinostep.data.GameSnapshot
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.PlayerStats
import com.gharmon255.dinostep.model.ProgressionThresholds
import java.time.Instant
import java.util.UUID

object CloudSaveMapper {
    fun toCloud(
        snapshot: GameSnapshot,
        revision: Long,
        updatedAt: String,
        lastRewardedEggRarity: String? = null,
        lastRewardRollPercent: Double? = null,
    ): CloudGameSave {
        val active = snapshot.activeCreature
        return CloudGameSave(
            revision = revision,
            updatedAt = updatedAt,
            activeCreature = CloudActiveCreature(
                speciesId = active.creature.id,
                eggRarity = active.eggRarity.name,
                steps = active.steps,
                isRevealed = active.isRevealed,
                nickname = active.nickname,
                startedAt = Instant.ofEpochMilli(active.startedAt).toString(),
                hatchStep = active.progression.hatchStep,
                juvenileStep = active.progression.juvenileStep,
                totalStepsRequired = active.progression.totalStepsRequired,
                economyVersion = active.progression.economyVersion,
            ),
            completedCreatures = snapshot.collection.map { completed ->
                CloudCompletedCreature(
                    id = completed.id.takeIf { it > 0 }?.toString() ?: UUID.randomUUID().toString(),
                    speciesId = completed.creature.id,
                    stepsCompleted = completed.stepsCompleted,
                    completedAt = Instant.ofEpochMilli(completed.completedAt).toString(),
                    nickname = completed.nickname,
                    eggRarityAtHatch = completed.eggRarityAtHatch.name,
                    exSteps = completed.exSteps,
                    exLevel = completed.exLevel,
                )
            },
            playerStats = CloudPlayerStats(
                eggsHatched = snapshot.playerStats.eggsHatched,
                creaturesCompleted = snapshot.playerStats.creaturesCompleted,
                lastSyncedStepTotal = snapshot.playerStats.lastSyncedStepTotal,
                lastSyncDayStartMillis = snapshot.playerStats.lastSyncDayStartMillis,
                lifetimeStepsApplied = snapshot.playerStats.lifetimeStepsApplied,
            ),
            lastRewardedEggRarity = lastRewardedEggRarity,
            lastRewardRollPercent = lastRewardRollPercent,
            pendingRewardEggRarity = snapshot.playerStats.pendingRewardEggRarity,
            redeemedPromoCodes = snapshot.playerStats.redeemedPromoCodes,
        )
    }

    fun toSnapshot(cloud: CloudGameSave): GameSnapshot? {
        val creature = CreatureCatalog.fallbackCreature(cloud.activeCreature.speciesId)
        val progression = ProgressionThresholds(
            hatchStep = cloud.activeCreature.hatchStep,
            juvenileStep = cloud.activeCreature.juvenileStep,
            totalStepsRequired = cloud.activeCreature.totalStepsRequired,
            economyVersion = cloud.activeCreature.economyVersion,
        )
        val active = ActiveCreatureState(
            creature = creature,
            eggRarity = EggRarity.fromRaw(cloud.activeCreature.eggRarity),
            progression = progression,
            steps = cloud.activeCreature.steps,
            startedAt = Instant.parse(cloud.activeCreature.startedAt).toEpochMilli(),
            isRevealed = cloud.activeCreature.isRevealed,
            nickname = cloud.activeCreature.nickname,
        ).normalized()

        val collection = cloud.completedCreatures.mapNotNull { entry ->
            val definition = CreatureCatalog.byId(entry.speciesId) ?: return@mapNotNull null
            CompletedCreature(
                id = entry.id.toLongOrNull() ?: 0L,
                creature = definition,
                stepsCompleted = entry.stepsCompleted,
                completedAt = Instant.parse(entry.completedAt).toEpochMilli(),
                nickname = entry.nickname,
                eggRarityAtHatch = EggRarity.fromRaw(entry.eggRarityAtHatch),
                exSteps = entry.exSteps,
                exLevel = entry.exLevel.coerceAtLeast(1),
            )
        }

        return GameSnapshot(
            activeCreature = active,
            collection = collection,
            playerStats = PlayerStats(
                eggsHatched = cloud.playerStats.eggsHatched,
                creaturesCompleted = cloud.playerStats.creaturesCompleted,
                lastSyncedStepTotal = cloud.playerStats.lastSyncedStepTotal,
                lastSyncDayStartMillis = cloud.playerStats.lastSyncDayStartMillis,
                lifetimeStepsApplied = cloud.playerStats.lifetimeStepsApplied,
                pendingRewardEggRarity = cloud.pendingRewardEggRarity,
                redeemedPromoCodes = cloud.redeemedPromoCodes,
            ),
        )
    }

    fun isLocalEmpty(snapshot: GameSnapshot): Boolean {
        return snapshot.collection.isEmpty() &&
            snapshot.playerStats.lifetimeStepsApplied == 0 &&
            snapshot.playerStats.creaturesCompleted == 0 &&
            snapshot.activeCreature.steps == 0 &&
            !snapshot.activeCreature.isRevealed
    }
}
