# Auth sequence

How a user gets from app launch to an authorized API call. The app has two sign-in paths, and both
end with a Bearer token that the API validates as an OAuth2 Resource Server (no sessions, no cookies).
Why Google and Credential Manager: [ADR 0002](adr/0002-auth-google-resource-server.md).

## Sign in with Google

```mermaid
sequenceDiagram
    autonumber
    actor U as User
    participant A as Android app (LoginScreen)
    participant CM as Credential Manager / Google Play services
    participant G as Google (accounts.google.com)
    participant API as EventHub API (Spring Resource Server)
    participant DB as Postgres

    U->>A: Launch app, tap "Sign in with Google"
    A->>CM: getCredential(GetSignInWithGoogleOption(serverClientId))
    CM->>U: Account picker and consent
    U->>CM: Choose account
    CM->>G: Authorize the app for serverClientId
    G-->>CM: Signed ID token (JWT: iss, aud = serverClientId, sub, email)
    CM-->>A: GoogleIdTokenCredential.idToken
    A->>API: GET /api/v1/users/me, Authorization: Bearer <ID token>
    API->>G: Fetch signing keys (JWKS, cached)
    API->>API: Verify signature, expiry, iss = https://accounts.google.com, aud = GOOGLE_CLIENT_ID
    API->>DB: Find user by oauth_subject, create on first sign-in
    DB-->>API: User row (is_admin)
    API-->>A: 200 {id, username, admin, ...}
    A->>A: Keep token in AuthSession, open Admin or Events screen
    A->>API: Any protected call, Authorization: Bearer <ID token>
    API-->>A: 200, or 401 (bad or expired token), or 403 (not organizer or not admin)
```

## Username and password

```mermaid
sequenceDiagram
    autonumber
    actor U as User
    participant A as Android app (LoginScreen)
    participant API as EventHub API
    participant DB as Postgres

    U->>A: Enter username and password
    A->>API: POST /api/v1/auth/login {username, password}
    API->>DB: Find user by username
    DB-->>API: User row with BCrypt hash
    API->>API: BCrypt check (same 401 for unknown user and wrong password)
    API-->>A: 200 {id, username, admin, accessToken}
    Note over API: accessToken is an HS256 JWT signed with APP_TOKEN_SECRET,<br/>iss = eventhub-app, sub = user id, valid one hour
    A->>API: Protected call, Authorization: Bearer <accessToken>
    API->>API: Verify signature, expiry and iss = eventhub-app
    API-->>A: 200, 401 or 403
```

## Logout

The app drops the token from `AuthSession` and returns to the login screen. Tokens are stateless, so
the API keeps no session to end; a token simply stops working when it expires.
