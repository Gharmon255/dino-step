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

/**
 * Single source of truth for the Room schema version. Referenced by the [Database] annotation and
 * verified by `MigrationCoverageTest` so a bumped version can never ship without a matching
 * migration (the #1 cause of "my save disappeared after updating").
 */
internal const val DINO_STEP_DB_VERSION = 9

@Database(
    entities = [
        ActiveCreatureEntity::class,
        CompletedCreatureEntity::class,
        PlayerStatsEntity::class,
    ],
    version = DINO_STEP_DB_VERSION,
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

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE completed_creatures ADD COLUMN eggRarityAtHatch TEXT NOT NULL DEFAULT 'COMMON'",
                )
                db.execSQL(
                    "ALTER TABLE completed_creatures ADD COLUMN exSteps INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE completed_creatures ADD COLUMN exLevel INTEGER NOT NULL DEFAULT 1",
                )
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE player_stats ADD COLUMN pendingRewardEggRarity TEXT",
                )
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE player_stats ADD COLUMN redeemedPromoCodes TEXT",
                )
            }
        }

        /**
         * Every migration the app ships, in order. This is the exact list handed to Room and the
         * one `MigrationCoverageTest` inspects to prove the 1..[DINO_STEP_DB_VERSION] upgrade path
         * is unbroken (no gaps, no duplicates, terminates at the current version).
         */
        internal val ALL_MIGRATIONS: Array<Migration> = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
        )

        fun getInstance(context: Context): DinoStepDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DinoStepDatabase::class.java,
                    "dino_step.db",
                )
                    .addMigrations(*ALL_MIGRATIONS)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
