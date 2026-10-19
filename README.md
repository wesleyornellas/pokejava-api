# pokejava-api

REST API built with Java 25 and Spring Boot 4 that browses Pokémon from [PokeAPI](https://pokeapi.co/docs/v2),
syncs them into a local PostgreSQL database and lets authenticated users enrich them with custom fields.

## Quick start (demo / interview panel)

Requirements: JDK 25 and Docker Desktop (WSL2 engine enabled).

```bash
docker compose up -d          # PostgreSQL on localhost:5432
./gradlew bootRun             # seeds demo user + local Pokémon on first boot
```

No PokeAPI sync is required to walk through the local CRUD flow — demo rows are inserted automatically when the local store is empty.

### Mock credentials

| Username | Password   | Display name  |
|----------|------------|---------------|
| `demo`   | `demo1234` | Demo Trainer  |

### Seeded local Pokémon

| ID | Name       | Local name      | Region        | Tags                         |
|----|------------|-----------------|---------------|------------------------------|
| 1  | bulbasaur  | Bulbasaurzinho  | Kanto-South   | starter, grass, demo         |
| 4  | charmander | Charmandinho    | Kanto-Central | starter, fire, demo          |
| 7  | squirtle   | Squirtlezinho   | Kanto-Coast   | starter, water, demo         |
| 25 | pikachu    | Pikachuzinho    | Kanto-Power   | mascot, electric, demo       |

### Demo script

```bash
# 1) Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo","password":"demo1234"}' | jq -r .accessToken)

# 2) Browse seeded local Pokémon (public)
curl -s 'http://localhost:8080/api/v1/local/pokemon?page=0&size=20' | jq

# 3) Update proprietary fields (protected)
curl -s -X PUT http://localhost:8080/api/v1/local/pokemon/pikachu \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"localName":"Pika BR","region":"Sao Paulo","tags":["mascot","demo"]}' | jq

# 4) Optional: sync another species from PokeAPI (protected)
curl -s -X POST http://localhost:8080/api/v1/local/pokemon/eevee/sync \
  -H "Authorization: Bearer $TOKEN" | jq
```

**Public routes:** `POST /api/v1/auth/*`, `GET /api/v1/pokemon/**`, `GET /api/v1/local/pokemon/**`  
**Protected routes:** `POST /api/v1/local/pokemon/{id}/sync`, `PUT /api/v1/local/pokemon/{id}`

Disable seeding with `app.demo.seed-enabled=false` (or env-style overrides via Spring properties).

## Running locally

```bash
docker compose up -d
./gradlew bootRun
```

Defaults: DB `pokejava` / user `pokejava` / password `local_dev_password` on port `5432`.

## Tests

```bash
./gradlew test
```

Integration tests start their own PostgreSQL container with Testcontainers, so only Docker needs to be running.
