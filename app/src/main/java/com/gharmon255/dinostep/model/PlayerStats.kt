package com.gharmon255.dinostep.model

data class PlayerStats(
    val totalFakeStepsAdded: Int = 0,
    val eggsHatched: Int = 0,
    val creaturesCompleted: Int = 0,
    val lastSyncedStepTotal: Int = 0,
    val lastSyncDayStartMillis: Long = 0L,
    val lifetimeStepsApplied: Int = 0,
    val pendingRewardEggRarity: String? = null,
    val redeemedPromoCodes: String? = null,
)
