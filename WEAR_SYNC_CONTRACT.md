# Wear OS sync contract (Dino Step)

Phone and Wear share the `:shared` module. State is published on the Wearable **Data Layer** at a single path.

## Data Layer path

| Constant | Value |
|----------|--------|
| `WearSyncPaths.CURRENT_CREATURE` | `/dino_step/current_creature` |

Both APKs use the same `applicationId` (`com.gharmon255.dinostep`) and matching signing keys (debug/release per build type).

## Payload encoding

`WearCreaturePayload` ↔ `DataMap` via `WearCreaturePayloadCodec` (`shared/.../wear/WearCreaturePayload.kt`).

### Required for decode

- `display_name` — if missing, payload is rejected (`fromDataMap` returns `null`).

### Core fields

| DataMap key | Property | Meaning |
|-------------|----------|---------|
| `creature_id` | `creatureId` | Stable catalog slug (e.g. `trex`). **Empty on legacy payloads.** |
| `creature_name` | `creatureName` | Internal creature name |
| `display_name` | `displayName` | UI label (egg mystery name or species name) |
| `stage` | `stage` | `EGG`, `BABY`, `JUVENILE`, `ADULT` |
| `current_steps` | `currentSteps` | Steps applied to active creature |
| `next_milestone` | `nextMilestone` | Next step threshold for current segment |
| `total_steps_required` | `totalStepsRequired` | Steps to fully grown |
| `progress_percent` | `progressPercent` | **0–100 within current stage only** (see below) |
| `steps_until_next_milestone` | `stepsUntilNextMilestone` | Legacy alias; often mirrors next-stage steps |
| `steps_until_next_stage` | `stepsUntilNextStage` | Steps until next growth stage |
| `next_stage_label` | `nextStageLabel` | e.g. `hatch`, `juvenile`, `adult`, `ready to claim` |
| `is_revealed` | `isRevealed` | Egg hatched / species known |
| `display_emoji` | `displayEmoji` | Fallback when no drawable |
| `species_short_label` | `speciesShortLabel` | Optional short label under art |
| `stage_scale` | `stageScale` | Visual scale hint |
| `egg_rarity` | `eggRarity` | `COMMON`, `RARE`, etc. |
| `creature_rarity` | `creatureRarity` | Set when revealed |
| `accent_color_argb` | `accentColorArgb` | Rarity accent (optional on old payloads) |
| `is_asset_backed` | `isAssetBacked` | **Optional** — phone sets true for 9 backed species |
| `stage_drawable_key` | `stageDrawableKey` | **Optional** — logical name e.g. `dino_trex_baby` |
| `event_type` | `eventType` | `NONE`, `HATCHED`, `GREW`, `COMPLETED` |
| `updated_at` | `updatedAtMillis` | Publish timestamp; watch picks newest on launch |

Legacy payloads may omit `creature_id`, `steps_until_next_stage`, `next_stage_label`, `is_asset_backed`, and `stage_drawable_key`. Codec fills safe defaults.

## Species ID rule

- Phone sends `creatureId` = `CreatureDefinition.id` (catalog slug).
- Supported **asset-backed** slugs: `tiny_raptor`, `triceratops`, `trex`, `stegosaurus`, `brachiosaurus`, `ankylosaurus`, `parasaurolophus`, `spinosaurus`, `pteranodon`.
- Legacy save aliases (`t_rex`, `pterodactyl`) normalize in catalog on phone; watch receives canonical id when phone maps from saved state.
- Watch art uses `DrawableCreatureResolver` with pattern `dino_{speciesId}_{stage}` (via `CreatureAssetNames`), **not** display-name switches.
- If `creatureId` is blank: resolver uses stage placeholders / emoji; UI does not crash.

## Stage progress rule

**Progress ring and `progress_percent` = progress within the current growth stage toward the next stage**, not lifetime egg→adult.

Examples (phone `CreatureDefinition.progressPercent`):

- Egg: `steps / hatchStep`
- Baby: `(steps - hatchStep) / (juvenileStep - hatchStep)`
- Juvenile: `(steps - juvenileStep) / (totalStepsRequired - juvenileStep)`
- Adult: 100%

Subtitle line uses `steps_until_next_stage` + `next_stage_label` (`WearStageProgress` on phone and watch).

## Fallback behavior (watch)

| Condition | Behavior |
|-----------|----------|
| Missing `creature_id` | Empty id → placeholder PNG or emoji |
| Unknown `stage` string | Defaults to `EGG` (`WearGrowthStage.fromRaw`) |
| Non-backed species | Placeholder vector per stage, not another species’ PNG |
| Missing drawable | `WearCreatureDrawableOrEmoji` shows emoji (default `🦕` if blank) |
| Unparseable DataMap | Item skipped; prior/mock state retained |

## Phone debug (DEBUG builds only)

Stats → **Wear sync debug** (`DevTools.isEnabled` / `BuildConfig.DEBUG`): last payload species id, display name, stage, rarities, progress %, asset-backed flag, drawable key, connected Wear node count, sync timestamp, **Force Wear Sync**.

## Emulator testing (no physical device required)

1. `./gradlew assembleDebug`
2. Run phone emulator + Wear OS emulator; pair via Wear OS companion / extended controls.
3. Open app on phone; use Developer Testing to force a species egg (DEBUG).
4. Stats → Force Wear Sync; confirm debug fields update.
5. Open Wear app; verify ring % and creature visual (PNG or placeholder/emoji).

Does **not** require Health Connect or real step counts for sync payload testing.

## Real device testing (still needed)

- Galaxy Watch / physical Wear paired over Bluetooth
- Long-running sync after step updates from Health Connect
- Battery / background delivery edge cases

**Not verified on physical hardware in Sprint 4** unless explicitly noted in release notes.

## Related code

| Area | Location |
|------|----------|
| Payload + codec | `shared/.../wear/WearCreaturePayload.kt` |
| Stage progress helpers | `shared/.../wear/WearStageProgress.kt` |
| Phone publish | `app/.../wear/WearDataLayerPublisher.kt` |
| Phone map | `app/.../wear/WearCreaturePayloadMapper.kt` |
| Watch listen | `wear/.../data/WatchStateRepository.kt` |
| Watch UI | `wear/.../ui/WearMainScreen.kt`, `CreatureVisual.kt` |
| Drawable resolver | `shared/.../visual/DrawableCreatureResolver.kt` |
