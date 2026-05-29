package com.gharmon255.dinostep.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_stats")
data class PlayerStatsEntity(
    @PrimaryKey val id: Int = STATS_ROW_ID,
    val totalFakeStepsAdded: Int = 0,
    val eggsHatched: Int = 0,
    val creaturesCompleted: Int = 0,
) {
    companion object {
        const val STATS_ROW_ID = 1
    }
}
