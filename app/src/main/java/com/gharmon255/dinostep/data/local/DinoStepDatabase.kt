package com.gharmon255.dinostep.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 6,
    exportSchema = false,
)
abstract class DinoStepDatabase : RoomDatabase() {
    abstract fun activeCreatureDao(): ActiveCreatureDao

    abstract fun completedCreatureDao(): CompletedCreatureDao

    abstract fun playerStatsDao(): PlayerStatsDao

    companion object {
        @Volatile
        private var instance: DinoStepDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE player_stats ADD COLUMN lastSyncedStepTotal INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE player_stats ADD COLUMN lastSyncDayStartMillis INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE active_creature ADD COLUMN eggRarity TEXT NOT NULL DEFAULT 'COMMON'",
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE player_stats ADD COLUMN lifetimeStepsApplied INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE active_creature ADD COLUMN hatchStep INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE active_creature ADD COLUMN juvenileStep INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE active_creature ADD COLUMN totalStepsRequired INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE active_creature ADD COLUMN economyVersion INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE active_creature ADD COLUMN nickname TEXT",
                )
                db.execSQL(
                    "ALTER TABLE completed_creatures ADD COLUMN nickname TEXT",
                )
            }
        }

        fun getInstance(context: Context): DinoStepDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DinoStepDatabase::class.java,
                    "dino_step.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
