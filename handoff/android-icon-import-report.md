# Android Icon Import Report

**Repo:** dino-step  
**Date:** 2026-06-01  
**Scope:** Replace default launcher icons with branded assets from `dino-step-assets` (read-only source)

## Executive summary

- Imported branded **adaptive icon** layers for phone `:app` from `dino-step-assets/icons/adaptive_foreground_1024.png` and `adaptive_background_1024.png`.
- Removed default vector `ic_launcher_foreground.xml` / `ic_launcher_background.xml`; `mipmap-anydpi/ic_launcher.xml` and `ic_launcher_round.xml` unchanged (still reference `@drawable/ic_launcher_foreground` / `@drawable/ic_launcher_background`, now resolved as PNGs).
- Replaced Wear default vector `ic_launcher.xml` with `app_icon_512.png` as `wear/.../drawable-nodpi/ic_launcher.png`.
- **`LAUNCH_CHECKLIST.md`** updated — branded launcher, adaptive icon, and Wear icon checked off; Play 512 upload remains manual.
- **All requested builds passed.**

## Files changed

| File | Change |
|------|--------|
| `app/src/main/res/drawable-nodpi/ic_launcher_foreground.png` | **Added** — from `adaptive_foreground_1024.png` (1024×1024 RGBA) |
| `app/src/main/res/drawable-nodpi/ic_launcher_background.png` | **Added** — from `adaptive_background_1024.png` (1024×1024 RGB) |
| `app/src/main/res/drawable/ic_launcher_foreground.xml` | **Deleted** — replaced by PNG |
| `app/src/main/res/drawable/ic_launcher_background.xml` | **Deleted** — replaced by PNG |
| `wear/src/main/res/drawable-nodpi/ic_launcher.png` | **Added** — from `app_icon_512.png` (512×512 RGB) |
| `wear/src/main/res/drawable/ic_launcher.xml` | **Deleted** — replaced by PNG |
| `LAUNCH_CHECKLIST.md` | Checked off branded launcher / adaptive / Wear items; noted Play 512 source path |

**Unchanged:** `app/src/main/res/mipmap-anydpi/ic_launcher.xml`, `ic_launcher_round.xml` (already pointed at drawable layer names).

## Where foreground/background PNGs live

| Asset | Path in dino-step |
|-------|-------------------|
| Adaptive foreground | `app/src/main/res/drawable-nodpi/ic_launcher_foreground.png` |
| Adaptive background | `app/src/main/res/drawable-nodpi/ic_launcher_background.png` |
| Wear launcher | `wear/src/main/res/drawable-nodpi/ic_launcher.png` |

Source masters (do not edit): `/Users/gharmon/projects/dino-step-assets/icons/`

## Play Store 512 path for listing upload

Upload manually in Play Console — **not bundled in APK:**

`/Users/gharmon/projects/dino-step-assets/icons/app_icon_512.png`

## Build results

| Task | Result |
|------|--------|
| `:app:assembleDebug` | **PASS** |
| `:app:assembleRelease` | **PASS** |
| `:wear:assembleDebug` | **PASS** |

Notes: Experimental `android.disallowKotlinSourceSets=false` warning only; no icon/resource errors.

## Remaining blockers

- **Play Console:** Upload `app_icon_512.png` as store hi-res icon
- **On-device:** Verify adaptive icon masks (circle, squircle, rounded square) on API 26+ phone
- **Feature graphic** (1024×500) — optional, not created
- **Release signing** — upload keystore + `signingConfigs.release` still unwired
- **Privacy policy URL** and **Data Safety** form — manual Play Console work
- **Physical smoke test** on phone + paired Wear with new icons

## Suggested commit message

```
Import branded launcher icons from dino-step-assets for phone and Wear
```

---
END OF HANDOFF — dino-step
