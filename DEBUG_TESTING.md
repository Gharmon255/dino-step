# Dino Step — Deep Debug & Save-Integrity Checks (Android)

This is the safety net for the two scariest ways a live game can break between releases:

1. **A new version download silently loses player data.**
2. **The "miss a day → dino goes back to an egg → then grows forward" flow breaks.**

Everything here runs as **fast JVM unit tests — no emulator required** — so you can (and should) run
it before every Play Store upload.

## Run it

```bash
scripts/run-debug-checks.sh          # just the deep debug suites (seconds)
scripts/run-debug-checks.sh --all    # debug suites + all other unit tests
```

Or directly:

```bash
./gradlew :app:testDebugUnitTest --tests "com.gharmon255.dinostep.debug.*"
```

HTML report: `app/build/reports/tests/testDebugUnitTest/index.html`

## What each suite proves

All suites live in `app/src/test/java/com/gharmon255/dinostep/debug/`.

### `MigrationCoverageTest` — "an update can never wipe the DB"
The single most common cause of "my dinos disappeared after updating" is bumping the Room
`@Database(version = ...)` without a matching migration, which triggers a destructive recreate.
This test inspects `DinoStepDatabase.ALL_MIGRATIONS` and fails the build if the upgrade path from
version 1 → current has any **gap, duplicate, backwards step, or missing link**. If you add a
column, this test forces you to add the migration.

### `AppUpdateSaveIntegrityTest` — "nothing is lost on a new version"
A store update keeps the app's data directory, so the game reloads from the same Room tables (and,
for signed-in players, the same cloud row). This suite round-trips data across every serialization
boundary it must survive:
- **Room entity ⇆ domain** for `PlayerStats`, the active creature, and completed creatures (the
  exact bytes that persist between versions).
- **Legacy back-fill**: a save written by a pre-economy build (all economy columns `0`) is upgraded
  in place to the frozen v1 curve *without touching steps, rarity, or nickname*.
- **Cloud round trip** preserves the active creature, the full collection, stats, the pending promo
  reward, and redeemed promo codes.
- Pins two deliberate behaviors so they can't change by accident: the debug-only
  `totalFakeStepsAdded` counter is not synced to cloud, and unknown species in a cloud save are
  dropped rather than crashing the restore.

### `DayMissRecoveryTest` — "miss a day, drop to an egg, grow back"
Drives the real domain logic end to end: grow a creature to **adult → miss an inactive day → it
resets to a 500-step egg → regrow to adult again**. Verifies:
- The penalty resets *progress* (500 steps, hidden, `EGG` stage) but preserves *identity* (species,
  rarity, nickname, progression curve, hatch date).
- The 5,000-step daily goal boundary (exactly 5,000 = safe, 4,999 = reset).
- Idempotency: a freshly reset egg isn't stomped again the next inactive day.
- After a reset the creature re-hatches at the hatch threshold (hatch counter increments again) and
  climbs back through baby/juvenile/adult.
- `DayRolloverEvaluator.resolveYesterdaySteps` prefers the cached HealthConnect total when it maps
  to yesterday, otherwise falls back to a fresh read.

### `ProgressionLifecycleTest` — "the growth economy is sane"
Per-rarity adult totals (40k → 240k), ordered hatch/juvenile/total thresholds (~18% / ~45%), exact
stage boundaries, "reaches adult exactly at its total," and the weighted reward-roll table
(`0–64` common … `99` legendary) including out-of-range clamping and distribution sanity.

### `PromoRedemptionTest` — "codes are one-time-use, even offline"
The on-device redeemed-codes codec is stable and case/whitespace-insensitive, so a code (`epic20`,
`legend20`) can't be re-redeemed by changing capitalization or spacing — this is what makes local
redemption safe when a player never signs in.

### `LifecycleSimulationTest` — property-based journeys (the deep one)
Rather than checking fixed points, this plays out **150 seeded, 45-day player journeys** with random
daily step counts and random missed days. After **every day** it asserts a battery of invariants:
steps never negative, the penalty fires exactly when the real guard says, a penalty always yields a
hidden 500-step egg with identical identity/curve, `eggsHatched`/`lifetimeStepsApplied` match an
independent oracle and stay monotonic, the reported stage always agrees with steps, and — crucially —
the **full save round-trips losslessly (Room entity + cloud) at that exact moment**. Seeds are fixed,
so any failure reproduces deterministically.

### `SaveRoundTripFuzzTest` — future-proof save integrity
Generates **thousands of random-but-valid** active creatures, completed creatures, player stats, and
full snapshots and round-trips them through both the Room entity mappers and the cloud mapper. Uses
full data-class equality where possible, so if anyone ever adds a model field and forgets to map it,
a random value in that field fails here automatically — no bespoke assertion required.

### `CrossPlatformParityTest` — Android and iOS can't drift
Pins the shared "rules of the game" constants (5,000 / 500 step thresholds, per-rarity adult totals,
the reward-roll table, promo codes). The iOS repo has a mirror (`CrossPlatformParityTests.swift`)
asserting the same numbers, so tuning a value on one platform fails the build until the other matches.

## Manual on-device checks (the parts a unit test can't cover)

Real update behavior across the actual SQLite file is best confirmed once on a device/emulator:

1. **Update-in-place**: install the current Play build, play until you have a collection + an
   in-progress egg, then `adb install -r app-release.aab`-equivalent (or the Play internal-testing
   update) and relaunch. Collection, steps, and stats should be intact.
2. **Full Room migration walk (optional, gold standard)**: enable `exportSchema = true` +
   `room.schemaLocation` and add `androidx.room:room-testing` to run `MigrationTestHelper` across
   v1→9 with real data. Left out by default to avoid checking in schema JSONs.
3. **Miss a day for real**: use the in-app debug `simulateInactiveDayForTesting(...)` hook (or leave
   the app for a day) and confirm the active dino visibly returns to an egg and the notification
   fires.
