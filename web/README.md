# Pokédex web

A responsive React and TypeScript field guide for the sibling Spring Boot API. Browse the PokéAPI catalog, inspect Pokémon details, and sign in to synchronize Pokémon into a local collection with editable field notes.

## Features

- Paginated catalog and local collection views.
- Pokémon detail view with description, stats, abilities, weight, and evolution line.
- Search within the current page and filter by starter/electric/water/fire/grass categories.
- Demo account sign-in and account registration.
- Protected sync and local field-note updates using the API's bearer token.
- Responsive layout, loading placeholders, and API error messages.

## Run locally

Requirements: Node.js 20+ and npm. Start the backend and PostgreSQL from the repository root:

```bash
docker compose up -d
./gradlew bootRun
```

The API runs on `http://localhost:8080`. In another terminal:

```bash
cd web
npm install
npm run dev
```

Open the local URL printed by Vite (usually `http://localhost:5173`). Vite proxies `/api` requests to the Spring Boot server on port 8080.

The seeded demo account is `demo` / `demo1234`. Demo Pokémon are seeded in the local collection by the backend, so open **My collection** to see and edit them. Catalog browsing and Pokémon details use the backend's public routes.

## Configuration

By default, the web app calls `/api/v1` on its current origin. The Vite development server proxies `/api` to `http://localhost:8080`. To point the frontend at a different API, set `VITE_API_URL` to the API base path, for example:

```bash
VITE_API_URL=https://api.example.com/api/v1 npm run dev
```

Vite reads this variable at startup; restart the dev server after changing it.

## API routes used

| Feature | Method and route |
| --- | --- |
| Browse catalog | `GET /api/v1/pokemon?page=0&size=20` |
| Pokémon details | `GET /api/v1/pokemon/{idOrName}` |
| Browse local collection | `GET /api/v1/local/pokemon?page=0&size=20` |
| Local Pokémon details | `GET /api/v1/local/pokemon/{idOrName}` |
| Register / sign in | `POST /api/v1/auth/register`, `POST /api/v1/auth/login` |
| Synchronize Pokémon | `POST /api/v1/local/pokemon/{idOrName}/sync` (bearer token) |
| Update field notes | `PUT /api/v1/local/pokemon/{idOrName}` (bearer token) |

## Scripts

```bash
npm run dev      # Start the development server
npm run lint     # Run ESLint
npm run build    # Type-check and create a production build
npm run preview  # Preview the production build
```
