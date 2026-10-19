# pokejava-api

REST API built with Java 25 and Spring Boot 4 that browses Pokémon from [PokeAPI](https://pokeapi.co/docs/v2),
syncs them into a local PostgreSQL database and lets authenticated users enrich them with custom fields.

## Running locally

Requirements: JDK 25 and Docker.

```bash
docker compose up -d   # PostgreSQL on localhost:5432
./gradlew bootRun
```

## Tests

```bash
./gradlew test
```

Integration tests start their own PostgreSQL container with Testcontainers, so only Docker needs to be running.
