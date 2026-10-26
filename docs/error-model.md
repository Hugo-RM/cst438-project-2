# Error model

Every error response uses **RFC 9457 Problem Details** with the media type
`application/problem+json`. It is the `Problem` schema in [`openapi.yaml`](openapi.yaml).

```json
{
  "title": "Not Found",
  "status": 404,
  "detail": "Event not found",
  "instance": "/api/v1/events/42"
}
```

| Field      | Meaning                                                    |
|------------|------------------------------------------------------------|
| `title`    | Standard reason phrase for the status code                 |
| `status`   | HTTP status code, repeated in the body                     |
| `detail`   | What went wrong, safe to show to the user                  |
| `instance` | The request path that failed                               |

## How it is produced

- Services throw `ResponseStatusException(status, detail)`.
- `spring.mvc.problemdetails.enabled: true` (in `application.yaml`) makes Spring render those, and its
  own validation and binding errors, as Problem Details.
- `AuthController` builds the same `ProblemDetail` body explicitly for login and register errors.
- Error bodies never contain stack traces, SQL, or credentials.

## Status codes

| Code | When                                                                                 |
|------|--------------------------------------------------------------------------------------|
| 400  | Malformed JSON, failed validation, bad query parameter (for example `from=notadate`) |
| 401  | Missing, invalid or expired token; wrong username or password on login               |
| 403  | Valid token but not allowed: not the organizer, or not an admin                      |
| 404  | The event, category, RSVP target or user does not exist                              |
| 409  | Username taken, category exists or is in use, already RSVP'd, event full             |
| 500  | Unexpected server error                                                              |

One exception: when a protected route gets no token or a token that fails validation, Spring
Security answers 401 before the request reaches a controller. That response has an empty body and a
`WWW-Authenticate: Bearer` header, as the OAuth2 Bearer Token spec (RFC 6750) describes.
