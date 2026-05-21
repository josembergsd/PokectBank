# Codebase Concerns

**Analysis Date:** 2026-05-21

## Tech Debt

### Duplicate LoginViewModel Instances

- Issue: `LoginViewModel` is created twice — once in `MainActivity.kt` (line 52) for the auth gate and once in `NavGraph.kt` (line 46) for the `LoginScreen`. These are separate instances with separate `_uiState` StateFlows. The `login()` method in the NavGraph instance sets `isLoggedIn = true` in its own state, but the MainActivity instance observing a separate `LoginViewModel` through `collectAsStateWithLifecycle()` relies on DataStore flow emission to detect the login. This creates a fragile indirection — the auth gate depends on DataStore propagation timing rather than direct state.
- Files: `app/src/main/java/com/pokect/bank/kids/MainActivity.kt` (line 52), `app/src/main/java/com/pokect/bank/kids/ui/navigation/NavGraph.kt` (line 46), `app/src/main/java/com/pokect/bank/kids/ui/screens/login/LoginViewModel.kt` (line 29)
- Impact: Auth gate may fail to transition or show a flash of login screen due to stale DataStore emission. Login state is not shared across ViewModel instances.
- Fix approach: Extract a single `LoginViewModel` scoped to the Activity (via `viewModel()` at the `MainActivity` level) and pass it down to both the auth gate and `LoginScreen`. Alternatively, use a singleton auth holder via `AppContainer`.

### UiState Classes Defined Inside ViewModels

- Issue: `GoalsUiState` (in `GoalsViewModel.kt`), `RewardsUiState`/`RewardUi` (in `RewardsViewModel.kt`), `MissionsUiState`/`MissionFilterCategory` (in `MissionsViewModel.kt`), and `RankingUiState`/`RankingMember` (in `RankingViewModel.kt`) are all defined within ViewModel files instead of separate files.
- Files: `app/src/main/java/com/pokect/bank/kids/ui/screens/goals/GoalsViewModel.kt` (line 16), `app/src/main/java/com/pokect/bank/kids/ui/screens/rewards/RewardsViewModel.kt` (lines 21-35), `app/src/main/java/com/pokect/bank/kids/ui/screens/missions/MissionsViewModel.kt` (lines 19-40), `app/src/main/java/com/pokect/bank/kids/ui/screens/ranking/RankingViewModel.kt` (lines 20-30)
- Impact: Inconsistent with `HomeUiState` which lives in its own file. Test files must import from ViewModel files. Harder to reuse UiState in test factories.
- Fix approach: Extract each UiState to its own file (e.g., `MissionsUiState.kt`, `RewardsUiState.kt`) following the `HomeUiState.kt` pattern.

### Inconsistent Currency Formatting

- Issue: `GoalsScreen.kt` (lines 100-101) uses `"R$ %.2f".format(it.balance)` for balance display, while the rest of the app uses the `formatCurrency()` utility from `CurrencyFormatter.kt`. The manual format does not use `NumberFormat` locale-aware formatting and produces inconsistent output (e.g., no thousands separator).
- Files: `app/src/main/java/com/pokect/bank/kids/ui/screens/goals/GoalsScreen.kt` (lines 100-101), `app/src/main/java/com/pokect/bank/kids/util/CurrencyFormatter.kt`
- Impact: Balance display differs between Goals screen and Home screen.
- Fix approach: Replace `"R$ %.2f".format(it.balance)` with `formatCurrency(it.balance)`.

### No Database Migrations — Destructive Fallback

- Issue: `PokectBankDatabase.kt` uses `.fallbackToDestructiveMigration()` (line 64). Any Room schema change (version bump) will drop and recreate all tables, destroying user data. The export schema is also disabled (`exportSchema = false`), making migration testing impossible.
- Files: `app/src/main/java/com/pokect/bank/kids/data/local/PokectBankDatabase.kt` (lines 36-37, 64)
- Impact: All user data (balances, deposited coins, goals) is lost on any database version upgrade. This is acceptable for v1 but should be addressed before a production release.
- Fix approach: Implement Room `Migration` objects for each version bump. Enable `exportSchema = true` and store schema JSON files in a versioned directory.

### RepositoryScope Not Recreated After Reset

- Issue: `AppRepository.resetData()` cancels `repositoryScope` (line 301) and calls `PokectBankDatabase.resetInstance()` to delete the DB. However, `AppContainer.resetRepository()` nullifies both `_repository` and `_database` fields. The next access to `repository` will lazily create a new `AppRepository` with a fresh `repositoryScope` and database. But the old `AppRepository`'s StateFlow bridge coroutines (7 launches in `init{}`) are now cancelled — any ViewModel still holding a reference to the old repository will observe stale frozen StateFlows.
- Files: `app/src/main/java/com/pokect/bank/kids/data/repository/AppRepository.kt` (lines 298-309), `app/src/main/java/com/pokect/bank/kids/di/AppContainer.kt` (lines 56-66)
- Impact: Race condition between `resetRepository()` call and ViewModel re-instantiation. ViewModels may hold a stale repository reference.
- Fix approach: After `resetRepository()`, navigate to Login screen which recreates all ViewModels. Current `HomeScreen.onNavigateToLogin` does this (line 75 of `NavGraph.kt`). Document this constraint explicitly.

### No Interface for AppRepository

- Issue: All ViewModels depend directly on the concrete `AppRepository` class. There is no `Repository` interface or abstraction layer. This makes it impossible to swap implementations (e.g., remote API backend) or provide test doubles without modifying all ViewModels.
- Files: `app/src/main/java/com/pokect/bank/kids/data/repository/AppRepository.kt`, all `*ViewModel.kt` files, all `*ViewModelFactory.kt` files
- Impact: Tight coupling between UI layer and data layer. Hard to test ViewModels with mocked repositories.
- Fix approach: Extract a `Repository` interface with the public API surface. Have `AppRepository` implement it. Update ViewModels and factories to depend on the interface.

### Non-Thread-Safe Lazy Initialization in AppContainer

- Issue: `AppContainer` uses nullable backing fields (`_database`, `_repository`) with a custom getter-based lazy pattern (lines 27-52). These fields are not `@Volatile` and the getter has no synchronization. Under concurrent access, two threads could create two `AppRepository` instances with separate StateFlow bridges.
- Files: `app/src/main/java/com/pokect/bank/kids/di/AppContainer.kt` (lines 26-52)
- Impact: Under rare multi-threaded access (e.g., configuration change + coroutine launch), duplicate Repository instances could be created, leading to inconsistent state between ViewModels.
- Fix approach: Use `kotlin.Lazy` with `SynchronizedLazyImpl` (`by lazy`) or add `@Volatile` + `synchronized` block to the getters.

### Force-Unwrap Operators

- Issue: Two `!!` force-unwrap operations in `AppContainer.kt` — `return _database!!` (line 32) and `return _repository!!` (line 51). If `init(context)` is never called and `_database`/`_repository` are null due to a bug, these throw `NullPointerException` with no diagnostic message.
- Files: `app/src/main/java/com/pokect/bank/kids/di/AppContainer.kt` (lines 32, 51)
- Impact: Hard crash with unhelpful stack trace.
- Fix approach: Replace `!!` with `?: error("AppContainer not initialized. Call init(context) first.")`.

### Room Main-Thread Query Asymmetry

- Issue: `PokectBankDatabase.getTestInstance()` uses `.allowMainThreadQueries()` (line 84), but `getInstance()` does not. Test code can call DAOs synchronously on main thread, but production code would crash with the same pattern. This hides potential ANR issues during testing.
- Files: `app/src/main/java/com/pokect/bank/kids/data/local/PokectBankDatabase.kt` (lines 78-86)
- Impact: Tests may pass with main-thread queries that would cause ANRs in production.
- Fix approach: Remove `.allowMainThreadQueries()` from test instance construction. Use `runTest`/`StandardTestDispatcher` properly so Room dispatches suspend functions correctly.

### Comma-Based StringList Converter

- Issue: `PokectBankConverters.kt` uses comma as a delimiter for `List<String>` conversion (line 54). The converter comment (lines 52-53) acknowledges this limitation: "If IDs ever include commas, this converter will need a different delimiter strategy."
- Files: `app/src/main/java/com/pokect/bank/kids/data/local/PokectBankConverters.kt` (lines 49-58)
- Impact: If any participant ID or badge name ever contains a comma, the list serialization breaks.
- Fix approach: Use JSON array serialization (`org.json.JSONArray`) or a custom delimiter (e.g., `|`). Alternatively, store these as separate join tables.

### Suppressed Unchecked Casts in All ViewModel Factories

- Issue: Every `*ViewModelFactory.kt` uses `@Suppress("UNCHECKED_CAST")` with `as T` cast (6 factories + 2 inline factories in MainActivity and NavGraph = 8 occurrences). This is a known Android ViewModel pattern but 8 separate suppress annotations suggest a missing abstraction.
- Files: `app/src/main/java/com/pokect/bank/kids/ui/screens/home/HomeViewModelFactory.kt` (line 10), `GoalsViewModelFactory.kt` (line 10), `MissionsViewModelFactory.kt` (line 10), `RewardsViewModelFactory.kt` (line 10), `RankingViewModelFactory.kt` (line 10), `NavGraph.kt` (line 48), `MainActivity.kt` (line 54)
- Impact: Type safety depends on developer diligence. A mismatched ViewModel class in a factory would throw `ClassCastException` at runtime.
- Fix approach: Extract a generic `ViewModelFactory<T : ViewModel>` base class that encapsulates the unchecked cast once.

## Known Bugs

### RewardsViewModel.confirmRedemption Overwrites Fresh Coins

- Symptoms: After a successful redemption, the coin count displayed may briefly flash the pre-redemption value before updating.
- Files: `app/src/main/java/com/pokect/bank/kids/ui/screens/rewards/RewardsViewModel.kt` (lines 114-152)
- Trigger: On successful redemption, line 132 calls `refreshCoins()` which fetches the latest user data and updates `_uiState` via `update {}`. Then on lines 133-140, `_uiState.update {}` is called again with `state.copy(...)` — but `state` was captured before `refreshCoins()` returned via `_uiState.value` snapshot. The fresh coins value from `refreshCoins()` may be overwritten by the stale snapshot captured at line 133.
- Workaround: The subsequent `refreshRewards()` call triggered by `RepositoryEvent.RewardsChanged` event corrects the state after a short delay.
- Fix approach: Remove the `refreshCoins()` call on line 132 and instead fold the coin update into the `_uiState.update {}` block on line 133. Or restructure to use `repository.getUser().coins` directly in the `update` lambda.

### LoginViewModel Password Plaintext in State

- Symptoms: Password is stored in `LoginUiState.password` field with no encryption. Visible in heap dumps, debug logs, and process inspection.
- Files: `app/src/main/java/com/pokect/bank/kids/ui/screens/login/LoginViewModel.kt` (line 19, 75-76)
- Trigger: Always — password is held in StateFlow memory for the lifetime of the LoginViewModel.
- Workaround: None.
- Fix approach: Clear the password field from UiState after login completes (`_uiState.update { it.copy(password = "") }`). Consider using `char[]` or Android `SecureString` instead of `String`.

### LoginViewModel with Null LoginPreferences Allows Unauthenticated Login

- Symptoms: If `LoginViewModel` is constructed with `loginPrefs = null` (the constructor default), the `login()` method (lines 133-143) skips the DataStore write entirely but still sets `isLoggedIn = true`. This means the auth gate `MainActivity` — which observes `LoginViewModel(AppContainer.loginPreferences)` — would not detect this login because the DataStore `loginState` flow never emits `true`.
- Files: `app/src/main/java/com/pokect/bank/kids/ui/screens/login/LoginViewModel.kt` (lines 30-31, 131-143), `NavGraph.kt` (line 50)
- Trigger: The `LoginViewModel` in `NavGraph.kt` is created with `container.loginPreferences` (non-null), so this path is not currently triggered. However, the nullable constructor parameter is a latent risk.
- Workaround: The production code always passes a non-null preferences instance.
- Fix approach: Make `loginPrefs` non-nullable. If the parameter is truly optional, skip the `_uiState.update { it.copy(isLoggedIn = true) }` when no preferences are available and show an error instead.

### No Error Display on Goals, Missions, Rewards, and Ranking Screens

- Symptoms: Validation errors (e.g., "Erro ao criar meta") are set in `UiState.error` but the screens do not have a `SnackbarHost` or any error display component. Only `HomeScreen` and `LoginScreen` display errors.
- Files: `app/src/main/java/com/pokect/bank/kids/ui/screens/goals/GoalsScreen.kt`, `MissionsScreen.kt`, `RewardsScreen.kt`, `RankingScreen.kt`
- Trigger: Any failed operation (e.g., insufficient coins for a goal allocation, network error) silently fails with no user-facing feedback.
- Workaround: None — errors are invisible to the user.
- Fix approach: Add a `SnackbarHost` (as in `LoginScreen`) or an error banner to each screen. Alternatively, create a shared error display composable.

### StateFlow Bridge Errors Silently Swallowed

- Symptoms: If any Room DAO Flow emits an error (e.g., database corruption, schema mismatch), the `.catch {}` block in `AppRepository.init{}` (lines 103-135) logs a warning and the `StateFlow` remains frozen at its last successful value or empty list. The UI shows stale data with no error indication.
- Files: `app/src/main/java/com/pokect/bank/kids/data/repository/AppRepository.kt` (lines 98-136)
- Trigger: Database corruption, Room migration failure, or SQLite-level errors in any table.
- Workaround: None — error is only visible in logcat at `WARN` level.
- Fix approach: After catching a flow error, broadcast a `RepositoryEvent` that indicates data is stale. ViewModels should observe this and surface an error in the UI.

### FloatingEmojiBackground Leaks Coroutines

- Symptoms: `LoginScreen`'s `FloatingEmojiBackground` (lines 213-265) launches 6 `LaunchedEffect` coroutines with `while(true)` infinite loops. These have no cancellation mechanism — a `LaunchedEffect(Unit)` with `Unit` key runs for the entire composition lifetime, but once the LoginScreen is removed from composition (login succeeds), they should be cancelled automatically by the Compose runtime. However, the manual `delay()` inside the loop makes cancellation timing dependent on when the step loop is interrupted. If the loop is mid-step when cancelled, the state update `yOffsets[idx].floatValue = ...` may throw on a disposed snapshot.
- Files: `app/src/main/java/com/pokect/bank/kids/ui/screens/login/LoginScreen.kt` (lines 213-265)
- Trigger: Navigate away from LoginScreen while emojis are mid-animation.
- Workaround: The error is transient — state updates on disposed snapshots are typically no-ops in Compose.
- Fix approach: Replace manual animation loops with `InfiniteTransition` or `Animatable` APIs, which are lifecycle-aware and properly handle cancellation.

## Security Considerations

### Allow Backup Enabled

- Risk: `android:allowBackup="true"` in `AndroidManifest.xml` (line 5) enables automatic backup of app data (Room database, DataStore preferences) to Google Drive/cloud. This includes the Room database with the user's financial simulation data and the DataStore with login state. If the backup is restored on a different device or a rooted device, data can be extracted.
- Files: `app/src/main/AndroidManifest.xml` (line 5)
- Current mitigation: None — backup is enabled with no restrictions.
- Recommendations: Set `android:allowBackup="false"` for a children's banking app. If backup is needed, use `android:fullBackupContent` with rules excluding sensitive files, and consider encrypting the database with SQLCipher.

### No ProGuard/R8 Obfuscation

- Risk: Release builds have `isMinifyEnabled = false` (line 22 of `app/build.gradle.kts`). The APK contains unobfuscated bytecode with all class names, method names, and string constants visible. This makes reverse engineering trivial.
- Files: `app/build.gradle.kts` (line 22), `app/proguard-rules.pro`
- Current mitigation: None — minification is disabled.
- Recommendations: Enable `isMinifyEnabled = true` with appropriate ProGuard keep rules for Room entities, JSON models, and Compose classes. At minimum, use R8's default shrinking.

### Activity Exported Without Intent Filters

- Risk: `MainActivity` has `android:exported="true"` (line 14 of `AndroidManifest.xml`) because it has an `intent-filter` with `MAIN`/`LAUNCHER`. While this is required for the launcher activity, it means any app on the device can launch `MainActivity` with arbitrary intent extras.
- Files: `app/src/main/AndroidManifest.xml` (lines 12-22)
- Current mitigation: The Activity does not process intent extras beyond standard lifecycle, so the attack surface is minimal.
- Recommendations: Validate that no intent extras are consumed by `MainActivity`. Add intent data validation if extras are used in the future.

### Plaintext Password in StateFlow

- Risk: `LoginUiState.password` (line 19 of `LoginViewModel.kt`) stores the user's password as a plain `String` in a `MutableStateFlow`. Kotlin `String` objects are immutable and remain in heap memory until garbage collected. If a heap dump is taken (e.g., for crash reporting), the password is captured.
- Files: `app/src/main/java/com/pokect/bank/kids/ui/screens/login/LoginViewModel.kt` (lines 19, 75-76)
- Current mitigation: None — password is in plaintext throughout login flow.
- Recommendations: Use `CharArray` instead of `String` for password storage. Clear the password from state immediately after login. For a children's app where "login" is purely cosmetic (no real authentication), this is low severity but still a security smell.

## Performance Bottlenecks

### Infinite Animation Loops in LoginScreen

- Problem: `LoginScreen` has 12+ coroutine-based animation loops running simultaneously — 3 in `LogoSection`, 6 in `FloatingEmojiBackground`, 3 in `FooterSection`. Each loop uses `delay()` with very short intervals (25-50ms) and continuously updates Compose state. On low-end devices, this causes visible jank and battery drain.
- Files: `app/src/main/java/com/pokect/bank/kids/ui/screens/login/LoginScreen.kt` (lines 213-265, 270-392, 932-1006)
- Cause: Manual `while(true)` + `delay()` loops are not frame-synchronized. Compose `InfiniteTransition` is frame-aware and more efficient.
- Improvement path: Replace all manual animation loops with `rememberInfiniteTransition()` and `animateFloat()`/`animateValue()`. This reduces coroutine overhead and synchronizes animations with the display refresh rate.

### Double Database Reads on Reward/Goal Refresh

- Problem: `RewardsViewModel.refreshRewards()` (lines 79-88) calls `repository.getUser()` and `repository.getRewards()` separately. `GoalsViewModel.loadGoalsAndUser()` (lines 43-47) calls `repository.getGoals()` and `repository.getUser()` separately. Each call is a separate Room query. Since there is only one user, this could be optimized to a single DAO call.
- Files: `app/src/main/java/com/pokect/bank/kids/ui/screens/rewards/RewardsViewModel.kt` (lines 79-88), `app/src/main/java/com/pokect/bank/kids/ui/screens/goals/GoalsViewModel.kt` (lines 43-47)
- Cause: ViewModels treat each data fetch as independent. Room queries are fast but the pattern adds unnecessary overhead.
- Improvement path: Combine user + rewards/goals into a single repository method or use parallel `async`/`await` to fetch both simultaneously, though for a single-user in-memory SQLite database the performance gain is marginal.

### No Lazy Loading for Transaction History

- Problem: `HomeViewModel.loadInitialData()` (lines 41-54) fetches ALL transactions from the database. If the app accumulates hundreds of transactions over time, loading and displaying them all in the `TransactionHistoryCard` composable will slow down the Home screen.
- Files: `app/src/main/java/com/pokect/bank/kids/ui/screens/home/HomeViewModel.kt` (lines 41-54), `app/src/main/java/com/pokect/bank/kids/data/local/dao/TransactionDao.kt` (line 14)
- Cause: Transaction DAO has no pagination — `SELECT * FROM transaction ORDER BY date DESC` returns all rows.
- Improvement path: Add a `LIMIT` clause to the DAO query (e.g., last 50 transactions). Add a "Load More" button or paginated fetching in the ViewModel.

## Fragile Areas

### AppRepository StateFlow Bridge

- Files: `app/src/main/java/com/pokect/bank/kids/data/repository/AppRepository.kt` (lines 98-136)
- Why fragile: Seven independent coroutine launches bridge Room `Flow` emissions into `MutableStateFlow.value = ...` assignments. If any Room Flow stops emitting (database corruption, thread death), the corresponding StateFlow silently freezes. The `catch {}` block only logs — it does not retry or propagate the error. All ViewModels depend on these StateFlows for correctness.
- Safe modification: When modifying Room entities or DAO queries, run the full test suite (`AppRepositoryTest.kt`) to verify seed data integrity. Never change DAO flow signatures without corresponding bridge updates.
- Test coverage: Good for seed data verification. No test for flow error recovery or bridge failure scenarios.

### AppContainer Lazy Initialization Chain

- Files: `app/src/main/java/com/pokect/bank/kids/di/AppContainer.kt` (lines 18-66)
- Why fragile: `AppContainer.init(context)` must be called before any access to `repository` or `loginPreferences`. If `MainActivity.onCreate()` is skipped (e.g., by a test or deep link), accessing `AppContainer.repository` crashes with `UninitializedPropertyAccessException` (from `lateinit var appContext` on line 19). The dependency chain (`context` → `database` → `repository`) has no validation.
- Safe modification: Always call `AppContainer.init(context)` in `Application.onCreate()` or the first Activity's `onCreate()`. Add a `check(::appContext.isInitialized)` guard in the `database` getter.
- Test coverage: `AppContainerTest.kt` exists. Check if it validates the initialization guard.

### Database Reset Flow

- Files: `app/src/main/java/com/pokect/bank/kids/data/repository/AppRepository.kt` (lines 298-309), `app/src/main/java/com/pokect/bank/kids/ui/screens/home/HomeViewModel.kt` (lines 147-174), `app/src/main/java/com/pokect/bank/kids/di/AppContainer.kt` (lines 56-66)
- Why fragile: The reset flow spans three classes:
  1. `HomeViewModel.confirmReset()` calls `repository.resetData(context)` 
  2. `AppRepository.resetData()` cancels the repository scope and deletes the database
  3. `HomeScreen` observes `resetCompleted` and navigates to Login
  4. `NavGraph.onNavigateToLogin` calls `AppContainer.resetRepository()` which nullifies `_repository` and `_database`
  5. On next Login, `AppContainer.repository` lazily creates a fresh `AppRepository` which triggers `PokectBankDatabase.getInstance()` which runs `SeedDatabaseCallback` to repopulate seed data
  
  If any step fails silently (e.g., database deletion fails but `Result.success(Unit)` is returned), the user may see corrupted state.
- Safe modification: Do not modify the reset flow without end-to-end testing. Consider consolidating reset logic into `AppContainer.resetRepository()`.
- Test coverage: `AppRepositoryTest.kt` tests `resetData()` and post-reset operation guards (lines 419-465). No test for the full chain (ViewModel → Repository → Container → new Repository).

## Scaling Limits

### Single User Architecture

- Current capacity: 1 user (hardcoded `"user-1"`). All DAO queries use `LIMIT 1` or `isCurrentUser = 1`.
- Limit: Cannot support multiple users or user switching. The `User` entity has no foreign key relationships to other entities — data isolation depends entirely on there being only one row in each table.
- Scaling path: If multi-user support is needed, add a `userId` column to all entities and use filtered DAO queries. Restructure `AppRepository` to be user-scoped.

### In-Memory Event Buffer

- Current capacity: `RepositoryEvent` SharedFlow has 16 extra buffer capacity with `DROP_OLDEST` overflow strategy (line 87).
- Limit: If more than 16 events are emitted before any collector consumes them, older events are silently dropped. This is logged at WARN level.
- Scaling path: Increase buffer capacity or switch to `BufferOverflow.SUSPEND` if event processing order is critical.

### No Migration Path for Database Schema

- Current capacity: Database version 1 with `exportSchema = false`.
- Limit: Any schema change (new column, new table, type change) requires a destructive migration that wipes all user data. There is no way to test migrations because schema history JSON files are not generated.
- Scaling path: Enable `exportSchema = true`, set up a `schemas/` directory, and write Room `Migration` objects for each version increment. Use `MigrationTestHelper` for testing.

## Dependencies at Risk

### Compose BOM 2024.12.01

- Risk: The Compose BOM version (`2024.12.01`) is from December 2024 — approximately 16 months old as of May 2026. Newer BOM versions may include bug fixes, performance improvements, and API changes.
- Impact: Potential compatibility issues with newer Kotlin versions (currently 2.1.0). Missing Compose bug fixes and optimizations.
- Migration plan: Update BOM version to latest stable (e.g., `2025.05.00` or newer). Run full test suite and verify all Compose screens render correctly.

### Robolectric 4.14.1

- Risk: Robolectric `4.14.1` is used for running Android-dependent tests on JVM. This version may not fully support `targetSdk = 36` or `compileSdk = 36`.
- Impact: Some tests may pass incorrectly or fail with `SdkExtensions` errors if Robolectric doesn't fully emulate API 36.
- Migration plan: Upgrade to Robolectric 4.14+ or the latest version compatible with API 36 SDK. Monitor for `@Config(sdk = [36])` annotations in tests.

## Missing Critical Features

### No Offline Data Validation on App Start

- Problem: When the app starts, it reads from Room database with no integrity check. If the database file is corrupted, the app may crash or show inconsistent data.
- Blocks: Reliable app startup on devices with storage issues.
- Recommendation: Add a database integrity check (`PRAGMA integrity_check`) on database open, or use Room's `JournalMode.WRITE_AHEAD_LOGGING` with proper error handling.

### No Error Recovery UI

- Problem: When a Room DAO flow fails (caught by `catch {}` in bridge), the UI silently shows stale data. There is no retry button, no error banner, and no mechanism for the user to know something went wrong.
- Blocks: User awareness of data issues. Debugging production issues.
- Recommendation: Add a `RepositoryEvent.DatabaseError` event variant. ViewModels should observe it and expose `error: String?` in UiState. Each screen should display errors via a `SnackbarHost`.

## Test Coverage Gaps

### No Compose UI Tests

- What's not tested: All 6 screens and their composable components have zero UI tests. `ComponentsTest.kt` is a placeholder with a single `assert(true)` test.
- Files: `app/src/test/java/com/pokect/bank/kids/ui/screens/home/components/ComponentsTest.kt` (entire file), all `*Screen.kt` files
- Risk: UI layout regressions, accessibility issues, dark mode rendering problems, and interaction bugs go undetected until manual testing.
- Priority: Medium — the app has manual verification. Should add Robolectric-based Compose UI tests for critical flows (login, deposit, redeem).

### No LoginViewModel Tests

- What's not tested: All login flow logic — validation, tab switching, step navigation, DataStore persistence, error handling, forgot password flow.
- Files: `app/src/main/java/com/pokect/bank/kids/ui/screens/login/LoginViewModel.kt` (entire file)
- Risk: Login validation errors or DataStore failures would only be caught during manual testing. The nullable `loginPrefs` parameter means the ViewModel can operate in a degraded mode that is never tested.
- Priority: High — login is the entry gate for the entire app.

### No Individual ViewModel Unit Tests

- What's not tested: `GoalsViewModel`, `RewardsViewModel`, `MissionsViewModel`, `RankingViewModel` have no dedicated test files. Only `HomeViewModel` has tests.
- Files: `app/src/test/java/com/pokect/bank/kids/ui/screens/goals/GoalsViewModelTest.kt` (exists but minimal), no test files for Rewards, Missions, Ranking ViewModels
- Risk: Business logic bugs in goal creation, reward redemption, mission filtering, or ranking display are not caught automatically.
- Priority: Medium — the repository layer is well tested, and ViewModels are thin wrappers.

### No DataStore/LoginPreferences Failure Tests

- What's not tested: What happens when `LoginPreferences.setLoggedIn()` throws `IOException` (line 136 of `LoginViewModel.kt`). The ViewModel catches this and sets an error, but the error path is never exercised in tests.
- Files: `app/src/main/java/com/pokect/bank/kids/data/preferences/LoginPreferences.kt`, `app/src/test/java/com/pokect/bank/kids/data/preferences/LoginPreferencesTest.kt`
- Risk: DataStore failures are silently handled incorrectly, potentially leaving the app in an inconsistent state.
- Priority: Low — DataStore failures are rare on production devices, but the error handling code should be verified.

### No Test for Concurrent Repository Operations

- What's not tested: `AppRepository` has no mutex-based synchronization anymore (the old `Mutex` locks were removed during the Room migration). Concurrent deposits or reward redemptions could race on the same user row.
- Files: `app/src/main/java/com/pokect/bank/kids/data/repository/AppRepository.kt`
- Risk: If two coroutines call `deposit()` simultaneously, both read the same `userDao.getUser()` snapshot, both compute `currentUser.balance + amount`, and the second write overwrites the first. The user loses coins from the first deposit.
- Priority: High — this is a data integrity bug. Room's `@Update` with `@Transaction` annotation or explicit mutex locking should be re-added.

---

*Concerns audit: 2026-05-21*
