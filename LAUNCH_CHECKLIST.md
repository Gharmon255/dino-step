# Dino Step — Android / Wear OS Launch Checklist

Pre–Google Play **internal testing** readiness for **dino-step**.  
Update checkboxes as items are completed.

---

## App icons & Play assets

- [x] **Branded launcher icon** — `ic_launcher` / `ic_launcher_round` use PNG layers in `app/src/main/res/drawable-nodpi/`
- [x] **Adaptive icon** — foreground + background PNGs from `dino-step-assets/icons/` (verify masks on device before store upload)
- [ ] **Play Store 512×512** high-res icon uploaded in Play Console (source: `dino-step-assets/icons/app_icon_512.png` — not bundled in APK)
- [ ] **Feature graphic** (optional but recommended) — 1024×500 for store listing
- [x] **Wear OS icon** — `wear/src/main/res/drawable-nodpi/ic_launcher.png` (from `app_icon_512.png`)
- [x] Source art imported from sibling repo **`dino-step-assets`**

**Current status:** Phone adaptive and Wear launcher icons are **branded**. Play Console still needs manual 512×512 upload and on-device mask verification.

---

## Privacy & legal

- [x] **Privacy policy text** — `docs/privacy-policy.html` (hosting steps: `docs/PRIVACY_POLICY_HOSTING.md`)
- [x] **In-app privacy link** — Stats tab + Health Connect rationale screen (`privacy_policy_url` in `strings.xml`)
- [x] **Data Safety draft answers** — `docs/DATA_SAFETY_PLAY_CONSOLE.md`
- [ ] **Privacy policy URL** published on a public HTTPS page (required for Play Console)
- [ ] Privacy policy linked in Play Console **App content → Privacy policy**
- [ ] **Play Console Data Safety** form completed and matches in-app behavior:
  - Collects: **Steps / fitness** (via Health Connect, user-initiated sync only)
  - Not sold to third parties
  - Not used for advertising or tracking
  - Data processed on-device; no account login required
- [ ] In-app disclosure aligns with policy (Stats Health Connect card, permission rationale screen)
- [ ] Review Data Safety answers whenever permissions or data flows change

---

## Health Connect

- [ ] **Health Connect permissions** declared in manifest (`android.permission.health.READ_STEPS`)
- [ ] **Permissions rationale** (`PermissionsRationaleActivity`) reviewed — reads steps for hatch/growth, manual sync only, no location/ads
- [ ] **Play Console Health apps declaration** completed if required for your target audience / region
- [ ] **Physical device test** — install Health Connect, grant permission, tap **Sync Steps** on Home, verify egg/dino progression
- [ ] Confirm app does **not** auto-sync in background (by design — manual sync only)

---

## Release signing & AAB build

### Current Gradle state (`app/build.gradle.kts`)

| Setting | Value |
|---------|--------|
| `versionCode` | `1` |
| `versionName` | `"1.0"` |
| Release `isMinifyEnabled` | `false` |
| `signingConfigs` | **Wired** in `:app` and `:wear` `build.gradle.kts` when `keystore.properties` exists |
| ProGuard rules file | Present but unused while minify is off |

### Create upload keystore (one-time, local machine)

**Never commit** `.jks`, `.keystore`, passwords, or `keystore.properties` to git.

```bash
keytool -genkey -v \
  -keystore ~/upload-keystores/dino-step-upload.jks \
  -alias dino-step-upload \
  -keyalg RSA -keysize 2048 -validity 10000
```

Store the keystore path and passwords in a password manager. Back up the keystore file securely — loss blocks future updates.

### Wire signing in Gradle

**Gradle wiring is done.** When `keystore.properties` exists, `:app` and `:wear` release builds use the upload keystore.

1. Create **`keystore.properties`** at project root (gitignored), or run **`./scripts/generate-upload-keystore.sh`**:

   ```properties
   storeFile=/absolute/path/to/dino-step-upload.jks
   storePassword=***
   keyAlias=dino-step-upload
   keyPassword=***
   ```

2. Add to **`app/build.gradle.kts`** (example pattern):

   ```kotlin
   val keystorePropertiesFile = rootProject.file("keystore.properties")
   val keystoreProperties = Properties()
   if (keystorePropertiesFile.exists()) {
       keystoreProperties.load(FileInputStream(keystorePropertiesFile))
   }

   signingConfigs {
       create("release") {
           keyAlias = keystoreProperties["keyAlias"] as String
           keyPassword = keystoreProperties["keyPassword"] as String
           storeFile = file(keystoreProperties["storeFile"] as String)
           storePassword = keystoreProperties["storePassword"] as String
       }
   }

   buildTypes {
       release {
           signingConfig = signingConfigs.getByName("release")
           // isMinifyEnabled stays false unless you add rules and test thoroughly
       }
   }
   ```

3. Enable **Play App Signing** in Play Console on first upload (Google holds the app signing key; you use the upload key).

### Build release AAB

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"  # if needed
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

- [ ] Upload keystore created and backed up (`./scripts/generate-upload-keystore.sh`)
- [x] `signingConfigs.release` wired in Gradle (`app` + `wear` `build.gradle.kts`)
- [ ] `keystore.properties` filled with real passwords (gitignored)
- [ ] `./gradlew bundleRelease` produces signed AAB
- [ ] Upload AAB to Play Console **Internal testing** track
- [ ] Add internal testers (email list or Google Group)

### Play upload (phone + Wear, same `applicationId`)

Phone and Wear share **`com.gharmon255.dinostep`**. In each release:

1. Upload **`app-release.aab`** to the main **App bundles** row (phones/tablets).
2. Expand **Advanced settings** → **Wear OS** and upload **`wear-release.aab`** there only.
3. Do **not** put the Wear AAB in the phone slot — Play may treat the app as watch-only and phones show **“Your device isn’t compatible.”**
4. Do **not** add `android.hardware.type.watch` to the phone manifest — Play rejects phone bundles that declare it (even with `required="false"`).
5. Keep Wear `versionCode` **≥** phone `versionCode` (phone **11**, wear **12**).

---

## Release build quality

- [ ] **Release configuration** hides DEBUG-only UI (`DevTools.isEnabled` = `BuildConfig.DEBUG`):
  - Home fake step buttons
  - Stats developer testing cards (species override, rarity eggs, reset/clear)
  - Wear sync debug panel + Force Wear Sync
  - Developer diagnostics (fake steps, HC debug rows)
- [ ] Stats tab in Release shows **Current Run** + **Lifetime** only (mirrors iOS)
- [ ] `GameViewModel` dev methods no-op when `!DevTools.isEnabled`
- [ ] No debug-only strings visible in release (e.g. "fake steps", "testing only")
- [ ] `./gradlew assembleRelease` succeeds
- [ ] Install release APK/AAB on test device and spot-check UI

---

## Wear OS companion

- [ ] Wear app included in same Play listing (or separate if you split later)
- [ ] **Paired Wear test** — phone progression updates watch face (egg/dino art, progress ring, milestone text)
- [ ] Watch receives state after phone app opened at least once
- [ ] Release build on physical watch (if available) — no dev panels visible

---

## Store listing (Play Console)

- [ ] App name: **Stepasaurus**
- [ ] Short description (80 chars)
- [ ] Full description (features: walk to hatch, collection, rarity tiers, Wear companion)
- [ ] **Screenshots** — phone (required), 7-inch / 10-inch tablet if targeting tablets
- [ ] **Feature graphic** 1024×500 (optional)
- [ ] Category, content rating questionnaire
- [ ] Contact email / support URL
- [ ] Privacy policy URL

---

## Pre-upload checklist

- [ ] Branded icons and Play 512×512 ready (or accept placeholder for internal-only smoke test)
- [ ] Privacy policy URL live
- [ ] Data Safety form submitted
- [ ] Health Connect declaration / policy compliance reviewed
- [ ] Signed `app-release.aab` built locally
- [ ] `versionCode` bumped for each new upload (when publishing updates)
- [ ] Release smoke test passed (below)
- [ ] Internal testing track configured with tester emails

---

## QA smoke test (Release build)

Install release build on a physical phone (and paired Wear if available):

- [ ] Fresh install → mystery egg → grant Health Connect → **Sync Steps** → progression through stages
- [ ] Claim reward → new egg → collection updates
- [ ] Collection sort/filter and adult PNGs for asset-backed species
- [ ] No crash when assets missing (emoji / placeholder fallback)
- [ ] Stats shows Current Run + Lifetime only — **no** developer sections
- [ ] Home has **no** fake step buttons
- [ ] Wear receives updated state after iPhone-equivalent phone changes (if watch paired)

---

## Known deferred (not blocking internal testing code merge)

- Branded app icon / Play marketing assets (may import from `dino-step-assets`)
- Release signing Gradle wiring (user-local keystore)
- Onboarding flow
- Automatic background Health Connect sync
- ProGuard / R8 minification (document only; `isMinifyEnabled = false` today)

---

## Changelog

| Date | Note |
|------|------|
| 2026-06-01 | Initial Android launch checklist; store-hardening sprint gates release Stats UI, improves Health Connect copy |
