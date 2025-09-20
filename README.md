# ClaimWildCats

ClaimWildCats is a university lost & found platform built with a React front-end, a Spring Boot API, and Firebase for authentication, storage, and notifications. This repository houses both the web client and the server in a single workspace.

## Project layout

```
frontend/  # React + Vite SPA (JavaScript, React Router, Firebase Auth)
backend/   # Spring Boot 3 REST API with Firebase-ready configuration
docs/      # Additional guides and architectural notes
```

## Prerequisites

- Node.js 18+ and npm
- Java 17+ (the Spring Boot wrapper downloads Maven automatically)
- A Firebase project with Authentication and Firestore enabled (Storage optional for now)

## Quick start

### 1. Configure Firebase credentials

1. Copy `frontend/.env.example` to `frontend/.env.local`.
2. Fill in the `VITE_FIREBASE_*` values from Firebase console ? Project settings ? General ? "Your apps".
3. Place your Admin SDK service account JSON outside the repo and point to it with `FIREBASE_CREDENTIALS_PATH` (see backend section).

### 2. Start the front-end (Vite + React)

```bash
cd frontend
npm install
npm run dev
```

The SPA runs at `http://localhost:5173`. The header reflects Firebase auth state, and the login page supports email/password plus Google sign-in once your env values are set.

### 3. Start the back-end (Spring Boot)

```bash
cd backend
./mvnw spring-boot:run   # use .\mvnw.cmd on Windows
```

The API listens on `http://localhost:8080` with OpenAPI docs at `/swagger-ui/index.html`. When `firebase.enabled=true`, the Firebase Admin SDK boots with the credentials you provide.

## Backend Firebase configuration

Set environment variables or JVM properties before starting the API (see `backend/src/main/resources/application.yml`):

- `FIREBASE_ENABLED=true`
- `FIREBASE_PROJECT_ID`
- `FIREBASE_DATABASE_URL`
- `FIREBASE_STORAGE_BUCKET` (optional until Storage is configured)
- `FIREBASE_CREDENTIALS_PATH` (e.g. `file:/absolute/path/to/serviceAccount.json`)

With these in place, the `FirebaseAuthenticationFilter` accepts `Authorization: Bearer <idToken>` headers, securing POST/PATCH/DELETE item and claim endpoints.

## Recommended next steps

1. Replace service stubs in the Spring Boot services with Firestore reads/writes.
2. Gate profile/report routes on the client using the auth context and add registration UX.
3. Expand the lost/found reporting forms to submit real payloads to the API.
4. Add automated tests around authenticated endpoints and Firestore integration.


