# ADR 0002: Google sign-in, with the API as an OAuth2 Resource Server

**Status:** Accepted

## Context

A native Android app cannot keep a client secret and has no browser session to hold a cookie, so the
server-side OAuth2 Client pattern from web tutorials does not fit. The API has to validate a Bearer
token on every request, and the admin role has to live in our database.

## Decision

- **Provider: Google.** Everyone on the team and every test device already has a Google account.
- **Android: Credential Manager with Sign in with Google** (`GetSignInWithGoogleOption`). Google Play
  services runs the authorization with Google and returns a signed ID token for our server client
  ID. We chose it over a Chrome Custom Tab running Authorization Code + PKCE through AppAuth, because
  it is Android's current recommended sign-in API and needs no redirect URI handling in the app.
- **API: `spring-boot-starter-oauth2-resource-server`.** Each request's JWT is checked for its
  signature (Google's published keys), expiry, issuer `https://accounts.google.com` and audience
  `GOOGLE_CLIENT_ID`. The API has no login page and no session (`STATELESS`).
- **Roles:** `users.is_admin` in the database, checked in the services. A user row is created on
  first sign-in, keyed by the token's `sub`.
- **Username/password accounts** also exist for testing without a Google account. Passwords are
  BCrypt-hashed, and login returns an HS256 JWT signed with `APP_TOKEN_SECRET` (issuer
  `eventhub-app`), which the same resource server accepts.

## Consequences

- Google ID tokens expire after about an hour, and app tokens after one hour; the user signs in again.
- The app never handles a client secret or an authorization code.
- Sign-in depends on Google Play services, so emulators need a Google Play system image.
- The API accepts two token issuers, so it tries the app's own key first, then Google's.
- The prompt's reference flow is Authorization Code + PKCE in a Custom Tab; Credential Manager hides
  those steps inside Google Play services, so the app's code does not show the PKCE exchange.
- See [`../auth-sequence.md`](../auth-sequence.md) for the full flow.
