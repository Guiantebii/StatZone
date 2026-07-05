# StateZone

A football statistics and competition management platform — built for accurate data tracking, live match events, and automated tournament progression.

---

## Tech Stack

### Backend (`statezone-backend`)

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.4.1 |
| Database | PostgreSQL 15 (H2 for tests) |
| Migrations | Flyway |
| Auth | JWT with refresh tokens, httpOnly cookies |
| Realtime | WebSocket (STOMP over SockJS) |
| API docs | SpringDoc OpenAPI (Swagger UI) |
| External API | ApiFootball (WebClient) |
| Build | Maven, Lombok, MapStruct |

### Frontend (`statezone-frontend`)

| Layer | Technology |
|-------|-----------|
| Language | TypeScript (`strict: true`) |
| Framework | React 19 |
| Build | Vite 8 |
| Styling | Tailwind CSS 4 |
| Routing | React Router 7 |
| HTTP | Axios with interceptors |
| Realtime | STOMP.js + SockJS |
| Icons | Lucide React |
| Toasts | Sonner |
| Tests | Vitest + Testing Library (unit) |
| E2E | Playwright |
| Lint | ESLint + Prettier |
| Hooks | Husky + lint-staged |

---

## Features

### Backend

- User registration & JWT login with refresh token rotation
- Full CRUD for championships, teams, players, matches
- Automatic fixture generation (round-robin, groups, knockout)
- Bracket engine with winner propagation, penalty shoot-outs, automatic phase progression
- Live match event recording (goals, cards, substitutions, VAR)
- Player statistics (goals, assists, clean sheets, cards) per career and per championship
- League standings with tie-breaking criteria and per-turn classification
- Automatic suspension system (yellow card accumulation, straight red cards)
- Real-time match notifications via WebSocket
- External data import (teams, players) from ApiFootball
- Role-based access control (`USER` / `OPERADOR` / `ADMIN`)
- Rate limiting filter
- Global exception handling with consistent JSON error responses
- Cached ranking computation with LRU eviction

### Frontend

- Admin dashboard with stats summary & quick actions
- Public home page with live matches, upcoming fixtures, recent results
- Protected routes with role-based redirects
- Match detail page with timeline, lineups, events, and live WebSocket updates
- Championship detail with tabs: classification, matches, top scorers, assists, bracket view
- Team detail with squad, recent matches, statistics
- Player detail with career stats per championship
- Import page for ApiFootball data
- Search bar with keyboard navigation and ARIA attributes
- Polling with tab-visibility awareness (pauses when hidden)
- Responsive design (mobile menu, collapsible sidebar)
- 13 unit test files (63 tests) + Playwright E2E tests

---

## Project Structure

```
.
├── statezone-backend/          # Spring Boot REST API
│   ├── src/
│   │   ├── main/java/.../
│   │   │   ├── config/         # Security, WebSocket, CORS
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── filter/         # Rate limiting, JWT auth
│   │   │   ├── model/          # JPA entities
│   │   │   ├── repository/     # Spring Data repositories
│   │   │   ├── security/       # JWT service, auth filter
│   │   │   ├── service/        # Business logic
│   │   │   │   ├── helper/     # Job schedulers
│   │   │   │   └── ranking/    # Ranking/cache engine
│   │   │   └── dto/            # Request/Response DTOs
│   │   └── test/               # 123 tests
│   └── pom.xml
│
├── statezone-frontend/         # React SPA
│   ├── src/
│   │   ├── api/                # Axios client, interceptors
│   │   ├── components/         # Shared UI components
│   │   │   └── ui/             # Button, Card, Modal, etc.
│   │   ├── constants/          # Helpers, status, pagination
│   │   ├── context/            # AuthContext
│   │   ├── hooks/              # useWebSocket, usePolling
│   │   ├── pages/              # Route pages
│   │   ├── test/               # Unit tests (63 tests)
│   │   ├── types/              # TypeScript interfaces
│   │   └── utils/              # Logger
│   ├── e2e/                    # Playwright E2E tests
│   ├── playwright.config.ts
│   └── package.json
│
├── .github/workflows/          # GitHub Actions CI
├── .husky/                     # Git hooks (lint-staged)
├── .lintstagedrc.json
└── docker-compose.yml          # PostgreSQL container
```

---

## Quick Start

### Prerequisites

- JDK 17+
- Node.js 22+
- PostgreSQL 15+ (or Docker)
- Maven

### 1. Clone

```bash
git clone https://github.com/Guiantebii/StatZone.git
cd StatZone
```

### 2. Database

**Option A — Local PostgreSQL**
```bash
createdb statzone
# Then configure application.properties
```

**Option B — Docker**
```bash
# Create .env with POSTGRES_USER, POSTGRES_PASSWORD, POSTGRES_DB
docker compose up -d db
```

### 3. Backend

```bash
cd statezone-backend
cp src/main/resources/application.properties.example src/main/resources/application.properties
# Edit application.properties with your DB credentials and JWT secret
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`.

### 4. Frontend

```bash
cd statezone-frontend
npm install --legacy-peer-deps
cp .env.example .env
npm run dev
```

The app will be available at `http://localhost:5173`.

---

## Testing

```bash
# Backend
cd statezone-backend && mvn test

# Frontend unit
cd statezone-frontend && npm test

# Frontend E2E (requires backend running)
cd statezone-frontend && npm run test:e2e
```

### Current coverage

| Layer | Tests |
|-------|-------|
| Backend | 123 (controllers, services, repositories) |
| Frontend unit | 63 (components, hooks, API client) |
| Frontend E2E | 4 (login, dashboard, navigation) |

---

## CI/CD

GitHub Actions runs on every push/PR to `main`:

1. **Backend** — `mvn test` (H2 in-memory DB)
2. **Frontend** — `npm ci --legacy-peer-deps`, `npm run lint`, `npm run build`, `npm test`

---

## Environment Variables

### Backend

| Variable | Description |
|----------|-------------|
| `DB_USERNAME` | PostgreSQL user |
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | Base64-encoded HMAC key |
| `JWT_EXPIRATION_MS` | Access token TTL |
| `API_FOOTBALL_KEY` | ApiFootball API key |

### Frontend

| Variable | Description |
|----------|-------------|
| `VITE_API_URL` | Backend API URL |
| `VITE_WS_URL` | Backend WebSocket URL |

---

## API Documentation

Interactive docs at `/swagger-ui.html` when the backend is running.

Key endpoints:

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/auth/login` | POST | Public | Login |
| `/api/auth/registro` | POST | Public | Register |
| `/api/auth/refresh` | POST | Cookie | Refresh token |
| `/api/auth/me` | GET | User | Current user info |
| `/campeonatos` | GET | Public | List championships |
| `/campeonatos/{id}` | GET | Public | Championship details |
| `/partidas` | GET | Public | List matches |
| `/partidas/{id}` | GET | Public | Match details with timeline |
| `/times` | GET | Public | List teams |
| `/jogadores` | GET | Public | List players |

Admin-only endpoints (`@PreAuthorize("hasRole('ADMIN')")`) for CRUD operations, imports, and sensitive mutations.
