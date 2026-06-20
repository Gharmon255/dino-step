# Google Play — Data Safety form (Dino Step)

Use these answers when completing **Play Console → App content → Data safety**. Adjust if behavior changes.

## Does your app collect or share any of the required user data types?

**Yes** — collects data types listed below (processed on-device for app functionality; optional server storage when signed in).

## Data types

| Type | Collected | Shared | Ephemeral | Required | Purpose |
|------|-----------|--------|-----------|----------|---------|
| **Health and fitness → Steps** | Yes | No | No | Optional (user grants Health Connect permission) | App functionality |
| **Personal info → Email address** | Yes (optional) | No | No | Optional (only if user signs in) | Account / backup / battles |
| **App info → Other user-generated content** | Yes (optional) | No | No | Optional (game save backup if signed in) | Account / backup |

### Steps — details

- **How collected:** Health Connect API — user taps **Sync again** on Home and/or about **once per hour** via Android background work (WorkManager) when permission is granted
- **Why:** Hatch eggs and grow dinosaurs based on step count
- **Uploaded to servers:** **No** — steps stay on device; only derived game progress may be backed up if user signs in
- **Users can request deletion:** Uninstall app or clear app data; revoke Health Connect permission

### Notifications

- **Local notifications only** (stage milestones, daily step-goal reminders) — not declared as a separate Data Safety collection type; content generated on-device, not sent to our servers
- User can disable notifications in system settings

## Data NOT collected (unless optional sign-in enabled)

- Location
- Financial info
- Photos / videos
- Audio
- Contacts
- Messages
- App activity for advertising
- Device or other IDs for tracking

Without cloud sign-in: no email or server-stored game data.

**Optional PvP (signed in):** battle history (species ids, outcomes, timestamps) stored in Supabase when user uses Battle tab. No step data uploaded for battles.

**Sign-in in production builds:** `ACCOUNT_SIGN_IN_ENABLED = true` in `AccountBackupCard.kt`. Cloud features require `supabase.properties` at build time; without it, sign-in UI shows as unavailable but local play works.

## Security practices

- Health/step data processed on-device
- Cloud save encrypted in transit (HTTPS); stored on Supabase
- No account required for core gameplay

## Privacy policy URL

`https://gharmon255.github.io/dino-step/privacy-policy.html`

After updating `docs/privacy-policy.html`, push to `main` and confirm GitHub Pages serves the new text (see `docs/PRIVACY_POLICY_HOSTING.md`).

## Health apps declaration

Declare that the app uses Health Connect for **steps** to support fitness/game functionality. Align wording with `PermissionsRationaleActivity`, `HealthConnectCard`, and `docs/privacy-policy.html`.

## Support contact

`support@gharmon255.dev` (also in privacy policy)
