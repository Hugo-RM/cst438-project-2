# EventHub Proposal

## 1. The pitch (one paragraph)
The API lets users create, publish and manage events, such as meetups, concerts, workshops)
and allows users to find and RSVP to them. (add more info)

## 2. Resources
| Resource | Key fields                                                                                       | Relationships                                               |
|----------|--------------------------------------------------------------------------------------------------|-------------------------------------------------------------|
| User     | id, email, displayName, role (USER/ADMIN)                                                        | A user organizes events but an user has many RSVPs          |
| Event    | id, title, description, startsAt, endsAt, location, capacity, status (Draft/Published/Cancelled) | Belongs to the organizing User, one category. Has "x" RSVPs |
| Category | id, name                                                                                         | A category has "x" number of events                         |
| RSVP     | id, userId, eventId, status (Going/Maybe/Declined), createdAt                                    | This belongs to a user and an event                         |

## 3. ER sketch
Tables, primary and foreign keys, and cardinality. Edit this Mermaid diagram (it renders on GitHub;
try changes at https://mermaid.live):

```mermaid
erDiagram
    USER ||--o{ EVENT : organizes
    USER ||--o{ RSVP : makes
    EVENT ||--o{ RSVP : receives
    CATEGORY ||--o{ EVENT: classifies
    USER {
        bigint id PK
        string username UK
        string password
        string role
    }
    CATEGORY {
        bigint id PK
        string name
    }
    EVENT {
        bigint id PK
        bigint organizer_id FK
        bigint category_id FK
        string title
        string description "nullable"
        timestamp starts_at
        timestamp ends_at
        string location
        int capacity "nullable"
        string status
    }
    RSVP{
        bigint id PK
        bigint user_id FK
        bigint event_id FK
        string status
        timestamp created_at
    }
```

## 4. Endpoints
| Verb   | Path                                                        | Auth   | Purpose                                                 |
|--------|-------------------------------------------------------------|--------|---------------------------------------------------------|
| GET    | /api/v1/events?page=0&size=20&category=&from=&sort=startsAt | public | list events published (**paginated filters:** category) |
| GET    | /api/v1/events/{id}                                         | public | event details                                           |
| POST   | /api/v1/events                                              | user   | create an event                                         |
| PUT    | /api/v1/events/{id}                                         | user   | update my event (only for organizers)                   |
| DELETE | /api/v1/events/{id}                                         | user   | delete my event (only for organizers)                   |
| GET    | /api/v1/users/me/events                                     | user   | events I organize                                       |
| POST   | /api/v1/events/{id}/rsvps                                   | user   | RSVP to an event                                        |
| DELETE | /api/v1/events/{id}/rsvps                                   | user   | cancel my RSVP                                          |
| GET    | /api/v1/events/{id}/rsvps                                   | user   | attendee  list (only for organizers)                    |
| GET    | /api/v1/categories                                          | public | list category                                           |
| POST   | /api/v1/categories                                          | admin  | create a category                                       |
| DELETE | /api/v1/admin/events/{id}                                   | admin  | remove any event (moderation)                           |
Mark each endpoint `public`, `user`, or `admin`. Mark which collection paginates and which
filters or sorts.

## 5. Technical choices
- **Database host:** (Neon, Supabase, Railway, Atlas, ...) and why
- **OAuth2 provider:** (Google, GitHub, Auth0) and confirmation that it supports Authorization Code + PKCE from a native app
- **Repo layout:** monorepo or split, and why
These become your ADRs later.

## 6. Risks
The two things most likely to go wrong, and what you will do first to find out.

## 7. Team and Sprint 1
Who owns what in Sprint 1. Link your Project board and Sprint 1 milestone.
Victor Borba - User DAO/Repository
