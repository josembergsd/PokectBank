# External Integrations

**Analysis Date:** 2026-05-21

## APIs & External Services

**No external APIs or services detected.**

This application is fully offline and self-contained. A comprehensive search across all 59+ Kotlin source files found:
- Zero network libraries (no Retrofit, OkHttp, Ktor, Volley, or Apache HttpClient)
- Zero HTTP client imports
- Zero socket/WebSocket libraries
- Zero Firebase or Google Play Services SDKs
- Zero payment processing SDKs (no Stripe, PayPal, etc.)
- Zero cloud storage SDKs (no AWS, GCP, Azure)
- Zero third-party analytics or crash reporting

**Network permissions:** None declared in `AndroidManifest.xml` — the app has no ability to make network requests.

**Google Services:** No `google-services.json` file present; no Firebase or Google Play Services integration.

## Data Storage

**Databases:**
- **Room (SQLite)** — `pokectbank.db`
  - Database class: `app/src/main/java/com/pokect/bank/kids/data/local/PokectBankDatabase.kt`
  - Location: Internal app storage (standard Android SQLite path)
  - Connection: Managed by Room `RoomDatabase` singleton — no external connection strings
  - Client: Room DAO interfaces (7 DAOs: `UserDao`, `TransactionDao`, `SavingsGoalDao`, `MissionDao`, `RewardDao`, `FamilyMemberDao`, `WeeklyChallengeDao`)
  - Schema version: 1, migration: destructive fallback
  - Seed data: Pre-populated on creation via `SeedDatabaseCallback` in `PokectBankDatabase`
  - Test variant: `getTestInstance()` returns an in-memory Room database for unit testing

**Key-Value Storage:**
- **DataStore Preferences** — `login_state`
  - Class: `app/src/main/java/com/pokect/bank/kids/data/preferences/LoginPreferences.kt`
  - Properties stored: `is_logged_in` (Boolean), `selected_avatar` (Int)
  - Access: `Flow<LoginPrefsData>` from `LoginPreferences.loginState`
  - Initialized via `preferencesDataStore` delegate in `app/src/main/java/com/pokect/bank/kids/di/AppContainer.kt`

**File Storage:**
- Local filesystem only — no cloud file storage integration
- `pokectbank.db` is the sole database file; deleted and recreated on reset via `PokectBankDatabase.resetInstance()`

**Caching:**
- None beyond Room's built-in SQLite caching and StateFlow in-memory bridging in `AppRepository`
- Repository bridges Room DAO Flows → `MutableStateFlow` for real-time UI updates (see `AppRepository.kt` init block, lines 98-136)
- Event broadcast via `MutableSharedFlow<RepositoryEvent>` (buffer: 16, overflow: `DROP_OLDEST`)

## Authentication & Identity

**Auth Provider:**
- **Custom (DataStore-based)** — no external auth provider (no OAuth, no Firebase Auth, no Auth0)
  - Implementation: `LoginPreferences` wrapping DataStore Preferences
  - Login flow: Email + password form → avatar selection → stores `is_logged_in=true` in DataStore
  - Login gate: `MainActivity.kt` checks `authState.isCheckingLogin` / `authState.isLoggedIn` from `LoginViewModel`
  - Logout: `AppContainer.resetRepository()` clears DataStore via `loginPreferences.clearLogin()` + drops database
  - No real authentication — data is not actually validated against any backend (educational app)

**User Identity:**
- Single-user model (`User` entity with `uid=1`, `id="user-1"`)
- Family members stored as separate entities (`FamilyMember` table) for ranking display
- No multi-user isolation — all data belongs to the single device user

## Monitoring & Observability

**Error Tracking:**
- No external crash reporting (no Firebase Crashlytics, Sentry, Bugsnag)

**Logs:**
- `android.util.Log` — `Log.w()` used in two places:
  - `AppRepository.kt` line 94: `Log.w("AppRepository", "Dropped $event — buffer full")`
  - `AppContainer.kt` line 61: `Log.w("AppContainer", "Failed to clear login state during reset", e)`
  - DAO Flow bridging uses `Log.w("AppRepository", "...")` for caught exceptions in `.catch {}` blocks
- No debug/info logging configured
- No custom logging abstraction

## CI/CD & Deployment

**Hosting:**
- Android APK/AAB — deployable to Google Play Store or sideloaded
- No CI/CD pipeline configuration detected in repository (no `.github/workflows/`, `.gitlab-ci.yml`, `Jenkinsfile`, `bitbucket-pipelines.yml`, `circleci/config.yml`)
- No Fastlane, no Gradle Play Publisher plugin

**Build:**
- Standard Android Gradle build: `./gradlew assembleDebug` / `./gradlew assembleRelease`
- Release builds: ProGuard enabled with `proguard-android-optimize.txt` + `app/proguard-rules.pro` (keeps `@Composable` methods)

## Environment Configuration

**Required env vars:**
- None. The app requires no environment variables, API keys, or external configuration.

**Secrets location:**
- Not applicable — no secrets, API keys, or credentials in use.

## Webhooks & Callbacks

**Incoming:**
- None. The app has no server component and no network listener.

**Outgoing:**
- None. The app makes zero outbound HTTP requests.

## Summary

PokectBank is a **fully offline Android application** with zero external integrations. All data lives locally:

```
Device Storage
├── pokectbank.db          (Room/SQLite — 7 tables)
└── login_state.preferences_pb  (DataStore — 2 keys)
```

All "integrations" are internal Jetpack library integrations:
- **Room ↔ AppRepository:** DAO Flows bridged to StateFlow for reactive UI
- **DataStore ↔ LoginPreferences:** Preferences mapped to Flow<LoginPrefsData>
- **AppRepository ↔ ViewModels:** Manual DI via ViewModelProvider.Factory
- **ViewModels ↔ Compose UI:** StateFlow consumed via `collectAsStateWithLifecycle()`

No external network calls, no cloud services, no third-party authentication, no push notifications, no analytics.

---

*Integration audit: 2026-05-21*
