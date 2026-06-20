package com.gharmon255.dinostep.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gharmon255.dinostep.data.local.entity.CompletedCreatureEntity

@Dao
interface CompletedCreatureDao {
    @Query("SELECT * FROM completed_creatures ORDER BY completedAt ASC")
    suspend fun getAllOrderedByCompletedAt(): List<CompletedCreatureEntity>

    @Insert
    suspend fun insert(creature: CompletedCreatureEntity)

    @androidx.room.Update
    suspend fun update(creature: CompletedCreatureEntity)

    @Query("UPDATE completed_creatures SET nickname = :nickname WHERE id = :id")
    suspend fun updateNickname(id: Long, nickname: String?)

    @Query("DELETE FROM completed_creatures WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM completed_creatures")
    suspend fun deleteAll()
}
