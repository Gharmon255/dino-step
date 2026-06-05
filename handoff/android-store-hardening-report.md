---
# Android Store Hardening Sprint Report

**Repo:** dino-step  
**Date:** 2026-06-01  
**Base commit:** `da515df` — Add Android Dino Dex completion dashboard  
**Agent scope:** Android store hardening only

## Executive summary

- Added **`LAUNCH_CHECKLIST.md`** at repo root — Google Play internal testing checklist mirroring iOS tone (icons, privacy, Data Safety, Health Connect, signing/AAB, store listing, smoke tests).
- **Release Stats UI** now matches iOS: **Current Run** + **Lifetime** only; all developer/diagnostic cards gated behind `DevTools.isEnabled`.
- **Health Connect copy** updated for user-facing clarity (why steps are read, manual sync only, no location/ads); removed release-visible “fake step buttons” hint from unavailable status.
- **`GameViewModel.addSteps()`** and **`forceWatchSync()`** now no-op when `!DevTools.isEnabled` (defense in depth).
- **`./gradlew assembleDebug`**, **`assembleRelease`**, and **`bundleRelease`** all **passed**; release builds use default debug signing until upload keystore is wired.

## Files changed

| File | Change |
|------|--------|
| `LAUNCH_CHECKLIST.md` | **New** — Play internal testing checklist (icons, privacy, Data Safety, HC, signing, AAB, store listing, smoke tests) |
| `README.md` | Added link to `LAUNCH_CHECKLIST.md` under Docs |
| `app/.../ui/stats/StatsScreen.kt` | Split user stats into Current Run + Lifetime; moved all dev cards/diagnostics behind `DevTools.isEnabled`; gated dev dialogs |
| `app/.../game/GameViewModel.kt` | Gated `addSteps()` and `forceWatchSync()` with `DevTools.isEnabled` |
| `app/.../health/HealthConnectUiStatus.kt` | iOS-aligned permission/ready/unavailable messages; removed fake-step reference |
| `app/.../ui/common/HealthConnectCard.kt` | Added on-device privacy line (no sell/share for ads) |
| `app/.../PermissionsRationaleActivity.kt` | Expanded rationale: manual sync, no location, no ad tracking |
| `handoff/android-store-hardening-report.md` | **New** — this report |

**Not modified (per scope):** gameplay, `CreatureCatalog.kt`, Wear sync, Health Connect sync logic, drawable assets, version codes, `.idea/`.

## Release vs DEBUG UI summary

| Feature | Release | DEBUG |
|---------|---------|-------|
| Home fake step buttons (+500/+2000/+10000) | Hidden | Visible |
| Stats — Current Run / Lifetime cards | Visible | Visible |
| Stats — Wear sync debug + Force Wear Sync | Hidden | Visible |
| Stats — Developer species override / Force species egg | Hidden | Visible |
| Stats — Give random egg by rarity | Hidden | Visible |
| Stats — Clear collection / Reset game | Hidden | Visible |
| Stats — Developer diagnostics (fake steps, HC debug) | Hidden | Visible |
| Health Connect card on Stats | Visible (user copy) | Visible |
| `GameViewModel.addSteps()` | No-op | Works |
| `GameViewModel.forceWatchSync()` | No-op | Works |
| Other dev VM methods (`giveRandomEggForTesting`, etc.) | No-op (pre-existing) | Works |

**Audit notes:** `HomeScreen.kt` already gated fake steps. `DeveloperTestingSections.kt` only called from `StatsScreen` inside `DevTools.isEnabled` block. Dev dialogs additionally wrapped with `DevTools.isEnabled`.

## Build results

- **assembleDebug:** **PASS** — `BUILD SUCCESSFUL` (Gradle 9.4.1 first-run download). Warning: experimental `android.disallowKotlinSourceSets=false`.
- **assembleRelease / bundleRelease:** **PASS** — `BUILD SUCCESSFUL` in ~51s. Warnings: unable to strip `libandroidx.graphics.path.so` (packaged as-is); same experimental Kotlin source sets flag; configuration cache not enabled (informational).
- **Signing note:** No `signingConfigs` in `app/build.gradle.kts`; `bundleRelease` signed with **default debug key** — suitable for local smoke test only, **not** for Play upload until upload keystore is configured.

## Launch readiness

- **Score:** ~**65%** toward Play internal testing (code/docs ready; store ops and assets remain)
- **Done:**
  - Release vs DEBUG UI audit and fixes
  - Play-focused `LAUNCH_CHECKLIST.md`
  - Health Connect user-facing copy alignment
  - README pointer to launch checklist
  - Clean debug + release Gradle builds
- **Still blocked:**
  - Branded launcher / adaptive / 512×512 Play icon (still default `ic_launcher`)
  - Privacy policy URL (hosted externally)
  - Play Console Data Safety + Health apps declaration (manual)
  - Upload keystore + `signingConfigs.release` wiring
  - Store listing copy, screenshots, feature graphic
  - Physical device + paired Wear release smoke test on coordinator’s hardware

## Health Connect copy changes

- **`HealthConnectUiStatus.Unavailable`:** Install Health Connect from Play Store — no longer mentions fake step buttons.
- **`PermissionRequired`:** Explains step read for hatch/growth, manual Sync Steps only, no location/ads.
- **`Ready`:** Manual sync reminder for today’s walking steps.
- **`HealthConnectCard`:** Added “Step data stays on your device… not sell or share for ads.”
- **`PermissionsRationaleActivity`:** Matches permission screen with manual sync + no location/ad tracking.
- **Permissions/sync implementation:** unchanged.

## Signing / AAB status

**Current `app/build.gradle.kts` state:**

| Item | Status |
|------|--------|
| `versionCode` / `versionName` | `1` / `"1.0"` |
| `isMinifyEnabled` (release) | `false` |
| `signingConfigs` | **Missing** |
| ProGuard files | Referenced but inactive (minify off) |

**User must do at home:**

1. Generate upload keystore with `keytool` (see `LAUNCH_CHECKLIST.md`) — never commit `.jks` or passwords.
2. Add gitignored `keystore.properties` and wire `signingConfigs.create("release")` in `app/build.gradle.kts`.
3. Enable Play App Signing on first Console upload.
4. Run `./gradlew bundleRelease` → upload `app/build/outputs/bundle/release/app-release.aab` to Internal testing.
5. Bump `versionCode` for each subsequent upload.

**ProGuard/minify:** Not enabled; no changes made. Document-only — enable only after dedicated release testing if desired.

## App icon & Play asset status

- **Phone:** `@mipmap/ic_launcher` / `ic_launcher_round` — default Android Studio adaptive icon (`ic_launcher_foreground` + `ic_launcher_background` vectors).
- **Wear:** `@drawable/ic_launcher` — default vector.
- **Required deliverables (not yet branded):** adaptive foreground/background, 512×512 Play Store icon, optional 1024×500 feature graphic.
- **Source:** Branded PNGs may come from sibling repo **`dino-step-assets`** when ready.

## Suggested commit message

```
Add Play launch checklist and gate release Stats UI for store hardening
```

## Next steps for coordinator

1. Review sprint diff (exclude `.idea/` from commit).
2. Commit when ready using suggested message above.
3. Produce branded icons from `dino-step-assets` and replace default `ic_launcher` assets.
4. Publish privacy policy URL; complete Play Console Data Safety form (steps/fitness, not sold, not shared for ads).
5. Create upload keystore, add `keystore.properties`, wire `signingConfigs.release`, rebuild signed `bundleRelease`.
6. Upload AAB to Internal testing; add tester emails.
7. Run release smoke test checklist in `LAUNCH_CHECKLIST.md` on physical phone (+ paired Wear if available).
8. Prepare store listing: short/full description, screenshots, feature graphic.

---
END OF HANDOFF — dino-step
