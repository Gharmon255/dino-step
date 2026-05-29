package com.gharmon255.dinostep.wear

data class WearSyncDebugState(
    val connectedNodeCount: Int = 0,
    val lastAttemptTimeMillis: Long? = null,
    val lastStatusMessage: String = "Not synced yet",
    val lastPayloadDisplayName: String = "—",
    val lastPayloadStage: String = "—",
    val lastPayloadSteps: Int? = null,
    val lastPayloadStepsUntilNext: Int? = null,
    val lastPayloadNextStageLabel: String = "—",
    val lastPayloadSummary: String = "—",
)
