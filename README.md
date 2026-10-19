# pokejava-api

RESTful Pokémon API for a Java technical interview exercise.

Built with **Java 25**, **Spring Boot 4**, **Clean Architecture**, and **TDD**. The service talks to
[PokeAPI](https://pokeapi.co/docs/v2), replicates selected Pokémon into PostgreSQL, and allows authenticated
updates of proprietary fields (local name, region, tags).

A frontend consumer lives under `web/` (developed separately).

## Architecture

| Layer | Responsibility |
|-------|----------------|
| `adapter/web` | REST controllers, DTOs, exception mapping |
| `adapter/pokeapi` | External PokeAPI client + Caffeine response cache |
| `adapter/persistence` | JPA / PostgreSQL |
| `adapter/security` | BCrypt + JWT |
| `adapter/demo` | Seeded demo user + local Pokémon |
| `application` | Use cases (`AuthService`, `PokemonQueryService`, `LocalPokemonService`) |
| `domain` | Entities, validation, domain errors |

## Prerequisites

- JDK 25
- Docker Desktop (WSL2 backend on Windows)

## Environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/pokejava` | JDBC URL |
| `DB_USERNAME` | `pokejava` | DB user |
| `DB_PASSWORD` | `local_dev_password` | DB password |
| `JWT_SECRET` | `local-dev-jwt-secret-change-me-32b` | HMAC secret (≥ 32 chars) |
| `JWT_EXPIRATION_MINUTES` | `60` | Access token TTL |
| `APP_DEMO_SEED_ENABLED` | `true` | Seed demo user + local Pokémon on empty DB |
| `APP_DEMO_USERNAME` | `demo` | Demo login |
| `APP_DEMO_PASSWORD` | `demo1234` | Demo password |
| `APP_DEMO_DISPLAY_NAME` | `Demo Trainer` | Demo display name |
| `POKEAPI_CACHE_MAX_SIZE` | `500` | Caffeine max entries per cache |
| `POKEAPI_CACHE_TTL` | `10m` | Cache TTL (`10m`, `1h`, …) |

## Run with Docker (recommended for demos)

Builds the API image and starts PostgreSQL + API:

```bash
docker compose up --build -d
docker compose ps
docker compose logs -f api
```

API: `http://localhost:8080`

Stop / reset (wipes DB volume):

```bash
docker compose down        # keep data
docker compose down -v     # wipe seeded data
```

## Run with Gradle (local JVM)

```bash
docker compose up -d postgres   # DB only
./gradlew bootRun
```

## Mock credentials & seeded data

On first boot (empty store), the API seeds:

| Username | Password   | Display name  |
|----------|------------|---------------|
| `demo`   | `demo1234` | Demo Trainer  |

| ID | Name       | Local name      | Region        | Tags                   |
|----|------------|-----------------|---------------|------------------------|
| 1  | bulbasaur  | Bulbasaurzinho  | Kanto-South   | starter, grass, demo   |
| 4  | charmander | Charmandinho    | Kanto-Central | starter, fire, demo    |
| 7  | squirtle   | Squirtlezinho   | Kanto-Coast   | starter, water, demo   |
| 25 | pikachu    | Pikachuzinho    | Kanto-Power   | mascot, electric, demo |

No PokeAPI sync is required for the local CRUD walkthrough.

## How to demo

```bash
# 1) Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo","password":"demo1234"}' | jq -r .accessToken)

# 2) Public catalog browse (PokeAPI + cache)
curl -s 'http://localhost:8080/api/v1/pokemon?page=0&size=5' | jq

# 3) Public local list (seeded)
curl -s 'http://localhost:8080/api/v1/local/pokemon?page=0&size=20' | jq

# 4) Protected update of proprietary fields
curl -s -X PUT http://localhost:8080/api/v1/local/pokemon/pikachu \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"localName":"Pika BR","region":"Sao Paulo","tags":["mascot","demo"]}' | jq

# 5) Optional protected sync from PokeAPI
curl -s -X POST http://localhost:8080/api/v1/local/pokemon/eevee/sync \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Routes

| Access | Methods | Paths |
|--------|---------|-------|
| Public | `POST` | `/api/v1/auth/register`, `/api/v1/auth/login` |
| Public | `GET` | `/api/v1/pokemon/**`, `/api/v1/local/pokemon/**` |
| Protected (JWT) | `POST` | `/api/v1/local/pokemon/{idOrName}/sync` |
| Protected (JWT) | `PUT` | `/api/v1/local/pokemon/{idOrName}` |

## Nice-to-haves included

- **Caffeine cache** in front of PokeAPI (`list` / `summary` / `detail`)
- **JWT auth** with public vs protected routes
- **Demo seeder** for credentials + local Pokémon
- **Dockerfile** + Compose full stack

## Tests

```bash
./gradlew test
```

Integration tests use Testcontainers (Docker required). Fast slice without DB containers:

```bash
./gradlew test --tests 'com.wesleyornellas.pokejava_api.application.*' \
  --tests 'com.wesleyornellas.pokejava_api.adapter.web.*' \
  --tests 'com.wesleyornellas.pokejava_api.adapter.pokeapi.CachingPokemonCatalogTest' \
  --tests 'com.wesleyornellas.pokejava_api.domain.*'
```

## Project layout

```
src/main/java/.../   Clean Architecture backend
Dockerfile           Multi-stage JDK 25 build → JRE runtime
compose.yaml         postgres + api
web/                 Frontend (separate session — not part of this backend doc)
```
