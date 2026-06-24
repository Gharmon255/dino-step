# Hosting the Dino Step privacy policy (Android Play Console)

Play Console requires a **public HTTPS URL**. The policy text lives in **`docs/privacy-policy.html`**.

## Option A — GitHub Pages (recommended)

1. Push `dino-step` to GitHub (e.g. `gharmon255/dino-step`).
2. Repo **Settings → Pages → Build and deployment → Source**: Deploy from branch `main`, folder `/docs`.
3. Your URL becomes:
   ```
   https://gharmon255.github.io/dino-step/privacy-policy.html
   ```
4. That URL is already set in `app/src/main/res/values/strings.xml` as `privacy_policy_url`.
5. Paste the same URL in Play Console → **App content → Privacy policy**.

> If your GitHub username or repo name differs, update `privacy_policy_url` in `strings.xml` before release.

## Option B — Any static host

Upload `docs/privacy-policy.html` to Netlify, Cloudflare Pages, Google Sites, etc. Update `privacy_policy_url` to match.

## Before submission

- [ ] Push `main` — GitHub Pages rebuilds from `/docs` (may take 1–2 minutes)
- [ ] Run `./scripts/verify-privacy-url.sh` — confirms hosted page includes cloud backup, hourly sync, PvP, contact email (`stepasaurushelp@gmail.com`)
- [ ] Open the URL in a browser (incognito) — page loads over HTTPS
- [ ] Complete Play **Data Safety** using `docs/DATA_SAFETY_PLAY_CONSOLE.md`
