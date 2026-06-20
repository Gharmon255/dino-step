package com.gharmon255.dinostep.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "completed_creatures")
data class CompletedCreatureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val creatureId: String,
    val name: String,
    val rarity: String,
    val habitat: String,
    val completedStepTotal: Int,
    val completedAt: Long,
    val nickname: String? = null,
    val eggRarityAtHatch: String = "COMMON",
    val exSteps: Int = 0,
    val exLevel: Int = 1,
)
