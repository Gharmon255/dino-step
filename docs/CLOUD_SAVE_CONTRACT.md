# Cloud save contract (Stepasaurus)

Cross-platform game backup format. **Schema version 2** (v1 still readable with defaults).

## Principles

- **Local-first**: device save is always written first; cloud is optional backup.
- **Species IDs**: use stable `speciesId` strings (`tiny_raptor`, etc.), not iOS UUIDs.
- **No health data**: step history stays on-device (Health Connect / HealthKit). Only game progress syncs.
- **Revision**: monotonic `revision` per device save; server stores latest `updated_at`.
- **Roster EX**: completed adults earn passive EX from step drip (see PvP); stored per creature in v2.

## `CloudGameSave` JSON (v2)

```json
{
  "schemaVersion": 2,
  "revision": 42,
  "updatedAt": "2026-06-18T12:00:00.000Z",
  "activeCreature": {
    "speciesId": "compsognathus",
    "eggRarity": "COMMON",
    "steps": 8998,
    "isRevealed": true,
    "nickname": "Spike",
    "startedAt": "2026-06-01T10:00:00.000Z",
    "hatchStep": 5000,
    "juvenileStep": 15000,
    "totalStepsRequired": 40000,
    "economyVersion": 2
  },
  "completedCreatures": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "speciesId": "trex",
      "stepsCompleted": 50000,
      "completedAt": "2026-05-20T18:30:00.000Z",
      "nickname": "Chomper",
      "eggRarityAtHatch": "RARE",
      "exSteps": 1200,
      "exLevel": 3
    }
  ],
  "playerStats": {
    "eggsHatched": 3,
    "creaturesCompleted": 2,
    "lastSyncedStepTotal": 4200,
    "lastSyncDayStartMillis": 1718668800000,
    "lifetimeStepsApplied": 125000
  },
  "lastRewardedEggRarity": "RARE",
  "lastRewardRollPercent": 12.5
}
```

### Excluded from cloud

- Onboarding / what's-new flags (`AppExperiencePreferences` / `AppExperienceStore`)
- Debug-only stats (`totalFakeStepsAdded`)
- Health sync messages and authorization state

## Postgres row (`game_saves`)

| Column | Type | Notes |
|--------|------|-------|
| `user_id` | uuid PK | `auth.users.id` |
| `schema_version` | int | Copy of `save_json.schemaVersion` |
| `revision` | bigint | Copy for quick conflict checks |
| `save_json` | jsonb | Full `CloudGameSave` |
| `updated_at` | timestamptz | Server-side on upsert |

## Conflict rules

| Situation | Action |
|-----------|--------|
| Sign in, cloud empty | Upload local |
| Sign in, local empty (fresh install) | Apply cloud silently |
| Sign in, both exist, same revision | No-op |
| Sign in, both exist, different | Show UI: **Keep this device** (default) / **Use cloud save** |
| Signed in, local changes | Debounced push (~3s) |
| App launch, signed in, cloud newer | Pull only if user confirmed OR local empty |

## Auth providers

- Google (Android + iOS)
- Sign in with Apple (iOS + Android via OAuth)
