package com.gharmon255.dinostep.wear

data class WearSyncPublishResult(
    val success: Boolean,
    val statusMessage: String,
    val connectedNodeCount: Int,
    val payloadSummary: String,
    val dataPath: String,
    val payloadCreatureId: String = "",
    val payloadDisplayName: String = "—",
    val payloadStage: String = "—",
    val payloadProgressPercent: Float = 0f,
    val payloadEggRarity: String = "—",
    val payloadCreatureRarity: String = "—",
    val payloadIsAssetBacked: Boolean = false,
    val payloadStageDrawableKey: String = "",
    val payloadSteps: Int = 0,
    val payloadStepsUntilNext: Int = 0,
    val payloadNextStageLabel: String = "—",
)
