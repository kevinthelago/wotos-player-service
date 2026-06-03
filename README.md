# WoToS Player Service

Microservice in the [WoToS](https://github.com/users/kevinthelago/projects/2) system. Manages World of Tanks player accounts — searches the WoT API by nickname, persists player records locally, and stores time-series snapshots plus live achievements. Called exclusively by the edge service.

Spring Boot 3.2 · Java 17 · Spring Cloud 2023.0.x · MySQL · OpenFeign · SpringDoc OpenAPI.

## Prerequisites

- Java 17 (Temurin recommended)
- Maven or the included `./mvnw` wrapper
- MySQL 8 running at `localhost:3306`, user `root`, password `root`
- Database `wotos_players_database` (created automatically by Hibernate on first run)
- WoT application ID set as environment variable: `WG_APP_ID`
- `wotos-eureka-server` running (service registry)
- `wotos-config-server` running at `localhost:4040`

The service listens on port **4343**.

## Running Locally

### Command Line

```bash
./mvnw spring-boot:run
```

### IntelliJ

1. Open the project root in IntelliJ IDEA.
2. Set the environment variable `WG_APP_ID=<your-app-id>` in the Run Configuration.
3. Run `WotosPlayerServiceApplication`.

## Building

```bash
./mvnw clean package        # build JAR, skip tests
./mvnw clean install        # build JAR + run all tests
```

## Docker

Multi-stage build (Temurin 17 JRE, non-root) exposing 4343:

```bash
docker build -t ghcr.io/kevinthelago/wotos-player-service:dev .
```

CI (`.github/workflows/maven.yml`) builds and tests against a MySQL service, then
publishes `ghcr.io/kevinthelago/wotos-player-service:dev` on every push to `develop`.

## Testing

Unit tests run without infrastructure. The `contextLoads` test needs MySQL at
`localhost:3306`; the Testcontainers integration test needs a Docker daemon (it is
skipped automatically when Docker is unavailable).

```bash
./mvnw verify                            # run all tests + coverage report
./mvnw test -Dtest=PlayerServiceTest     # run a single test class
```

Jacoco coverage report: `target/site/jacoco/index.html` (controller/ + service/ ≥ 70%).

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/players` | Get stored player details by account IDs |
| `POST` | `/api/players` | Create player records by fetching from the WoT API |
| `PUT` | `/api/players` | Update player records from the WoT API |
| `GET` | `/api/players/list` | Search WoT players by nickname |
| `GET` | `/api/players/haveUpdated` | Check whether player records are stale vs the WoT API |
| `GET` | `/api/players/snapshots` | Get player snapshots as a time series (optional `from`/`to` epoch-second window) |
| `POST` | `/api/players/snapshots` | Create a snapshot per account from stored player records |
| `GET` | `/api/players/{accountId}/achievements` | Live player achievements + cached achievement metadata |
| `GET` | `/api/players/achievements` | Get stored achievement snapshots by account IDs |
| `POST` | `/api/players/achievements` | Create an achievement snapshot per account |

### Error responses

All errors share the envelope `{"error":{"code":<int>,"message":<string>,"correlationId":<uuid>}}`.
The `correlationId` is also emitted in the structured (JSON) logs for tracing.
Status mapping: `400` validation · `404` not found · `502` upstream WoT error ·
`504` upstream WoT timeout · `500` unexpected.

## Swagger UI

```
http://localhost:4343/swagger-ui/index.html
```
