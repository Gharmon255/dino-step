package com.gharmon255.dinostep.cloud

data class CloudGameSave(
    val schemaVersion: Int = SCHEMA_VERSION,
    val revision: Long,
    val updatedAt: String,
    val activeCreature: CloudActiveCreature,
    val completedCreatures: List<CloudCompletedCreature>,
    val playerStats: CloudPlayerStats,
    val lastRewardedEggRarity: String? = null,
    val lastRewardRollPercent: Double? = null,
    val pendingRewardEggRarity: String? = null,
) {
    companion object {
        const val SCHEMA_VERSION = 2
    }
}

data class CloudActiveCreature(
    val speciesId: String,
    val eggRarity: String,
    val steps: Int,
    val isRevealed: Boolean,
    val nickname: String?,
    val startedAt: String,
    val hatchStep: Int,
    val juvenileStep: Int,
    val totalStepsRequired: Int,
    val economyVersion: Int,
)

data class CloudCompletedCreature(
    val id: String,
    val speciesId: String,
    val stepsCompleted: Int,
    val completedAt: String,
    val nickname: String?,
    val eggRarityAtHatch: String = "COMMON",
    val exSteps: Int = 0,
    val exLevel: Int = 1,
)

data class CloudPlayerStats(
    val eggsHatched: Int,
    val creaturesCompleted: Int,
    val lastSyncedStepTotal: Int,
    val lastSyncDayStartMillis: Long,
    val lifetimeStepsApplied: Int,
)

data class CloudSaveRow(
    val userId: String,
    val schemaVersion: Int,
    val revision: Long,
    val save: CloudGameSave,
    val updatedAt: String,
)

data class CloudSession(
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val email: String?,
    val provider: String?,
)

sealed class CloudSaveConflict {
    data class LocalVsCloud(
        val local: CloudGameSave,
        val cloud: CloudGameSave,
    ) : CloudSaveConflict()
}

enum class CloudSyncStatus {
    Unavailable,
    SignedOut,
    Syncing,
    BackedUp,
    Error,
}

data class CloudAccountUiState(
    val isConfigured: Boolean,
    val syncStatus: CloudSyncStatus,
    val signedInUserId: String? = null,
    val signedInEmail: String? = null,
    val signedInProvider: String? = null,
    val lastBackedUpAtMillis: Long? = null,
    val lastError: String? = null,
    val pendingConflict: CloudSaveConflict.LocalVsCloud? = null,
)
