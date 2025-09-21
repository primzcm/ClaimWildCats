# Firebase Setup Guide

This guide walks through connecting the ClaimWildCats stack to your Firebase project.

## 1. Create Firebase project

1. Visit [Firebase console](https://console.firebase.google.com/), create a new project (e.g. `claimwildcats-dev`).
2. Enable the following products:
   - **Authentication** · Email/Password and Google sign-in.
   - **Firestore** · Native mode database for items and claims.
   - **Cloud Storage** · Store uploaded item photos.

## 2. Generate service account credentials

1. Open **Project settings ? Service accounts**.
2. Click **Generate new private key** for the `Firebase Admin SDK`.
3. Download the JSON file and place it somewhere private (outside of version control).
4. Set an environment variable that references the path:

   ```bash
   export FIREBASE_CREDENTIALS_PATH=file:/absolute/path/to/serviceAccount.json
   export FIREBASE_ENABLED=true
   export FIREBASE_PROJECT_ID=claimwildcats-dev
   export FIREBASE_STORAGE_BUCKET=claimwildcats-dev.appspot.com
   export FIREBASE_DATABASE_URL=https://claimwildcats-dev.firebaseio.com
   ```

   > On Windows PowerShell use `setx` or configure a `.env` file and load it with your process manager.

## 3. Configure backend application.yml

The Spring Boot API binds Firebase settings through `FirebaseProperties`. Either rely on environment variables (recommended for secrets) or edit `backend/src/main/resources/application.yml`:

```yaml
firebase:
  enabled: true
  project-id: claimwildcats-dev
  storage-bucket: claimwildcats-dev.appspot.com
  database-url: https://claimwildcats-dev.firebaseio.com
  credentials:
    location: file:/absolute/path/to/serviceAccount.json
```

> When the `firebase.enabled` flag is false, the API falls back to in-memory stubs so developers can keep working without cloud access.

## 4. Connect Firestore & Storage in code

- `FirebaseFacade` centralises access to `FirebaseApp`. Replace the stub methods in `ItemService`, `ClaimService`, and `AdminService` with Firestore calls.
- Use `FirestoreClient.getFirestore(firebaseFacade.getAppOrThrow())` to query collections such as `lostItems` and `claims`.
- For uploads, call `StorageClient.getInstance(firebaseFacade.getAppOrThrow())` or let the front-end upload directly using Firebase Web SDK.

### Storage structure & rules

- Upload evidence documents to the configured Storage bucket under `items/{itemId}/`. The API enforces this path and rejects URLs that point elsewhere.
- Use the item id returned from the API when building object names (for example `gs://<bucket>/items/{itemId}/evidence.pdf`).
- Lock Storage Rules to authenticated users, enforce `contentType == "application/pdf"`, and cap uploads at 10 MB so validation stays aligned.

## 5. Secure HTTP layer

- Validate Firebase ID tokens in `SecurityConfig` by adding a `FirebaseAuthenticationFilter`.
- Tie the authenticated UID to `UserProfile` lookups so only owners can edit their posts.

## 6. Front-end integration

- Install the Firebase Web SDK in the React app (`npm install firebase`).
- Create a config module under `frontend/src/lib/firebase.js` that exports the initialized client app.
- Use Firebase Auth hooks for login/register flows and attach ID tokens to API requests.

Once these steps are complete, both the SPA and the API will communicate securely through Firebase while maintaining the structure already scaffolded in this repository.

