# ADR 0001: Neon PostgreSQL as the database

**Status:** Accepted

## Context

The project requires a remote, hosted database that every team member can connect to, kept outside
the Docker image. Our data is relational: users organize events, events belong to a category, and
RSVPs join users to events with a "one RSVP per user per event" rule. We use Spring Data JPA, and the
integration tests run on Testcontainers.

## Decision

Use **PostgreSQL hosted on Neon** in production. The API reads the connection from the
`DATABASE_URL`, `DATABASE_USERNAME` and `DATABASE_PASSWORD` environment variables, so no credentials
are in the repo or the image. Local development uses a Postgres 17 container from
`docker-compose.yml`, and the tests use a Postgres Testcontainer.

## Alternatives considered

- **Supabase (Postgres):** works the same way, but bundles auth and storage we don't use.
- **MongoDB Atlas:** our data needs foreign keys, unique constraints and joins, which a document
  store does not enforce.
- **Railway or Render Postgres:** fine, but their free tiers expire or sleep, and Neon's free tier
  was enough for the team.

## Consequences

- One engine everywhere (Neon, compose and Testcontainers), so tests behave like production.
- Foreign keys and unique usernames, Google subjects and category names are enforced by the database.
- Neon suspends idle free databases, so the first query after a pause is slower.
- Schema changes rely on `ddl-auto: update` instead of versioned migrations; acceptable for a class
  project, but it can't express every change (for example renaming a column).
