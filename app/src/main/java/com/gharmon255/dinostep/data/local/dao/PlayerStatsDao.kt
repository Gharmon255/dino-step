package com.gharmon255.dinostep.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gharmon255.dinostep.data.local.entity.PlayerStatsEntity

@Dao
interface PlayerStatsDao {
    @Query("SELECT * FROM player_stats WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int = PlayerStatsEntity.STATS_ROW_ID): PlayerStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: PlayerStatsEntity)
}
