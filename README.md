# pokejava-api

REST API built with Java 25 and Spring Boot 4 that browses Pokémon from [PokeAPI](https://pokeapi.co/docs/v2),
syncs them into a local PostgreSQL database and lets authenticated users enrich them with custom fields.

## Running locally

Requirements: JDK 25 and Docker.

```bash
docker compose up -d   # PostgreSQL on localhost:5432
./gradlew bootRun
```

### Demo credentials

On startup the API seeds a demo user when missing:

| Username | Password  | Display name  |
|----------|-----------|---------------|
| `demo`   | `demo1234`| Demo Trainer  |

```bash
# Register
curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"ash","password":"pikachu12","displayName":"Ash Ketchum"}'

# Login
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo","password":"demo1234"}'

# Protected mutation (Bearer token from login/register)
curl -s -X POST http://localhost:8080/api/v1/local/pokemon/bulbasaur/sync \
  -H "Authorization: Bearer <accessToken>"
```

**Public routes:** `POST /api/v1/auth/*`, `GET /api/v1/pokemon/**`, `GET /api/v1/local/pokemon/**`  
**Protected routes:** `POST /api/v1/local/pokemon/{id}/sync`, `PUT /api/v1/local/pokemon/{id}`

## Tests

```bash
./gradlew test
```

Integration tests start their own PostgreSQL container with Testcontainers, so only Docker needs to be running.
