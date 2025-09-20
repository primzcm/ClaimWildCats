# AGENTS.md - Working Agreements for ClaimWildCats

This file defines conventions and tips for agents and contributors working in this repository. Its scope is the entire repo.

## Repository Layout

- `frontend/` - React + Vite + JavaScript SPA
- `backend/` - Spring Boot 3 (Java 17+) REST API
- `docs/` - Architecture notes and guides

## General Principles

- Keep changes focused and minimal; prefer small, well-named modules.
- Update documentation when adding or changing endpoints, routes, or config.
- Never add license or copyright headers unless explicitly requested.
- Use clear names; avoid one-letter identifiers except for simple indices.
- Follow existing patterns before introducing new ones; if you must, document them.

## Frontend Conventions (React + Vite)

- Language: JavaScript (ESM), React functional components and hooks only.
- Routing: `react-router-dom` with routes defined in `frontend/src/router.jsx`.
- File structure:
  - Pages: `frontend/src/pages/*Page.jsx`
  - Reusable UI: `frontend/src/components/*`
  - API helpers: `frontend/src/api/*`
- Styling: co-located lightweight CSS files per component/page (e.g., `HomePage.css`). Reuse CSS variables defined in `src/index.css` (burgundy/gold palette).
- Data fetching: use the shared helper `frontend/src/api/client.js`:
  - `api(path, init?)` returns parsed JSON.
  - Errors: throw on non-2xx and surface a friendly message in the UI.
- Dev proxy: Vite proxies `/api` to `http://localhost:8080`. Keep frontend calls path-relative (e.g., `/api/items`).
- UX text wireframes: Each page should start as a minimal skeleton that can be iterated.

## Backend Conventions (Spring Boot)

- Java 17+, Spring Boot 3.5.x.
- Package layout under `com.claimwildcats.api`:
  - `config/` - configuration classes (Firebase, OpenAPI, etc.)
  - `controller/` - REST controllers; endpoints under `/api/...`
  - `domain/` - enums/records for core models returned to clients
  - `dto/` - request/response DTOs; use Jakarta Validation annotations
  - `service/` - business logic; keep controllers thin
  - `security/` - Spring Security configuration and filters
- Documentation: annotate endpoints with springdoc OpenAPI annotations.
- Firebase: configuration via `FirebaseConfig` and `FirebaseProperties`; toggle with `firebase.enabled`. Use `FirebaseFacade` to access the Admin SDK.
- Security: currently permissive for local dev. Before production, add Firebase token verification; document any changes.
- Tests: keep default Spring Boot context tests; add slice/controller tests as endpoints grow.

## Documentation Rules

- Update `DOCS_UPDATES.md` for every meaningful change (code, config, or docs). Include date, scope, and short summary.
- Keep `README.md` accurate for quick start commands.
- Add or extend guides in `docs/` when introducing new subsystems (auth, storage, data model).

## Code Style

- JavaScript: modern ESM syntax; avoid implicit globals. Prefer small pure components and clear props.
- Java: prefer records for simple immutables; avoid Lombok. Use `@Validated` on controllers and Jakarta validation on DTOs.
- Formatting: follow project defaults; do not introduce new formatters without consensus.

## Dependency Management

- Frontend: add runtime deps only when used; keep dev deps lean.
- Backend: declare versions via properties when cross-cutting (e.g., springdoc). Avoid unused starters.

## PR / Change Checklist

- [ ] Code follows structure above and names are descriptive
- [ ] Docs updated (`DOCS_UPDATES.md` entry plus guides if needed)
- [ ] Frontend API calls use `api` and `/api` paths
- [ ] Controllers small; logic in services; validation present
- [ ] Local dev still works: `npm run dev` and `./mvnw spring-boot:run`

---

Notes for agents: Instructions in this AGENTS.md apply to all files in the repo. More deeply nested AGENTS.md files (if added later) take precedence within their directory trees.
