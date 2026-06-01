package com.gharmon255.dinostep.wear

data class WearSyncDebugState(
    val connectedNodeCount: Int = 0,
    val lastAttemptTimeMillis: Long? = null,
    val lastStatusMessage: String = "Not synced yet",
    val lastPayloadCreatureId: String = "—",
    val lastPayloadDisplayName: String = "—",
    val lastPayloadStage: String = "—",
    val lastPayloadProgressPercent: Float? = null,
    val lastPayloadEggRarity: String = "—",
    val lastPayloadCreatureRarity: String = "—",
    val lastPayloadIsAssetBacked: Boolean = false,
    val lastPayloadStageDrawableKey: String = "—",
    val lastPayloadSteps: Int? = null,
    val lastPayloadStepsUntilNext: Int? = null,
    val lastPayloadNextStageLabel: String = "—",
    val lastPayloadSummary: String = "—",
)
