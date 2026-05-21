---
phase: 01-persist-ncia-room-datastore
plan: 01
subsystem: "Build Configuration + DataStore Preferences"
tags:
  - gradle
  - ksp
  - room
  - datastore
  - login-persistence
  - preferences
dependency-graph:
  requires: []
  provides: ["KSP plugin", "Room 2.8.4", "DataStore Preferences 1.2.1", "LoginPreferences"]
  affects: ["01-02", "01-03", "01-04"]
tech-stack:
  added:
    - KSP 2.1.0-1.0.29 (annotation processing plugin)
    - Room 2.8.4 (runtime, ktx, compiler via KSP)
    - DataStore Preferences 1.2.1
  patterns:
    - "Top-level Context.dataStore extension with preferencesDataStore delegate (singleton)"
    - "LoginPreferences wraps DataStore<Preferences>, exposes Flow<LoginPrefsData>"
key-files:
  created:
    - "data/preferences/LoginPreferences.kt — DataStore wrapper for login persistence"
  modified:
    - "gradle/libs.versions.toml — added KSP, Room, DataStore versions/libraries/plugins"
    - "build.gradle.kts — added KSP plugin declaration"
    - "app/build.gradle.kts — added KSP plugin + Room + DataStore dependencies"
    - "di/AppContainer.kt — added Context init, DataStore delegate, loginPreferences"
    - "ui/screens/login/LoginViewModel.kt — added loginPrefs param, isCheckingLogin, DataStore write"
    - "ui/navigation/NavGraph.kt — added factory-created LoginViewModel for LoginScreen"
    - "MainActivity.kt — added AppContainer.init(), three-state auth gate"
decisions:
  - "LoginViewModel.loginPrefs is nullable with null default — preserves existing no-arg construction for tests and backward compat"
  - "NavGraph explicitly creates LoginViewModel with factory — ensures DataStore write on login"
  - "DataStore Preferences used instead of Room for login state (2 fields only, no relational schema needed)"
metrics:
  duration: "~3 minutes (build times included)"
  completed_date: "2026-05-21"
---

# Phase 01 Plan 01: Build + DataStore Preferences — Summary

**One-liner:** KSP 2.1.0-1.0.29 + Room 2.8.4 + DataStore Preferences 1.2.1 added to Gradle; Login class wrapping DataStore created; LoginViewModel reads login state on init and persists after login; MainActivity shows three-state auth gate (checking / logged-out / logged-in).

## Requirements Satisfied

| ID | Description | Status |
|----|-------------|--------|
| BUILD-01 | KSP plugin added to root and app build.gradle.kts | ✅ |
| BUILD-02 | Room 2.8.4 (runtime, ktx, compiler) + DataStore 1.2.1 in version catalog | ✅ |
| DATA-01 | LoginPreferences class with DataStore<Preferences> via preferencesDataStore delegate | ✅ |
| DATA-02 | isLoggedIn + selectedAvatar persisted via dataStore.edit {} | ✅ |
| DATA-03 | LoginPreferences.loginState exposes Flow<LoginPrefsData> consumed by LoginViewModel | ✅ |

## Commits

| # | Commit | Type | Description |
|---|--------|------|-------------|
| 1 | `9ba0748` | `build` | Add KSP + Room 2.8.4 + DataStore 1.2.1 dependencies |
| 2 | `9131c42` | `feat` | Add LoginPreferences with DataStore and wire login persistence |

## Files Changed

### Created
- `app/src/main/java/com/pokect/bank/kids/data/preferences/LoginPreferences.kt` — New DataStore wrapper class with `LoginPrefsData`, `setLoggedIn()`, `clearLogin()`, and `loginState` Flow.

### Modified
- `gradle/libs.versions.toml` — Added KSP 2.1.0-1.0.29, Room 2.8.4, DataStore 1.2.1 versions, 4 library entries, 1 plugin entry.
- `build.gradle.kts` (root) — Added `alias(libs.plugins.ksp) apply false`.
- `app/build.gradle.kts` — Added KSP plugin application, Room dependencies (`implementation(libs.room.runtime)`, `implementation(libs.room.ktx)`, `ksp(libs.room.compiler)`), DataStore dependency (`implementation(libs.datastore.preferences)`).
- `app/src/main/java/com/pokect/bank/kids/di/AppContainer.kt` — Added `Context.dataStore` top-level extension via `preferencesDataStore`, `init(context)`, `loginPreferences` lazy property.
- `app/src/main/java/com/pokect/bank/kids/ui/screens/login/LoginViewModel.kt` — Added `loginPrefs: LoginPreferences? = null` constructor param, `isCheckingLogin` to `LoginUiState`, DataStore collection in `init`, `setLoggedIn()` call in `login()`.
- `app/src/main/java/com/pokect/bank/kids/MainActivity.kt` — Added `AppContainer.init(applicationContext)`, factory-created LoginViewModel, three-state auth gate with `CircularProgressIndicator` + "Verificando..." loading screen.
- `app/src/main/java/com/pokect/bank/kids/ui/navigation/NavGraph.kt` — Updated LoginScreen composable to use factory-created `LoginViewModel` with `container.loginPreferences`.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Functionality] NavGraph updated to pass factory-created LoginViewModel**

- **Found during:** Task 2
- **Issue:** The plan stated "no changes needed" for NavGraph, but LoginViewModel now requires `LoginPreferences` to persist login state. Without this change, LoginScreen's default `viewModel()` would create a ViewModel with `loginPrefs = null`, silently skipping the DataStore write on login.
- **Fix:** Updated NavGraph's Login composable to create LoginViewModel via factory, passing `container.loginPreferences`. This follows the same pattern used by all other screens (HomeScreen, GoalsScreen, etc.) and ensures the DataStore write fires on successful login.
- **Files modified:** `ui/navigation/NavGraph.kt`
- **Commit:** `9131c42`

### Design Decisions

1. **LoginViewModel.loginPrefs nullable with null default:** Allows existing no-arg construction (e.g., `viewModel()`) to compile without changes. When null, the init block immediately sets `isCheckingLogin = false` and the login method silently skips persistence. Production code always provides LoginPreferences via factory.
2. **NavGraph deviation logged above:** The plan's expectation that LoginScreen would forgo DataStore access was corrected to ensure login state persistence actually works.
3. **LoginPreferences.setLoggedIn uses standard DataStore edit API:** Instead of the plan's `toMutablePreferences().apply{...}.toPreferences()` pattern, used the simpler idiomatic `prefs[key] = value` inside the `edit` block, which is the documented DataStore Preferences API.

## Threat Scan

No new threat surface introduced beyond the accepted T-01-01 and T-01-02 (DataStore Preferences file on internal storage with low-sensitivity fields: isLoggedIn boolean + selectedAvatar int).

## Self-Check: PASSED

- [x] LoginPreferences.kt exists at `data/preferences/LoginPreferences.kt`
- [x] `preferencesDataStore` referenced in AppContainer.kt
- [x] `AppContainer.init(applicationContext)` called in MainActivity.kt
- [x] `isCheckingLogin` field exists in LoginUiState
- [x] `prefs.setLoggedIn` call exists in LoginViewModel.login()
- [x] `./gradlew assembleDebug` compiles successfully
- [x] Room 2.8.4 and DataStore 1.2.1 resolved in dependency tree
