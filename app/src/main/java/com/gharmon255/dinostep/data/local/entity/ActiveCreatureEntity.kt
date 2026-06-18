package com.gharmon255.dinostep.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_creature")
data class ActiveCreatureEntity(
    @PrimaryKey val id: Int = ACTIVE_ROW_ID,
    val creatureId: String,
    val eggRarity: String,
    val currentSteps: Int,
    val startedAt: Long,
    val isRevealed: Boolean,
    val nickname: String? = null,
    val hatchStep: Int = 0,
    val juvenileStep: Int = 0,
    val totalStepsRequired: Int = 0,
    val economyVersion: Int = 0,
) {
    companion object {
        const val ACTIVE_ROW_ID = 1
    }

    val hasProgressionSnapshot: Boolean
        get() = economyVersion > 0 && hatchStep > 0 && totalStepsRequired > 0
}
