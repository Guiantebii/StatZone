# StateZone

A football statistics and competition management platform — built for accurate data tracking, live match events, and automated tournament progression.

## Project Structure

- `statezone-backend` — REST API and WebSocket server (Spring Boot)
- `statezone-frontend` — Web application (planned)

---

## Backend (`statezone-backend`)

### Tech Stack

- **Java 17**
- **Spring Boot 3.4.1**
- **PostgreSQL 15**
- **Spring Security** – JWT‑based authentication and role‑based authorization
- **Flyway** – database schema versioning
- **SpringDoc OpenAPI** – interactive API documentation (Swagger UI)
- **WebSocket (STOMP)** – real‑time match updates
- **WebClient** – external API integration (ApiFootball)
- **Maven**, **Lombok**, **MapStruct**

### Key Features

- User registration and login with JWT
- Full CRUD for championships, teams, players
- Automatic fixture generation (round‑robin, groups, knockout stages)
- Bracket engine with winner propagation, penalty shoot‑outs, and automatic phase progression
- Live match event recording (goals, cards, substitutions, VAR)
- Player statistics per career and per championship (goals, assists, clean sheets, cards)
- League standings with tie‑breaking criteria and per‑turn classification
- Automatic suspension system (yellow card accumulation, straight red cards)
- Real‑time match notifications via WebSocket
- External data import (teams, players)
- Role‑based access control (USER / ADMIN)
- Global exception handling with consistent JSON error responses

### Quick Start

1. **Requirements**: JDK 17+, PostgreSQL 15+ (or Docker), Maven.
2. Clone the repository.

3. **Option A – Local database**
   - Create a PostgreSQL database named `statzone`.
   - Configure connection properties in `application.properties`.

   **Option B – Docker database**
   - Create an `.env` file in the project root with the required variables
   - Start the database container: `docker compose up -d db`

4. Set the required environment variables (or edit `application-security.properties`):

```properties
# Database (adjust if not using Docker defaults)
spring.datasource.url=jdbc:postgresql://localhost:5432/statzone
spring.datasource.username=myuser
spring.datasource.password=yourpassword

# JWT
JWT_SECRET=your-base64-encoded-secret
JWT_EXPIRATION_MS=86400000
