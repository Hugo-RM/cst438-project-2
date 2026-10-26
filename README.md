# EventHub

An Android app and REST API for finding and organizing events (meetups, concerts, workshops).
Anyone can browse published events; signed-in users can create events and RSVP; admins manage
categories and moderate events.

**CST 438 Project 02** · Team: Victor Borba, Linus Schaub, Justin Tzeng, Hugo Ruiz-Mireles

- Proposal: [`docs/proposal.md`](docs/proposal.md)
- API contract: [`docs/openapi.yaml`](docs/openapi.yaml) (render it at https://editor.swagger.io)
- Design: [auth sequence](docs/auth-sequence.md), [error model](docs/error-model.md), [ADRs](docs/adr/)
- Project board: https://github.com/users/Hugo-RM/projects/3/views/1
- Deployed API: https://cst438-project-2.onrender.com (try `/api/v1/health`)
- Swagger UI: https://cst438-project-2.onrender.com/swagger-ui.html (generated from the annotated controllers)

## Tech stack

| Layer    | Choice                                                              |
|----------|---------------------------------------------------------------------|
| API      | Kotlin, Spring Boot, Spring Data JPA, Spring Security (JWT resource server) |
| Database | PostgreSQL (Neon in production, Postgres 17 container locally)      |
| Auth     | Google sign-in (ID token as Bearer JWT) plus BCrypt username/password accounts |
| Android  | Kotlin, Jetpack Compose, Material 3, Credential Manager             |
| CI/CD    | GitHub Actions: detekt, backend and Android tests, Android build, Docker image to GHCR, OpenAPI lint |

## Repository layout

```
app/        Android app (Jetpack Compose)
backend/    Spring Boot API (+ Dockerfile)
docs/       proposal.md (with ER diagram), openapi.yaml, auth-sequence.md, error-model.md, adr/
config/detekt/       detekt ruleset shared by app/ and backend/
.github/    CI workflows, issue and PR templates
docker-compose.yml   API + local Postgres
.env.example         environment variables for the API
```

## Run the API locally

Requirements: Docker.

```bash
echo "APP_TOKEN_SECRET=$(openssl rand -base64 32)" > .env   # required; compose refuses to start without it
docker compose up --build
```

This starts the API at http://localhost:8082 and a throwaway Postgres at `localhost:5432`
(database, user and password are all `eventhub`). Check it:

```bash
curl http://localhost:8082/api/v1/health        # {"status":"ok"}
curl http://localhost:8082/api/v1/events        # public: published events
```

Swagger UI is at http://localhost:8082/swagger-ui.html.

Tables are created automatically on startup (`ddl-auto: update`).

### Configuration

Set these in `.env` (gitignored); `.env.example` lists them all. Leave the `DATABASE_*` lines out to use
the local Postgres.

| Variable            | Purpose                                                              | Default                                      |
|---------------------|----------------------------------------------------------------------|----------------------------------------------|
| `DATABASE_URL`      | JDBC URL, for example the shared Neon database (`?sslmode=require`)  | `jdbc:postgresql://localhost:5432/eventhub`  |
| `DATABASE_USERNAME` | Database user                                                        | `eventhub`                                   |
| `DATABASE_PASSWORD` | Database password                                                    | `eventhub`                                   |
| `GOOGLE_CLIENT_ID`  | OAuth client ID that issued the app's Google tokens; JWTs with any other audience are rejected | empty |
| `APP_TOKEN_SECRET`  | Base64 key (32+ bytes) that signs the tokens returned by `/auth/login`. **Required** | none |

Never commit `.env`, keystores or `local.properties`.

### Run without Docker

Requires JDK 17 and a reachable Postgres (set the variables above, including `APP_TOKEN_SECRET`). The API runs at http://localhost:8080:

```bash
./gradlew :backend:bootRun
./gradlew :backend:test      # repository tests use Testcontainers, so Docker must be running
./gradlew detekt             # static analysis for app/ and backend/; fails on new violations
```

## Run the Android app

1. Open the project in Android Studio and let Gradle sync.
2. Start the API (above). The emulator reaches the host machine at `10.0.2.2`. The default
   `API_BASE_URL` is `http://10.0.2.2:8081`; to point at another server, set the Gradle property
   `eventhubApiUrl` (for example `eventhubApiUrl=http://10.0.2.2:8082` for `docker compose`, in
   `~/.gradle/gradle.properties`).
3. Run the `app` configuration on an emulator.
   Command line:

```bash
./gradlew testDebugUnitTest      # JVM unit tests
./gradlew assembleDebug          # debug APK
./gradlew connectedDebugAndroidTest   # UI tests (emulator or device required)
```

To use the deployed API instead of a local one, build with
`-PeventhubApiUrl=https://cst438-project-2.onrender.com`. If it runs on Render's free tier, the
first request after idle time can take about a minute while the service wakes up.

Google sign-in needs a Google account on the device and a `GOOGLE_CLIENT_ID` on the API that
matches the client ID compiled into the app.

## API overview

All routes are under `/api/v1`. Full details, schemas and error responses are in
[`docs/openapi.yaml`](docs/openapi.yaml).

| Area       | Endpoints                                                                                   | Access                       |
|------------|---------------------------------------------------------------------------------------------|------------------------------|
| Auth       | `POST /auth/register`, `POST /auth/login`                                                   | public                       |
| Events     | `GET /events` (paginated; `category`, `from`, `sort`), `GET /events/{id}`                   | public                       |
|            | `POST /events`, `PUT` / `PATCH` / `DELETE /events/{id}`                                     | signed in; change/delete: organizer only |
| RSVPs      | `POST` / `DELETE /events/{id}/rsvps`, `GET /events/{id}/rsvps`                              | signed in; list: organizer only |
| Categories | `GET /categories`; `POST /categories`, `DELETE /categories/{id}`                            | list public; changes admin only |
| Users      | `GET /users/me`, `GET /users/me/events`                                                     | signed in                    |
| Admin      | `DELETE /admin/events/{id}`, `GET /users`, `DELETE /users/{id}`                             | admin only                   |
| Health     | `GET /health`                                                                               | public                       |

Protected endpoints take `Authorization: Bearer <token>`, where the token is a Google ID token or the
`accessToken` returned by `POST /auth/login` (valid for one hour). Errors use RFC 9457 Problem Details. Event times are local date-times without a zone, for example `2026-11-05T18:00:00`.

### Working with the contract

```bash
npx @redocly/cli lint docs/openapi.yaml          # fix every error before opening a PR
npx @stoplight/prism-cli mock docs/openapi.yaml  # fake API at http://127.0.0.1:4010
```

The mock validates requests and returns the `example:` values from the contract; nothing is saved.

## Roles and rules

- A user becomes an **organizer** by creating an event. Only the organizer can edit, delete or
  list RSVPs for it.
- There is no endpoint to grant admin; set `is_admin = true` on the user's row in the database.
- Event status is `DRAFT` (default), `PUBLISHED` or `CANCELLED`. Only `PUBLISHED` events appear in the public list.
- One RSVP per user per event. A `GOING` RSVP is rejected with 409 once `capacity` is reached.
- A category cannot be deleted while an event uses it.

## Development workflow

- Every issue starts from the user-story template; every PR starts with `Closes #<issue>`.
- `main` is protected: PRs need approving teammates who are not the author.
- If the API changes, update `docs/openapi.yaml` in the same PR and make sure it lints clean.
- CI on each PR and on `main`: detekt, backend tests (Testcontainers), Android unit tests and build.
  PRs also build the Docker image and lint the contract; merges to `main` publish the image to GHCR.
## Links

- OpenAPI 3.0 spec: https://spec.openapis.org/oas/v3.0.3
- Prism: https://docs.stoplight.io/docs/prism
- Mermaid ER diagrams: https://mermaid.js.org/syntax/entityRelationshipDiagram.html
- RFC 9457 Problem Details: https://www.rfc-editor.org/rfc/rfc9457
 