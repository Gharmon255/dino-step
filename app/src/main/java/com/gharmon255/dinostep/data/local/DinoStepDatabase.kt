package com.gharmon255.dinostep.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gharmon255.dinostep.data.local.dao.ActiveCreatureDao
import com.gharmon255.dinostep.data.local.dao.CompletedCreatureDao
import com.gharmon255.dinostep.data.local.dao.PlayerStatsDao
import com.gharmon255.dinostep.data.local.entity.ActiveCreatureEntity
import com.gharmon255.dinostep.data.local.entity.CompletedCreatureEntity
import com.gharmon255.dinostep.data.local.entity.PlayerStatsEntity

@Database(
    entities = [
        ActiveCreatureEntity::class,
        CompletedCreatureEntity::class,
        PlayerStatsEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class DinoStepDatabase : RoomDatabase() {
    abstract fun activeCreatureDao(): ActiveCreatureDao

    abstract fun completedCreatureDao(): CompletedCreatureDao

    abstract fun playerStatsDao(): PlayerStatsDao

    companion object {
        @Volatile
        private var instance: DinoStepDatabase? = null

        fun getInstance(context: Context): DinoStepDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DinoStepDatabase::class.java,
                    "dino_step.db",
                ).build().also { instance = it }
            }
        }
    }
}
