# Documentation & Changes Log

Record of notable changes to code and docs. Add entries with date, scope, and brief summary.

## 2025-09-19

- Frontend: Scaffolded Vite + React app (`frontend/`), added React Router routes for sitemap (`src/router.jsx`), app shell and theme (`src/App.jsx`, CSS), and placeholder pages under `src/pages/`).
- Backend: Generated Spring Boot API (`backend/`) with controllers for items, claims, users, admin; domain records and DTOs; Firebase-ready configuration (`FirebaseConfig`, `FirebaseProperties`, `FirebaseFacade`); OpenAPI (`OpenApiConfig`); permissive `SecurityConfig` for dev.
- Docs: Added `README.md` with workspace overview and run instructions; `docs/firebase-setup.md` and `docs/architecture-overview.md`.
- Frontend Data: Created tiny API client `src/api/client.js`. Initial Home page consumed `/api/items` for stub data; utility is available for future pages. Confirmed Vite dev proxy routes `/api` to backend in `vite.config.js`.
- Conventions: Added repo-wide `AGENTS.md` with coding and documentation guidelines.

## 2025-09-19 - Frontend switched to JavaScript

- Migrated the SPA from TypeScript to JavaScript: renamed all `.tsx/.ts` to `.jsx/.js`.
- Replaced `vite.config.ts` with `vite.config.js`; removed `tsconfig.*` and type packages.
- Updated ESLint config to target JS/JSX only.
- Updated all docs to reference JavaScript and new `.jsx/.js` paths.

## 2025-09-20 - Landing hero redesign

- Replaced the feed-style Home page with a marketing hero matching the provided wireframe and color palette (`HomePage.jsx`, `HomePage.css`).
- Updated global theme variables and header/footer styling to the burgundy and gold palette (`index.css`, `App.css`).
- Added a "Get Started" route with placeholder guidance for the onboarding flow (`GetStartedPage.jsx`, router updates).
- Adjusted navigation to highlight Home, Get Started, Lost, Found, and kept Log in as a prominent action (`App.jsx`).

## 2025-09-20 - Firebase Auth wiring

- Frontend: added Firebase auth context and login UI (`AuthContext.jsx`, `LoginPage.jsx/css`), updated header to show user state, and updated the API client to send ID tokens.
- Backend: exposed `FirebaseAuth` bean, introduced a `FirebaseAuthenticationFilter`, and tightened `SecurityConfig` to authenticate write operations when Firebase is enabled.
- Shared: documented changes in this log.



## 2025-09-20 - Firestore persistence

- Backend: ItemService, ClaimService, and UserService now read/write Firestore via FirebaseFacade, storing reporter/claimant context and falling back to stubs when Firebase is disabled.
- Backend: Controllers attach the authenticated Firebase UID; added SecurityUtils helper plus Firestore query helpers.
- Docs: README recommended steps updated to focus on client gating, forms, and automated tests.

- Frontend: added ProtectedRoute/PublicOnlyRoute to guard signed-in pages and redirect guests to login.

## 2025-09-20 - Lost & found forms

- Frontend: Replaced lost/found placeholders with working forms that POST to `/api/items/lost` and `/api/items/found`, including validation, contact details, and redirects to the created item.

