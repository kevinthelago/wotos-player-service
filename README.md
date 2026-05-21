# WoToS Player Service

Microservice in the [WoToS](https://github.com/users/kevinthelago/projects/2) system. Manages World of Tanks player accounts — searches the WoT API by nickname, persists player records locally, and stores time-series snapshots of player achievements. Called exclusively by the edge service.

## Prerequisites

- Java 8 (Temurin recommended)
- Maven or the included `./mvnw` wrapper
- MySQL 8 running at `localhost:3306`, user `root`, password `root`
- Database `wotos_players_database` (created automatically by Hibernate on first run)
- WoT application ID set as environment variable: `app_id`
- `wotos-eureka-server` running (service registry)
- `wotos-config-server` running at `localhost:4040`

## Running Locally

### Command Line

```bash
./mvnw spring-boot:run
```

### IntelliJ

1. Open the project root in IntelliJ IDEA.
2. Set the environment variable `app_id=<your-app-id>` in the Run Configuration.
3. Run `WotosPlayerServiceApplication`.

## Building

```bash
./mvnw clean package        # build JAR, skip tests
./mvnw clean install        # build JAR + run all tests
```

## Testing

Tests require MySQL running locally.

```bash
./mvnw test                              # run all tests
./mvnw test -Dtest=PlayerServiceTest    # run a single test class
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/players` | Get stored player details by account IDs |
| `POST` | `/api/players` | Create player records by fetching from WoT API |
| `PUT` | `/api/players` | Update player records from WoT API |
| `GET` | `/api/players/list` | Search WoT players by nickname |
| `GET` | `/api/players/haveUpdated` | Check whether player records have been recently updated |
| `GET` | `/api/players/snapshots` | Get player achievement snapshots |
| `POST` | `/api/players/snapshots` | Create new player achievement snapshots |

## Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```
