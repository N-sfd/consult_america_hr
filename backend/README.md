# Consult America HR — Backend

Spring Boot 3 / Java 21 API backing the [Consult America HR Angular frontend](https://consult-america-hr-nine.vercel.app). Implements auth, resumes (with file storage), applicant/job/candidate CRUD, document uploads, user profiles, and email sending.

## Run locally

Requires JDK 21 and Maven (or use the included `mvnw` wrapper once generated).

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The `local` profile uses an in-memory H2 database (no setup needed) and relaxed cookie settings so it's reachable from `http://localhost:4200`. The app listens on port `9090` by default.

To point the Angular frontend at your local backend during development, set `apiBaseUrl: 'http://localhost:9090'` in `src/environments/environment.ts` (no context path — this backend uses clean root paths).

## Run against real Postgres

Set these environment variables before starting without the `local` profile:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/hrdb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

## Configuration reference

| Env var | Purpose | Default |
|---|---|---|
| `PORT` | HTTP port | `9090` |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | Postgres connection | — (required outside `local` profile) |
| `JPA_DDL_AUTO` | Hibernate schema strategy | `update` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins for the frontend | Vercel prod URL + `localhost:4200` |
| `FRONTEND_URL` | Base URL used to build password-reset links | Vercel prod URL |
| `MAIL_ENABLED` | Turn on real email sending | `false` (all mail calls no-op + log a warning until this is `true` and SMTP vars are set) |
| `SMTP_HOST` / `SMTP_PORT` / `SMTP_USERNAME` / `SMTP_PASSWORD` / `SMTP_FROM` | SMTP credentials | — |

## Deployment

See [DEPLOYMENT.md](./DEPLOYMENT.md) for the Render setup steps.
