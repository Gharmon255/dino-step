# Supabase setup (cloud save)

Cloud backup uses [Supabase](https://supabase.com) for auth (Google on Android; Apple + Google on iOS) and a `game_saves` table. Gameplay stays **local-first**; upload is optional after sign-in.

## 1. Create project

1. [supabase.com](https://supabase.com) → New project
2. Note **Project URL** and **anon public** key (Settings → API)
3. Use the base URL only — **no** `/rest/v1/` suffix in app config

## 2. Run migration

Paste [`supabase/migrations/001_game_saves.sql`](../supabase/migrations/001_game_saves.sql) into **SQL Editor** → Run.

The migration creates `profiles` + `game_saves`, RLS policies, and **GRANT** statements for the Data API. If sign-in works but `fetchGameSave` returns **403**, re-run the `GRANT` block at the bottom of that file.

## 3. Enable auth providers

**Authentication → Providers**

### Google (Android + iOS)

1. [Google Cloud Console](https://console.cloud.google.com/) → project **Stepasaurus** (or your app project)
2. **OAuth consent screen** configured; add test users while in testing mode if needed
3. Create credentials:
   - **Web client** — Client ID + secret → paste into Supabase Google provider
   - **Android client** — package `com.gharmon255.dinostep` + SHA-1 of your signing key (debug + release)
4. Supabase redirect URL (auto): `https://<project-ref>.supabase.co/auth/v1/callback`

### Apple (iOS only)

1. [Apple Developer](https://developer.apple.com/) → **Sign in with Apple** key (`.p8`), Key ID, Team ID
2. **Services ID** (e.g. `com.gharmon255.Dino-Step.auth`) with return URL:
   `https://<project-ref>.supabase.co/auth/v1/callback`
3. Supabase **Apple** provider:
   - **Client IDs:** iOS bundle ID + Services ID (comma-separated if both required)
   - **Secret:** a **JWT** signed with the `.p8` key — **not** the raw `.p8` file. Supabase expects `sub` = Services ID, `iss` = Team ID, `kid` = Key ID
4. **Authentication → URL configuration** → add redirect allow list:
   - `stepasaurus://auth-callback` (iOS custom scheme; matches `SupabaseAuth.plist`)

## 4. Android local config

Copy `supabase.properties.example` → `supabase.properties` (gitignored):

```properties
SUPABASE_URL=https://xxxx.supabase.co
SUPABASE_ANON_KEY=eyJ...
GOOGLE_WEB_CLIENT_ID=xxxx.apps.googleusercontent.com
```

Rebuild the app. Without this file, cloud backup UI shows as unavailable but local play works.

## 5. iOS local config

Copy `Dino Step/Config/SupabaseConfig.example.plist` → `SupabaseConfig.plist` (gitignored):

- `SUPABASE_URL`, `SUPABASE_ANON_KEY`
- `GOOGLE_OAUTH_REDIRECT` = `stepasaurus://auth-callback`

Enable **Sign in with Apple** on the iPhone target (`Dino Step.entitlements`). URL scheme `stepasaurus` is merged from `Config/SupabaseAuth.plist`.

## 6. Rollout / tester builds

Sign-in buttons are hidden behind **Coming soon** in release-facing builds:

| Platform | Flag | File |
|----------|------|------|
| Android | `ACCOUNT_SIGN_IN_ENABLED = false` | `app/.../AccountBackupCard.kt` |
| iOS | `CloudBackupFeatures.signInEnabled = false` | `Dino Step/Views/AccountBackupCard.swift` |

Set the flag to `true` **locally** to test OAuth. **Export local save** remains available regardless.

## 7. Verify

- Sign in on device A → play → check `game_saves` row in Supabase Table Editor
- Sign in on device B (same account) → conflict or restore flow
- Confirm steps / Health data do **not** appear in `save_json`

See also [`CLOUD_SAVE_CONTRACT.md`](CLOUD_SAVE_CONTRACT.md) for payload schema and conflict rules.
