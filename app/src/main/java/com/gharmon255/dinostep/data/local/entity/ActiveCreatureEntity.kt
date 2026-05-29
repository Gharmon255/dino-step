package com.gharmon255.dinostep.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_creature")
data class ActiveCreatureEntity(
    @PrimaryKey val id: Int = ACTIVE_ROW_ID,
    val creatureId: String,
    val currentSteps: Int,
    val startedAt: Long,
    val isRevealed: Boolean,
) {
    companion object {
        const val ACTIVE_ROW_ID = 1
    }
}
