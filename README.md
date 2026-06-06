# Dino Step (Android / Wear OS)

Dino Step is a step-based dinosaur pet and collection game. Walk to earn progress on a mystery egg, hatch a creature, grow it from baby → juvenile → adult, then claim and collect species across rarity tiers.

## Platform

- **Android phone app** — `:app`
- **Wear OS companion** — `:wear` (read-only UI synced from the phone)
- **Shared module** — `:shared` (asset naming, Wear payload codec, drawable resolver)

Open the project root in **Android Studio** (Gradle root `Dino Step`).

Package: `com.gharmon255.dinostep`

## Core gameplay loop

1. Receive a **mystery egg** (rarity: Common → Legendary).
2. **Walk steps** (step counter on device; DEBUG builds can add fake steps on Home).
3. **Hatch** when step threshold is reached.
4. Grow **baby → juvenile → adult** with additional steps per stage.
5. **Claim** the adult to add the species to your **collection**.
6. Eggs roll **egg rarity**; the species inside uses its own **creature rarity** from the catalog.

## Species and art status

See [`SPECIES_ROSTER.md`](SPECIES_ROSTER.md) for the full roster.

| | Count |
|---|------|
| Catalog species | **29** |
| Asset-backed (PNG art) | **29** |
| Placeholder / emoji only | **0** |

**Source assets** live in the sibling repo **`dino-step-assets`** (`dinos/` folder). Copy into this project after art is ready.

**Naming pattern** (`drawable-nodpi` on phone and Wear):

- `dino_{speciesId}_baby.png`
- `dino_{speciesId}_juvenile.png`
- `dino_{speciesId}_adult.png`

Non-backed species use `dino_placeholder_{stage}` vectors and species emoji — never another species’ PNGs.

## Project layout

| Module | Purpose |
|--------|---------|
| `app/` | Phone UI, `CreatureCatalog.kt`, Room persistence, DEBUG dev tools |
| `wear/` | Wear OS UI, sync receiver |
| `shared/` | `CreatureAssetNames`, `DrawableCreatureResolver`, `WearCreaturePayload` |

**Key paths**

- `app/src/main/java/.../model/CreatureCatalog.kt` — species catalog
- `shared/.../visual/CreatureAssetNames.kt` — asset-backed species list
- `app/src/main/res/drawable-nodpi/` — phone stage PNGs
- `wear/src/main/res/drawable-nodpi/` — Wear stage PNGs (same files as phone)

**Docs**

- [`SPECIES_ROSTER.md`](SPECIES_ROSTER.md) — ids, rarities, steps, asset status
- [`SPECIES_ONBOARDING_CHECKLIST.md`](SPECIES_ONBOARDING_CHECKLIST.md) — add a new asset-backed species
- [`WEAR_SYNC_CONTRACT.md`](WEAR_SYNC_CONTRACT.md) — phone → Wear Data Layer payload
- [`LAUNCH_CHECKLIST.md`](LAUNCH_CHECKLIST.md) — Google Play internal testing readiness (icons, privacy, signing, smoke tests)

## Developer testing (DEBUG only)

When `BuildConfig.DEBUG` is true (`DevTools.isEnabled`):

- **Stats / developer sections** — random egg, per-rarity eggs, reset/clear helpers
- **Force selected species egg** — `NextEggTestSpecies` picker (29 asset-backed ids)
- **Home** — fake step controls for emulator testing
- **Wear** — pair phone + Wear emulators; no real steps required for layout checks

These controls must **not** ship in release builds; production users rely on real steps and normal egg rolls.

## Asset workflow

1. Create or clean PNGs in **`dino-step-assets/dinos/`** (see `dino-step-assets/ADD_SPECIES_CHECKLIST.md` and `species_queue.md`).
2. Validate: **RGBA**, **1024×1024**, alpha **0–255**, hidden RGB cleared under transparency.
3. Copy the same three PNGs to **`app/.../drawable-nodpi/`** and **`wear/.../drawable-nodpi/`**.
4. Update **`CreatureCatalog.kt`**, **`CreatureAssetNames.assetBackedSpeciesIds`**, **`NextEggTestSpecies`**, and **`SPECIES_ROSTER.md`** (both app repos).
5. Build, test on emulators (Home, Collection, paired Wear), then commit.

## Build and run

**Command line**

```bash
./gradlew assembleDebug
```

**Android Studio**

1. Open the project folder.
2. Run the **app** configuration on a phone emulator.
3. Run the **wear** configuration on a Wear OS emulator (pair with the phone AVD).

**Java**

If `JAVA_HOME` is unset, use Android Studio’s bundled JBR, for example:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

No physical phone or watch is required for emulator-based development.

## Repo notes

- Do **not** commit `build/`, `.gradle/`, `local.properties`, or IDE noise (see `.gitignore`).
- Push via **GitHub**; keep `SPECIES_ROSTER.md` identical to `dino-step-ios` when roster or art changes.
- **`dino-step-assets`** is the source-of-truth workspace for PNGs before import.

## Known limitations / next work

- Catalog art is **complete** (29/29 species, all stages). See `dino-step-assets/species_queue.md` for expansion notes.
- UI polish and validation on physical phone + Wear hardware.
- Wear is read-only; progression is driven on the phone.
