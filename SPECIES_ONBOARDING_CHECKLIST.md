# Species onboarding checklist (Android + Wear OS)

Use this when adding a **new asset-backed species** to Dino Step. Cross-platform catalog lives in `CreatureCatalog`; art naming is shared via `CreatureAssetNames` / `DrawableCreatureResolver`.

**Canonical roster (ids, rarities, steps, asset status, Android drift):** see [`SPECIES_ROSTER.md`](SPECIES_ROSTER.md).  
Do not duplicate roster tables here — update `SPECIES_ROSTER.md` when design changes.

---

## 1. Art preparation

- [ ] Source PNGs per stage: **baby**, **juvenile**, **adult**
- [ ] Format: **RGBA**, **1024×1024**
- [ ] Alpha channel 0–255; **clear hidden RGB** where alpha = 0 (no fringe)
- [ ] Visual style matches existing dino set (lighting, padding, no extra borders)

---

## 2. Drawable files (phone + watch)

Copy the same three files into **both** modules:

| Module | Path |
|--------|------|
| Phone | `app/src/main/res/drawable-nodpi/` |
| Wear | `wear/src/main/res/drawable-nodpi/` |

**Exact file names** (`{speciesId}` = catalog slug, lowercase, underscores):

```
dino_{speciesId}_baby.png
dino_{speciesId}_juvenile.png
dino_{speciesId}_adult.png
```

Example for T-Rex: `dino_trex_baby.png`, `dino_trex_juvenile.png`, `dino_trex_adult.png`.

Do **not** rename to display names (e.g. avoid `dino_t_rex_*` unless catalog id is `t_rex`).

---

## 3. Catalog & asset registry

- [ ] Add or confirm `CreatureDefinition` in `app/.../model/CreatureCatalog.kt` matches [`SPECIES_ROSTER.md`](SPECIES_ROSTER.md) (stable `id` matching filenames)
- [ ] Add species id to `CreatureAssetNames.assetBackedSpeciesIds` in `shared/.../visual/CreatureAssetNames.kt`
- [ ] Confirm `CreatureCatalog.isAssetBacked(id)` returns true
- [ ] Confirm `CreatureAssetNames.assetSlugForSpeciesArt(id)` returns the slug used in drawable names
- [ ] If replacing a legacy alias (e.g. `t_rex` → `trex`), keep `legacyCreatureIdAliases` in sync — do not duplicate art for legendaries

---

## 4. Developer testing (DEBUG only)

- [ ] Add enum entry to `NextEggTestSpecies` if the species should be force-spawnable in Stats → Developer Testing
- [ ] **Do not** add to `NextEggTestSpecies` until assets exist (see Dilophosaurus)
- [ ] Stats → **Force Selected Species Egg** → grow → claim → verify collection

---

## 5. UI verification

### Collection (phone)

- [ ] Species appears in Collection roster (locked until completed)
- [ ] **Locked**: 🔒 avatar, “Undiscovered”, “Locked” chip — **no** `dino_{id}_*` PNG
- [ ] **Discovered**: adult drawable `dino_{speciesId}_adult` or `dino_placeholder_adult` — **never** another species’ PNG
- [ ] Default sort: discovered first, then catalog order

### Active creature (phone)

- [ ] Egg uses `egg_{rarity}` drawable
- [ ] Baby / juvenile / adult use `DrawableCreatureResolver` for this `speciesId` only

### Wear OS

- [ ] Phone publishes `creature_id` in Wear payload (`WEAR_SYNC_CONTRACT.md`)
- [ ] Watch shows same resolver rules (PNG or placeholder/emoji)
- [ ] No crash on missing optional payload fields

---

## 6. Emulator test checklist

No physical device required for basic validation:

```bash
./gradlew assembleDebug
```

- [ ] Install phone + Wear debug APKs on emulators; pair if testing sync
- [ ] DEBUG: force species egg → fake steps → hatch/grow → claim
- [ ] Collection: discovered row shows correct adult art or placeholder
- [ ] Locked rows unchanged for other species
- [ ] `./gradlew assembleDebug` succeeds

**Not verified on physical hardware** unless you explicitly test on a real phone/watch.

---

## 7. Commit message example

```
Add asset-backed art and catalog entry for {species display name}

Add dino_{id}_{baby,juvenile,adult} PNGs to app and wear drawable-nodpi,
register {id} in CreatureAssetNames, and enable DEBUG species override.
```

---

## Related docs

- `SPECIES_ROSTER.md` — canonical species ids, rarities, steps, asset status
- `WEAR_SYNC_CONTRACT.md` — phone ↔ watch payload and `creature_id`
- `shared/.../visual/CreatureAssetNames.kt` — naming helpers
- `shared/.../visual/DrawableCreatureResolver.kt` — safe fallback rules
