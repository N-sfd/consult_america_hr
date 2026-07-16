# Deploying to Render

This backend lives at `/backend` inside the same repo as the Angular frontend (github.com/N-sfd/consult_america_hr). These steps require your own Render account — they can't be done from here.

## 1. Push to GitHub

The backend is already committed as part of the monorepo. Just push the `main` branch as usual from the repo root:

```bash
git push origin main
```

## 2. Provision on Render

**Option A — Blueprint (recommended, one step):**
1. In the Render dashboard: New → Blueprint → connect the `N-sfd/consult_america_hr` GitHub repo. Render reads `render.yaml` at the repo root — it's configured with `rootDir: backend`, so the web service builds only from the `backend/` subdirectory and won't touch the Angular app. It deploys only the web service; the database is provided by an external free provider like Supabase or Neon.
2. Add the required secret `DATABASE_URL` in the Render web service settings, pointing to your external Postgres database.
3. Trigger a manual redeploy if you want to ensure the first deploy uses the new connection string.

**Option B — Manual:**
1. New → PostgreSQL on an external free provider (for example Supabase or Neon) and note the connection URL.
2. New → Web Service → connect the `N-sfd/consult_america_hr` repo → set **Root Directory** to `backend` → Environment: Docker.
3. Set env vars: `DATABASE_URL`, `CORS_ALLOWED_ORIGINS`, `FRONTEND_URL`, `MAIL_ENABLED=false`, `JPA_DDL_AUTO=update`.

Vercel (the frontend deploy) already ignores the `backend/` directory via `.vercelignore`, so this doesn't affect the existing frontend deployment.

## 3. Verify

Once deployed, Render gives you a URL like `https://consult-america-hr-backend.onrender.com`. Confirm it's up:

```bash
curl -i https://consult-america-hr-backend.onrender.com/jobs
```

Should return `200` with `[]` (empty list, no auth required).

## 4. Wire the frontend to it

Tell me the Render URL once it's live — I'll update `environment.ts`/`environment.prod.ts` in the frontend repo to point at it, fix the `forgot-password.ts` URL bug, and redeploy the frontend on Vercel.

## 5. (Later) Enable real email sending

Pick a free SMTP provider — a Gmail account with an [App Password](https://myaccount.google.com/apppasswords) is the simplest to set up. Then set on the Render web service:

```
MAIL_ENABLED=true
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=<your gmail address>
SMTP_PASSWORD=<the 16-character app password>
SMTP_FROM=<your gmail address>
```

No code changes needed — the app already reads these at startup.

## Notes

- Render's free web service tier spins down after inactivity; the first request after idling will be slow (cold start).
- Render's free Postgres tier can be blocked by account limits when another free database is already active. The blueprint uses the starter Postgres plan to avoid that provisioning failure.
