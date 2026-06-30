# JamConnect Backend

A backend API for **JamConnect**, a social network for musicians. It combines
an Instagram/Facebook-style social feed (posts, likes, comments, follows)
with **live real-time jam sessions**: a band leader can build a setlist,
switch the currently-playing song, and transpose its key on the fly — and
every connected musician's screen updates instantly over WebSocket.

## Tech stack

- **Java 17**, **Spring Boot 3.3**, **Maven**
- **MySQL** (via Spring Data JPA + Hibernate), schema managed by **Flyway**
- **JWT** authentication (stateless access tokens + rotating refresh tokens)
- **WebSocket / STOMP** for real-time jam session broadcasts and in-app chat delivery
- Local disk storage for uploaded photos/videos (no S3/cloud dependency, so the
  project runs anywhere out of the box)

## Project structure

```
src/main/java/com/jamconnect/app/
  config/        Spring configuration (security, JWT, CORS, uploads, WebSocket, JPA auditing)
  controller/    REST controllers (the full HTTP API surface)
  dto/           Request/response DTOs, grouped by feature
  entity/        JPA entities
  enums/         Shared enums (Role, InstrumentType, JamSessionStatus, ...)
  exception/     Custom exceptions + a global @RestControllerAdvice handler
  repository/    Spring Data JPA repositories
  security/      JWT provider, UserDetailsService, auth filter, SecurityUtils
  service/       Business logic
  util/          ChordTransposer (the music theory engine) + mappers
  websocket/     STOMP config, channel/handshake interceptors, event publishers
src/main/resources/
  application*.yml          Profile-based configuration (dev / prod / test)
  db/migration/V1__*.sql    Flyway schema migration
```

## Prerequisites

- JDK 17+
- Maven 3.8+
- A running MySQL 8 instance (or Docker)

## Running it locally

1. **Start MySQL.** The simplest option if you have Docker:

   ```bash
   docker run --name jamconnect-mysql -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql:8
   ```

   The app will auto-create the `jamconnect` database on first connect
   (`createDatabaseIfNotExist=true`), so you don't need to create it manually.

2. **Configure environment variables** (all have sensible local defaults, but
   you should always override the JWT secret before deploying anywhere real):

   | Variable | Default | Purpose |
   |---|---|---|
   | `JAMCONNECT_JWT_SECRET` | a placeholder base64 string | HMAC signing key for access tokens |
   | `JAMCONNECT_DB_URL` | `jdbc:mysql://localhost:3306/jamconnect?...` | JDBC URL |
   | `JAMCONNECT_DB_USERNAME` | `root` | MySQL username |
   | `JAMCONNECT_DB_PASSWORD` | `root` | MySQL password |
   | `JAMCONNECT_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173` | CORS allow-list, comma-separated |
   | `JAMCONNECT_UPLOADS_DIR` | `uploads` | Local folder for stored media |

3. **Run it:**

   ```bash
   mvn spring-boot:run
   ```

   Flyway runs automatically on startup and creates all tables. The API is
   then available at `http://localhost:8080`.

### Importing into an IDE

Both IntelliJ IDEA and Eclipse can open this directly as a Maven project —
just point "Open" / "Import Project" at the folder containing `pom.xml`.
Make sure annotation processing is enabled for Lombok (IntelliJ: Settings →
Build, Execution, Deployment → Compiler → Annotation Processors → enable;
this is on by default in recent versions). A Lombok plugin/IDE integration is
recommended for inline support but isn't required to build.

### Running tests

```bash
mvn test
```

Tests run against an in-memory H2 database (`test` profile) so no MySQL
instance is needed just to run `mvn test`.

## Authentication flow

1. `POST /api/v1/auth/register` or `/login` → returns `accessToken` (15 min
   lifetime) + `refreshToken` (7 day lifetime, rotates on each refresh).
2. Send `Authorization: Bearer <accessToken>` on every subsequent request.
3. When the access token expires, call `POST /api/v1/auth/refresh` with the
   refresh token to get a new pair.

## REST API overview

All endpoints are under `/api/v1`. A representative sample:

| Area | Endpoints |
|---|---|
| Auth | `POST /auth/register`, `/login`, `/refresh`, `/logout` |
| Profiles | `GET/PUT /users/me`, `GET /users/{username}`, `POST /users/me/profile-photo` |
| Follows | `POST/DELETE /users/{id}/follow`, `POST /follow-requests/{id}/accept`, `GET /follow-requests/pending` |
| Posts | `POST /posts` (multipart, optional photo/video), `GET /posts/feed`, `GET /posts/explore`, `POST /posts/{id}/like` |
| Comments | `POST /posts/{id}/comments` (supports `parentCommentId` for replies), `GET /comments/{id}/replies` |
| Chat | `GET /messages/conversations`, `POST /messages/users/{recipientId}`, `POST /messages/attachments` |
| Song library | `POST /songs`, `GET /songs/mine`, `GET /songs/public` |
| Jam sessions | `POST /jam-sessions`, `POST /jam-sessions/join/{inviteCode}`, `POST /jam-sessions/{id}/current-song`, `POST /jam-sessions/{id}/transpose` |
| Notifications | `GET /notifications`, `GET /notifications/unread-count`, `POST /notifications/mark-all-read` |

Most list endpoints accept standard Spring `Pageable` query params:
`?page=0&size=20&sort=createdAt,desc`.

## Live jam sessions over WebSocket

This is the core real-time feature. A session leader creates a session,
shares its 6-character invite code, and other musicians join. The leader then
controls what everyone sees:

1. **Connect**: open a STOMP connection to `ws://localhost:8080/ws` (SockJS
   fallback also registered at the same path). Authenticate by sending your
   JWT access token in the STOMP `CONNECT` frame's `Authorization` header
   (`Bearer <token>`), or as a `token` header.
2. **Subscribe** to `/topic/jam-sessions/{sessionId}` to receive every update
   for that session: song changes, live transpositions, setlist edits,
   participants joining/leaving, and session start/end.
3. **Control the session** either via plain REST (`POST
   /jam-sessions/{id}/current-song`, `POST /jam-sessions/{id}/transpose`) or
   by sending STOMP messages directly to `/app/jam-sessions/{id}/change-song`
   and `/app/jam-sessions/{id}/transpose` — both paths trigger the same
   broadcast to every subscriber.

Example broadcast payload when the leader switches songs:

```json
{
  "eventType": "SONG_CHANGED",
  "jamSessionId": 42,
  "timestamp": "2026-06-18T10:15:00Z",
  "currentSong": { "id": 7, "songId": 3, "title": "Wonderwall", "originalKey": "F#m", "position": 0 },
  "transposedKey": "Am",
  "transposedLyricsWithChords": "[Am]Today is gonna be the day...",
  "transposeOffset": 2
}
```

Every musician's client just needs to render `transposedLyricsWithChords` —
the server has already done the chord-shifting math (see
`util/ChordTransposer.java`), so there's no music theory logic required on
the frontend.

## Notes on local file storage

Uploaded images/videos are written to disk under `uploads/` (configurable via
`JAMCONNECT_UPLOADS_DIR`) and served back at `/api/v1/media/{category}/{filename}`.
This keeps the project dependency-free for local development; swapping in S3
or a CDN later would mean replacing `FileStorageService` without touching any
controller code, since callers only see the returned URL.

## Known limitations / things to harden before production

- `PostService.toggleLike`'s "liked by viewer" check does one extra query per
  post when building feed responses; fine for moderate feed sizes, but worth
  batching if you scale up.
- The JWT secret, DB credentials, and CORS origins all have local-dev
  defaults baked into `application.yml` — **always override these via
  environment variables in any shared or production environment.**
- There's no rate limiting, email verification flow, or password reset flow
  yet — `isEmailVerified` exists on the `User` entity but nothing populates
  it.
