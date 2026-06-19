# Supabase setup (cloud save)

## 1. Create project

1. [supabase.com](https://supabase.com) → New project
2. Note **Project URL** and **anon public** key (Settings → API)

## 2. Run migration

Paste [`supabase/migrations/001_game_saves.sql`](../supabase/migrations/001_game_saves.sql) into **SQL Editor** → Run.

## 3. Enable auth providers

**Authentication → Providers**

- **Google**: enable; add Web client ID + secret from Google Cloud Console
- **Apple**: enable; add Services ID, team ID, key ID, private key (.p8)

Redirect URL (both): `https://<project-ref>.supabase.co/auth/v1/callback`

## 4. Local app config

Copy `supabase.properties.example` → `supabase.properties` (gitignored):

```properties
SUPABASE_URL=https://xxxx.supabase.co
SUPABASE_ANON_KEY=eyJ...
GOOGLE_WEB_CLIENT_ID=xxxx.apps.googleusercontent.com
```

Rebuild the app. Without this file, cloud backup UI shows as unavailable but local play works.

## 5. iOS

Edit `Dino Step/Config/SupabaseConfig.plist` (copy from `SupabaseConfig.example.plist`):

- `SUPABASE_URL`, `SUPABASE_ANON_KEY`
- `GOOGLE_OAUTH_REDIRECT` = `stepasaurus://auth-callback` (must match Supabase redirect allow list)

Enable **Sign in with Apple** on the iPhone target (entitlement included). URL scheme `stepasaurus` is merged from `Config/SupabaseAuth.plist`.

## 6. Verify

- Sign in on device A → play → check `game_saves` row in Supabase Table Editor
- Sign in on device B (same account) → conflict or restore flow
