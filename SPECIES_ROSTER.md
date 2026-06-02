# Dino Step — Canonical Species Roster

**Source of truth** for cross-platform catalog alignment (iOS and Android).  
When code disagrees with this document, **treat this file as intended design** and update the diverging platform in a dedicated catalog-alignment sprint.

**Canonical source:** `dino-step-ios` — `Dino Step/Game/CreatureCatalog.swift` (June 2026).  
**Companion docs:** `SPECIES_ONBOARDING_CHECKLIST.md`, `WATCH_SYNC_CONTRACT.md` (iOS) / `WEAR_SYNC_CONTRACT.md` (Android).

---

## How to read this document

| Column | Meaning |
|--------|---------|
| **species id** | Stable slug for saves, assets, dev picker, and watch `speciesId` / `creature_id` |
| **steps to hatch** | Steps while egg → hatch (baby) |
| **steps baby → juvenile** | Steps after hatch until juvenile |
| **steps juvenile → adult** | Steps after juvenile until adult (claim) |
| **total steps** | Steps required to reach adult (claim) |
| **asset-backed** | `true` = listed in `assetBackedSpeciesIds`; resolver may use `dino_{id}_{stage}` |
| **asset prefix** | Base name for stage PNGs: `dino_{speciesId}` |
| **asset status** | Whether PNGs exist in app bundles today |

**Growth stages:** EGG (0 … hatch−1) → BABY (hatch … juvenile−1) → JUVENILE (juvenile … total−1) → ADULT (total+).

**Egg rarity vs creature rarity:** Mystery eggs roll an **egg rarity**; the species inside has its own **creature rarity** (below). Random species for an egg use the creature’s roster rarity tier.

---

## Asset-backed species (15)

These ids are in `CreatureAssetVisual.assetBackedSpeciesIds` (iOS) and `CreatureAssetNames.assetBackedSpeciesIds` (Android).

| species id | display name | rarity | habitat | steps to hatch | steps baby → juvenile | steps juvenile → adult | total steps | asset-backed | asset prefix | asset status |
|------------|--------------|--------|---------|----------------|----------------------|------------------------|-------------|--------------|--------------|--------------|
| `tiny_raptor` | Tiny Raptor | Common | Jungle | 1,600 | 2,400 | 4,000 | 8,000 | true | `dino_tiny_raptor` | **shipped** |
| `triceratops` | Triceratops | Common | Plains | 2,000 | 3,000 | 5,000 | 10,000 | true | `dino_triceratops` | **shipped** |
| `ankylosaurus` | Ankylosaurus | Common | Rocky | 2,400 | 3,600 | 6,000 | 12,000 | true | `dino_ankylosaurus` | **shipped** |
| `parasaurolophus` | Parasaurolophus | Common | Forest | 2,200 | 3,300 | 5,500 | 11,000 | true | `dino_parasaurolophus` | **shipped** |
| `pachycephalosaurus` | Pachycephalosaurus | Common | Rocky | 2,500 | 3,750 | 6,250 | 12,500 | true | `dino_pachycephalosaurus` | **shipped** |
| `stegosaurus` | Stegosaurus | Uncommon | Forest | 3,600 | 5,400 | 9,000 | 18,000 | true | `dino_stegosaurus` | **shipped** |
| `pteranodon` | Pteranodon | Uncommon | Mountain | 4,400 | 6,600 | 11,000 | 22,000 | true | `dino_pteranodon` | **shipped** |
| `brachiosaurus` | Brachiosaurus | Uncommon | Plains | 4,000 | 6,000 | 10,000 | 20,000 | true | `dino_brachiosaurus` | **shipped** |
| `dilophosaurus` | Dilophosaurus | Uncommon | Jungle | 4,000 | 6,000 | 10,000 | 20,000 | true | `dino_dilophosaurus` | **shipped** |
| `iguanodon` | Iguanodon | Uncommon | Forest | 3,800 | 5,700 | 9,500 | 19,000 | true | `dino_iguanodon` | **code-wired; assets missing** |
| `carnotaurus` | Carnotaurus | Uncommon | Volcano | 4,800 | 7,200 | 12,000 | 24,000 | true | `dino_carnotaurus` | **shipped** |
| `trex` | T-Rex | Rare | Volcano | 10,000 | 15,000 | 25,000 | 50,000 | true | `dino_trex` | **shipped** |
| `spinosaurus` | Spinosaurus | Rare | Swamp | 12,000 | 18,000 | 30,000 | 60,000 | true | `dino_spinosaurus` | **shipped** |
| `allosaurus` | Allosaurus | Rare | Rocky | 9,600 | 14,400 | 24,000 | 48,000 | true | `dino_allosaurus` | **shipped** |
| `mosasaurus` | Mosasaurus | Rare | Ocean | 13,000 | 19,500 | 32,500 | 65,000 | true | `dino_mosasaurus` | **shipped** |

**Stage file names (when shipped):** `dino_{speciesId}_baby`, `dino_{speciesId}_juvenile`, `dino_{speciesId}_adult`.

**Iguanodon:** Listed in `assetBackedSpeciesIds` on both platforms; resolver will look up PNGs and fall back to emoji/placeholder until art is imported. Do not remove from asset-backed set until assets ship or an explicit roster change.

---

## Non-asset-backed species (14)

Emoji / placeholder visuals only. **Never** use another species’ `dino_*` art (Android: `dino_placeholder_{stage}`).

| species id | display name | rarity | habitat | steps to hatch | steps baby → juvenile | steps juvenile → adult | total steps | asset-backed | asset prefix | notes |
|------------|--------------|--------|---------|----------------|----------------------|------------------------|-------------|--------------|--------------|-------|
| `gallimimus` | Gallimimus | Common | Plains | 1,800 | 2,700 | 4,500 | 9,000 | false | — | |
| `baryonyx` | Baryonyx | Uncommon | Swamp | 5,000 | 7,500 | 12,500 | 25,000 | false | — | |
| `velociraptor_alpha` | Velociraptor Alpha | Rare | Jungle | 9,000 | 13,500 | 22,500 | 45,000 | false | — | |
| `therizinosaurus` | Therizinosaurus | Rare | Forest | 11,000 | 16,500 | 27,500 | 55,000 | false | — | |
| `giganotosaurus` | Giganotosaurus | Epic | Plains | 17,000 | 25,500 | 42,500 | 85,000 | false | — | |
| `quetzalcoatlus` | Quetzalcoatlus | Epic | Mountain | 18,000 | 27,000 | 45,000 | 90,000 | false | — | |
| `indominus_rex_style_hybrid` | Indominus Rex Style Hybrid | Epic | Lab | 19,000 | 28,500 | 47,500 | 95,000 | false | — | iOS auto-slug; Android catalog id is `indominus_hybrid` — see [Legacy IDs](#legacy-ids-and-aliases) |
| `ancient_spinosaurus` | Ancient Spinosaurus | Epic | Swamp | 20,000 | 30,000 | 50,000 | 100,000 | false | — | Distinct legendary variant; not base `spinosaurus` art |
| `volcanic_t_rex` | Volcanic T-Rex | Legendary | Volcano | 25,000 | 37,500 | 62,500 | 125,000 | false | — | Not `trex` art |
| `frost_raptor` | Frost Raptor | Legendary | Ice | 22,000 | 33,000 | 55,000 | 110,000 | false | — | |
| `shadow_triceratops` | Shadow Triceratops | Legendary | Dark | 26,000 | 39,000 | 65,000 | 130,000 | false | — | Not `triceratops` art |
| `titanosaur` | Titanosaur | Legendary | Plains | 30,000 | 45,000 | 75,000 | 150,000 | false | — | |
| `cosmic_pterodactyl` | Cosmic Pterodactyl | Legendary | Sky | 35,000 | 52,500 | 87,500 | 175,000 | false | — | Not `pteranodon` art |
| `ancient_apex_rex` | Ancient Apex Rex | Legendary | Volcano | 40,000 | 60,000 | 100,000 | 200,000 | false | — | Not `trex` art |

**iOS save UUIDs:** Creatures use stable UUIDs per catalog index in `CreatureCatalog.swift`; do not reassign UUIDs when editing roster entries.

---

## Known cross-platform drift (catalog not yet aligned)

Canonical values are **iOS** (this document). **Android** `CreatureCatalog.kt` must be updated to match — do not change iOS catalog for these without a deliberate roster revision.

| species id | field | canonical (iOS) | Android current | platform to change |
|------------|-------|-----------------|-----------------|-------------------|
| `brachiosaurus` | steps to hatch | 4,000 | 4,800 | **Android** |
| `brachiosaurus` | steps baby → juvenile | 6,000 | 7,200 | **Android** |
| `brachiosaurus` | steps juvenile → adult | 10,000 | 12,000 | **Android** |
| `brachiosaurus` | total steps | 20,000 | 24,000 | **Android** |
| `pachycephalosaurus` | rarity | Common | Uncommon | **Android** |
| `carnotaurus` | rarity | Uncommon | Rare | **Android** |
| `allosaurus` | rarity | Rare | Epic | **Android** |
| `mosasaurus` | rarity | Rare | Legendary | **Android** |

**iOS:** Already matches this roster for the rows above.  
**Iguanodon:** Rarity Uncommon on both; asset gap on both — no catalog drift, only missing PNGs.

---

## Legacy IDs and aliases

| Alias | maps to | used for |
|-------|---------|----------|
| `t_rex` | `trex` | Older saves / Android `legacyCreatureIdAliases` |
| `pterodactyl` | `pteranodon` | Older saves; display name “Pterodactyl” |
| `Pterodactyl` (display) | `pteranodon` | iOS `legacySpeciesNameAliases` |
| `tyrannosaurus`, `tyrannosaurus_rex` | `trex` | Dev picker / asset resolver (iOS) |
| `indominus_hybrid` | — | Android catalog id for Indominus Rex Style Hybrid; iOS slug may be `indominus_rex_style_hybrid` until ids unified |

**Do not** map `volcanic_t_rex` or `ancient_apex_rex` to `trex` for artwork.

---

## Egg assets (rarity, not species)

Mystery eggs use **egg rarity** drawables (same names on both platforms):

| egg rarity | drawable base name |
|------------|-------------------|
| Common | `egg_common` |
| Uncommon | `egg_uncommon` |
| Rare | `egg_rare` |
| Epic | `egg_epic` |
| Legendary | `egg_legendary` |

Android may also try `egg_{rarity}_(1)` as a fallback candidate. Species stage art is never `egg_*`.

---

## Rarity egg pools (creature roster tier)

When a mystery egg of tier X is opened, a **random species** is chosen from all creatures whose **creature rarity** equals tier X (see tables above).

After Android alignment, uncommon eggs include: Stegosaurus, Pteranodon, Brachiosaurus, Dilophosaurus, Iguanodon, Carnotaurus, Baryonyx — not Pachycephalosaurus (Common).

---

## Changelog

| Date | Change |
|------|--------|
| 2026-06-01 | Initial canonical roster from iOS `CreatureCatalog.swift`; documented Android drift for Brachiosaurus steps and five rarity mismatches; Iguanodon marked code-wired / assets missing |
