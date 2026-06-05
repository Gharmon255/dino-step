# Google Play — Data Safety form (Dino Step)

Use these answers when completing **Play Console → App content → Data safety**. Adjust if behavior changes.

## Does your app collect or share any of the required user data types?

**Yes** — collects data types listed below (processed on-device for app functionality).

## Data types

| Type | Collected | Shared | Ephemeral | Required | Purpose |
|------|-----------|--------|-----------|----------|---------|
| **Health and fitness → Steps** | Yes | No | No | Optional (user grants Health Connect permission) | App functionality |

### Steps — details

- **How collected:** Health Connect API, user-initiated sync only (tap Sync Steps)
- **Why:** Hatch eggs and grow dinosaurs based on step count
- **Encrypted in transit:** N/A (local Health Connect read on device)
- **Users can request deletion:** Uninstall app or clear app data; revoke Health Connect permission

## Data NOT collected

- Location
- Personal info (name, email, address) via the app
- Financial info
- Photos / videos
- Audio
- Contacts
- Messages
- App activity for advertising
- Device or other IDs for tracking

## Security practices

- Data is processed on-device
- No account login required for core gameplay

## Privacy policy URL

Use the hosted URL from `docs/PRIVACY_POLICY_HOSTING.md` (default: `https://gharmon255.github.io/dino-step/privacy-policy.html`).

## Health apps declaration

Declare that the app uses Health Connect for **steps** to support fitness/game functionality. Align wording with `PermissionsRationaleActivity` and `HealthConnectCard` in the app.
