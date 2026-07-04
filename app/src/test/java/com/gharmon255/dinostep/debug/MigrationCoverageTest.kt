package com.gharmon255.dinostep.debug

import com.gharmon255.dinostep.data.local.DINO_STEP_DB_VERSION
import com.gharmon255.dinostep.data.local.DinoStepDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the single most common way a shipped update silently destroys player saves: bumping the
 * Room `@Database(version = ...)` without registering a matching [androidx.room.migration.Migration].
 * When that happens Room falls back to a destructive recreate (or crashes), and every dino is gone.
 *
 * These are pure JVM checks over [DinoStepDatabase.ALL_MIGRATIONS] — no emulator required — so they
 * run on every `./gradlew :app:testDebugUnitTest` and fail the build the moment the upgrade path
 * develops a hole.
 */
class MigrationCoverageTest {
    private val migrations = DinoStepDatabase.ALL_MIGRATIONS

    @Test
    fun migrationsFormAnUnbrokenChainFromV1ToCurrentVersion() {
        val ordered = migrations.sortedBy { it.startVersion }

        assertEquals(
            "First migration must start at version 1",
            1,
            ordered.first().startVersion,
        )
        assertEquals(
            "Last migration must end at the current DB version",
            DINO_STEP_DB_VERSION,
            ordered.last().endVersion,
        )

        // Each migration must advance exactly one version and pick up where the previous left off.
        ordered.forEachIndexed { index, migration ->
            assertEquals(
                "Migration #$index must move up exactly one schema version",
                migration.startVersion + 1,
                migration.endVersion,
            )
            if (index > 0) {
                assertEquals(
                    "Gap in migration chain before ${migration.startVersion}->${migration.endVersion}",
                    ordered[index - 1].endVersion,
                    migration.startVersion,
                )
            }
        }
    }

    @Test
    fun everyConsecutiveVersionPairHasExactlyOneMigration() {
        for (from in 1 until DINO_STEP_DB_VERSION) {
            val matches = migrations.count { it.startVersion == from && it.endVersion == from + 1 }
            assertEquals(
                "Expected exactly one migration for $from -> ${from + 1} (found $matches)",
                1,
                matches,
            )
        }
    }

    @Test
    fun migrationCountMatchesVersionSpan() {
        // A contiguous 1..N single-step chain always has exactly (N - 1) migrations.
        assertEquals(DINO_STEP_DB_VERSION - 1, migrations.size)
    }

    @Test
    fun noMigrationSkipsOrRunsBackwards() {
        migrations.forEach { migration ->
            assertTrue(
                "Migration ${migration.startVersion}->${migration.endVersion} must move forward",
                migration.endVersion > migration.startVersion,
            )
        }
    }
}
