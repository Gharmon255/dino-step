# Dino Step — Canonical Species Roster

**Source of truth** for cross-platform catalog alignment (iOS and Android).  
When code disagrees with this document, **treat this file as intended design** and update the diverging platform in a dedicated catalog-alignment sprint.

**Canonical source:** `dino-step-ios` — `Dino Step/Game/CreatureCatalog.swift` (June 2026).  
**Companion docs:** `SPECIES_ONBOARDING_CHECKLIST.md`, `WATCH_SYNC_CONTRACT.md` (iOS) / `WEAR_SYNC_CONTRACT.md` (Android).  
**Asset source of truth:** `dino-step-assets/dinos/` — import per `dino-step-assets/ADD_SPECIES_CHECKLIST.md` and `dino-step-assets/species_queue.md`.

---

## Roster summary (June 2026)

| Count | Description |
|-------|-------------|
| **29** | Catalog species on iOS and Android (`CreatureCatalog`) |
| **29** | Asset-backed species (`assetBackedSpeciesIds` on both platforms) |
| **0** | Non-asset-backed species (emoji / placeholder art only) |

All **29** asset-backed species have baby, juvenile, and adult PNGs in `dino-step-assets/dinos/`, iOS phone `Assets.xcassets`, iOS watch `Assets.xcassets`, Android phone `drawable-nodpi/`, and Android Wear `drawable-nodpi/`.

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
| **asset status** | Whether PNGs exist in app bundles and `dino-step-assets` today |

**Growth stages:** EGG (0 … hatch−1) → BABY (hatch … juvenile−1) → JUVENILE (juvenile … total−1) → ADULT (total+).

**Egg rarity vs creature rarity:** Mystery eggs roll an **egg rarity**; the species inside has its own **creature rarity** (below). Random species for an egg use the creature’s roster rarity tier.

---

## Asset-backed species (29)

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
| `iguanodon` | Iguanodon | Uncommon | Forest | 3,800 | 5,700 | 9,500 | 19,000 | true | `dino_iguanodon` | **shipped** |
| `carnotaurus` | Carnotaurus | Uncommon | Volcano | 4,800 | 7,200 | 12,000 | 24,000 | true | `dino_carnotaurus` | **shipped** |
| `trex` | T-Rex | Rare | Volcano | 10,000 | 15,000 | 25,000 | 50,000 | true | `dino_trex` | **shipped** |
| `spinosaurus` | Spinosaurus | Rare | Swamp | 12,000 | 18,000 | 30,000 | 60,000 | true | `dino_spinosaurus` | **shipped** |
| `allosaurus` | Allosaurus | Rare | Rocky | 9,600 | 14,400 | 24,000 | 48,000 | true | `dino_allosaurus` | **shipped** |
| `mosasaurus` | Mosasaurus | Rare | Ocean | 13,000 | 19,500 | 32,500 | 65,000 | true | `dino_mosasaurus` | **shipped** |
| `gallimimus` | Gallimimus | Common | Plains | 1,800 | 2,700 | 4,500 | 9,000 | true | `dino_gallimimus` | **shipped** |
| `baryonyx` | Baryonyx | Uncommon | Swamp | 5,000 | 7,500 | 12,500 | 25,000 | true | `dino_baryonyx` | **shipped** |
| `velociraptor_alpha` | Velociraptor Alpha | Rare | Jungle | 9,000 | 13,500 | 22,500 | 45,000 | true | `dino_velociraptor_alpha` | **shipped** |
| `therizinosaurus` | Therizinosaurus | Rare | Forest | 11,000 | 16,500 | 27,500 | 55,000 | true | `dino_therizinosaurus` | **shipped** |
| `giganotosaurus` | Giganotosaurus | Epic | Plains | 17,000 | 25,500 | 42,500 | 85,000 | true | `dino_giganotosaurus` | **shipped** |
| `quetzalcoatlus` | Quetzalcoatlus | Epic | Mountain | 18,000 | 27,000 | 45,000 | 90,000 | true | `dino_quetzalcoatlus` | **shipped** |
| `indominus_hybrid` | Indominus Rex Style Hybrid | Epic | Lab | 19,000 | 28,500 | 47,500 | 95,000 | true | `dino_indominus_hybrid` | **shipped** |
| `ancient_spinosaurus` | Ancient Spinosaurus | Epic | Swamp | 20,000 | 30,000 | 50,000 | 100,000 | true | `dino_ancient_spinosaurus` | **shipped** |
| `frost_raptor` | Frost Raptor | Legendary | Ice | 22,000 | 33,000 | 55,000 | 110,000 | true | `dino_frost_raptor` | **shipped** |
| `volcanic_t_rex` | Volcanic T-Rex | Legendary | Volcano | 25,000 | 37,500 | 62,500 | 125,000 | true | `dino_volcanic_t_rex` | **shipped** |
| `shadow_triceratops` | Shadow Triceratops | Legendary | Dark | 26,000 | 39,000 | 65,000 | 130,000 | true | `dino_shadow_triceratops` | **shipped** |
| `titanosaur` | Titanosaur | Legendary | Plains | 30,000 | 45,000 | 75,000 | 150,000 | true | `dino_titanosaur` | **shipped** |
| `cosmic_pterodactyl` | Cosmic Pterodactyl | Legendary | Sky | 35,000 | 52,500 | 87,500 | 175,000 | true | `dino_cosmic_pterodactyl` | **shipped** |
| `ancient_apex_rex` | Ancient Apex Rex | Legendary | Volcano | 40,000 | 60,000 | 100,000 | 200,000 | true | `dino_ancient_apex_rex` | **shipped** |

**Stage file names:** `dino_{speciesId}_baby`, `dino_{speciesId}_juvenile`, `dino_{speciesId}_adult` (PNG in `dino-step-assets/dinos/`; iOS imagesets; Android `drawable-nodpi` on phone and Wear).

**Iguanodon:** **Shipped everywhere** — `dino-step-assets/dinos/`, iOS phone and watch `Assets.xcassets`, Android phone and Wear `drawable-nodpi/` (`dino_iguanodon_{baby,juvenile,adult}`).

---

## Non-asset-backed species (0)

None — full catalog is asset-backed.

**iOS save UUIDs:** Creatures use stable UUIDs per catalog index in `CreatureCatalog.swift`; do not reassign UUIDs when editing roster entries.

---

## Cross-platform catalog alignment

**Status (June 2026):** iOS and Android catalogs match this roster. **No open catalog drift.**

### Resolved drift (historical)

Android `CreatureCatalog.kt` was updated to match iOS. These rows are **resolved** — do not treat Android as still diverging:

| species id | field | canonical (both platforms now) | was (Android, pre-alignment) |
|------------|-------|--------------------------------|------------------------------|
| `brachiosaurus` | steps to hatch | 4,000 | 4,800 |
| `brachiosaurus` | steps baby → juvenile | 6,000 | 7,200 |
| `brachiosaurus` | steps juvenile → adult | 10,000 | 12,000 |
| `brachiosaurus` | total steps | 20,000 | 24,000 |
| `pachycephalosaurus` | rarity | Common | Uncommon |
| `carnotaurus` | rarity | Uncommon | Rare |
| `allosaurus` | rarity | Rare | Epic |
| `mosasaurus` | rarity | Rare | Legendary |

---

## Legacy IDs and aliases

| Alias | maps to | used for |
|-------|---------|----------|
| `t_rex` | `trex` | Older saves / Android `legacyCreatureIdAliases` |
| `pterodactyl` | `pteranodon` | Older saves; display name “Pterodactyl” |
| `Pterodactyl` (display) | `pteranodon` | iOS `legacySpeciesNameAliases` |
| `tyrannosaurus`, `tyrannosaurus_rex` | `trex` | Dev picker / asset resolver (iOS) |
| `indominus_rex_style_hybrid` | `indominus_hybrid` | Older iOS slug / cross-platform saves; dev picker / slug lookup |

**Do not** map `volcanic_t_rex` or `ancient_apex_rex` to `trex` for artwork. Do not map variant legendaries to base species art (see `dino-step-assets/species_queue.md`).

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

Uncommon eggs include: Stegosaurus, Pteranodon, Brachiosaurus, Dilophosaurus, Iguanodon, Carnotaurus, Baryonyx — not Pachycephalosaurus (Common).

---

## Changelog

| Date | Change |
|------|--------|
| 2026-06-06 | **29 / 29 asset-backed** — final four legendaries shipped; legacy `trex`, `triceratops`, `tiny_raptor` PNGs normalized to 1024×1024 |
| 2026-06-02 | Docs: roster summary (29 / 20 / 9); cross-platform drift marked **resolved**; Iguanodon and five new species noted shipped on all asset targets |
| 2026-06-02 | Gallimimus, Baryonyx, Velociraptor Alpha, Therizinosaurus, Giganotosaurus asset-backed and **shipped** (`dino-step-assets`, iOS, Android phone, Wear) |
| 2026-06-02 | Android `CreatureCatalog.kt` aligned with iOS (Brachiosaurus steps; Pachycephalosaurus, Carnotaurus, Allosaurus, Mosasaurus rarities) |
| 2026-06-02 | Iguanodon PNGs **shipped** on `dino-step-assets`, iOS phone/watch, Android phone/Wear |
| 2026-06-02 | Indominus Rex Style Hybrid canonical id `indominus_hybrid` on iOS (legacy slug `indominus_rex_style_hybrid`) |
| 2026-06-01 | Initial canonical roster from iOS `CreatureCatalog.swift`; documented Android drift (later resolved 2026-06-02) |
