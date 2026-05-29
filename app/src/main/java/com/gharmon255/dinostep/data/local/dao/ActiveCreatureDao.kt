package com.gharmon255.dinostep.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gharmon255.dinostep.data.local.entity.ActiveCreatureEntity

@Dao
interface ActiveCreatureDao {
    @Query("SELECT * FROM active_creature WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int = ActiveCreatureEntity.ACTIVE_ROW_ID): ActiveCreatureEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(creature: ActiveCreatureEntity)
}
