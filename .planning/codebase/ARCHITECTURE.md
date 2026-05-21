<!-- refreshed: 2026-05-21 -->
# Architecture

**Analysis Date:** 2026-05-21

## System Overview

```text
┌──────────────────────────────────────────────────────────────────────────────┐
│                              UI Layer (Compose)                              │
├─────────────┬──────────┬───────────┬──────────┬───────────┬─────────────────┤
│  HomeScreen │GoalsScreen│MissionsS. │RewardsS. │RankingS.  │  LoginScreen    │
│ `ui/screens │`ui/screens│`ui/screen │`ui/screen│`ui/screen │ `ui/screens/    │
│  /home/`    │ /goals/`  │s/missions│s/rewards/│s/ranking/ │  login/`        │
│             │           │ /`        │          │           │                 │
│  +HomeVM    │+GoalsVM   │+MissionsVM│+RewardsVM│+RankingVM │ +LoginVM       │
│  +HomeUiSt. │+GoalsUiSt │+MissionsU │+RewardsUi│+RankingUi │ +LoginUiSt     │
│             │           │ iSt       │ St       │ St        │                 │
└──────┬──────┴─────┬─────┴─────┬─────┴────┬─────┴─────┬─────┴────────┬────────┘
       │            │           │          │           │              │
       │   via ViewModelProvider.Factory   │           │    via LoginPreferences
       │     (HomeVMFactory, etc.)         │           │    (DataStore)
       ▼            ▼           ▼          ▼           ▼              ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                          Data Layer                                          │
│  `data/repository/AppRepository.kt`                                          │
│  ┌───────────────────────────────────────────────────────────────────┐       │
│  │  StateFlow<User>     StateFlow<List<SavingsGoal>>                 │       │
│  │  StateFlow<Mission>  StateFlow<List<Reward>>                      │       │
│  │  StateFlow<FamilyM.> StateFlow<List<Transaction>>                 │       │
│  │  StateFlow<Challenge> SharedFlow<RepositoryEvent>                 │       │
│  └───────────────────────────────────────────────────────────────────┘       │
│       │                                                                      │
│       │  Bridges Room DAO Flows → StateFlows (init block collectors)         │
│       ▼                                                                      │
│  ┌───────────────────────────────────────────────────────────────────┐       │
│  │  Room DAOs (7 interfaces)                                         │       │
│  │  `data/local/dao/`                                                │       │
│  │  UserDao | TransactionDao | SavingsGoalDao | MissionDao           │       │
│  │  RewardDao | FamilyMemberDao | WeeklyChallengeDao                 │       │
│  └───────────────────────────────────────────────────────────────────┘       │
│  ┌───────────────────────────────────────────────────────────────────┐       │
│  │  DataStore Preferences                                            │       │
│  │  `data/preferences/LoginPreferences.kt`                           │       │
│  │  LoginPrefsData (isLoggedIn, selectedAvatar)                      │       │
│  └───────────────────────────────────────────────────────────────────┘       │
└──────────────────────────────────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│  SQLite Database (Room)                                                       │
│  `pokectbank.db` — 7 tables: user, transaction, savings_goal, mission,        │
│  reward, family_member, weekly_challenge                                      │
└──────────────────────────────────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│  DI Layer                                                                     │
│  `di/AppContainer.kt` — singleton object                                      │
│  Creates: PokectBankDatabase, AppRepository, LoginPreferences                 │
│  Reset: resetRepository() → clears login + drops DB instance                  │
└──────────────────────────────────────────────────────────────────────────────┘
```

## Component Responsibilities

| Component | Responsibility | File |
|-----------|----------------|------|
| MainActivity | App entry point, Scaffold + bottom nav, auth gate (checking→logged-out→logged-in), initializes AppContainer with context | `app/src/main/java/com/pokect/bank/kids/MainActivity.kt` |
| PokectBankNavGraph | Route definitions, ViewModel creation via factories, screen transitions with enter/exit animations | `app/src/main/java/com/pokect/bank/kids/ui/navigation/NavGraph.kt` |
| Screen | Sealed class defining 6 routes with icons and labels, bottom nav tab list | `app/src/main/java/com/pokect/bank/kids/ui/navigation/Screen.kt` |
| AppContainer | Manual DI singleton — creates Room database, AppRepository with all DAOs, LoginPreferences, reset support | `app/src/main/java/com/pokect/bank/kids/di/AppContainer.kt` |
| AppRepository | All CRUD operations, bridges Room Flows to StateFlows, event broadcast via SharedFlow, validation + transaction recording | `app/src/main/java/com/pokect/bank/kids/data/repository/AppRepository.kt` |
| RepositoryEvent | Sealed interface for cross-screen event types (UserUpdated, MissionsChanged, GoalsChanged, etc.) | `app/src/main/java/com/pokect/bank/kids/data/repository/RepositoryEvent.kt` |
| PokectBankDatabase | Room database — 7 entities, 7 DAOs, seed data callback, in-memory test instance support | `app/src/main/java/com/pokect/bank/kids/data/local/PokectBankDatabase.kt` |
| PokectBankConverters | Room TypeConverters — enums (MissionStatus, MissionDifficulty, MissionCategory, TransactionType, GoalColor), List<String>, Instant | `app/src/main/java/com/pokect/bank/kids/data/local/PokectBankConverters.kt` |
| LoginPreferences | DataStore wrapper — persists login state (isLoggedIn, selectedAvatar), clearLogin for logout | `app/src/main/java/com/pokect/bank/kids/data/preferences/LoginPreferences.kt` |
| HomeViewModel | Home screen state: balance, level, missions, transactions, deposit flow, confetti, history expand, reset flow | `app/src/main/java/com/pokect/bank/kids/ui/screens/home/HomeViewModel.kt` |
| GoalsViewModel | Goals CRUD, allocate funds to goal, add-goal modal flow | `app/src/main/java/com/pokect/bank/kids/ui/screens/goals/GoalsViewModel.kt` |
| MissionsViewModel | Mission list with category filter (All/Daily/Weekly/Special) | `app/src/main/java/com/pokect/bank/kids/ui/screens/missions/MissionsViewModel.kt` |
| RewardsViewModel | Reward shop, redeem flow, coin affordability calculation, redemption confirmation + confetti | `app/src/main/java/com/pokect/bank/kids/ui/screens/rewards/RewardsViewModel.kt` |
| RankingViewModel | Family leaderboard sorted by XP, weekly challenge join | `app/src/main/java/com/pokect/bank/kids/ui/screens/ranking/RankingViewModel.kt` |
| LoginViewModel | Auth flow (login/signup tabs, 2-step wizard with avatar selection, DataStore persistence) | `app/src/main/java/com/pokect/bank/kids/ui/screens/login/LoginViewModel.kt` |
| PokectBankTheme | Material 3 theme: custom colors, Nunito typography, kid-friendly shapes, animation specs | `app/src/main/java/com/pokect/bank/kids/ui/theme/Theme.kt` |

## Pattern Overview

**Overall:** MVVM (Model-View-ViewModel) with Repository pattern

**Key Characteristics:**
- Each screen is a triad: `*Screen.kt` (Composable) + `*ViewModel.kt` + `*UiState` (immutable data class) — See "Key Abstractions" for where UiState classes live
- ViewModels expose a single `StateFlow<UiState>` consumed via `collectAsStateWithLifecycle()`
- All state mutations flow through `MutableStateFlow.update {}` in ViewModels (atomic transitions)
- Repository bridges Room DAO `Flow<>` emissions to `MutableStateFlow<>` via collector coroutines in `init {}`
- Repository exposes `SharedFlow<RepositoryEvent>` for cross-screen change notifications — ViewModels listen via `distinctUntilChanged().collect {}`
- All repository writes are `suspend` functions that return `Result<Unit>`, performing validation → DAO writes → event broadcast
- Room provides SQLite persistence with seed data via `SeedDatabaseCallback.onCreate()`
- DataStore stores login session (boolean + avatar index) across app restarts
- Manual DI via `object AppContainer` (no Hilt, no Koin)
- Each ViewModel has its own `ViewModelProvider.Factory` receiving the repository or LoginPreferences

## Layers

**UI Layer:**
- Purpose: Jetpack Compose UI rendering, user interaction handling
- Location: `app/src/main/java/com/pokect/bank/kids/ui/`
- Contains: 6 feature screens, shared components (`AvatarDisplay`), navigation (`NavGraph`, `Screen`), theme (`Color`, `Type`, `Shape`, `Theme` with `AnimationSpecs`)
- Depends on: Data layer (`data/`) via ViewModels
- Used by: MainActivity sets the root composable

**Data Layer:**
- Purpose: Single source of truth for all application state, business logic, seed data, persistence
- Location: `app/src/main/java/com/pokect/bank/kids/data/`
- Contains: Models (7 Room entities + `AvatarOption` in `data/models/`), Room DAOs (`data/local/dao/`), Database + Converters (`data/local/`), Repository (`data/repository/AppRepository.kt`), Events (`data/repository/RepositoryEvent.kt`), DataStore preferences (`data/preferences/LoginPreferences.kt`)
- Depends on: Kotlin stdlib, kotlinx.coroutines, Room, DataStore
- Used by: ViewModels (direct injection via factories) and LoginViewModel (DataStore directly)

**DI Layer:**
- Purpose: Provide singleton repository, database, and preferences instances to ViewModels
- Location: `app/src/main/java/com/pokect/bank/kids/di/AppContainer.kt`
- Contains: `object AppContainer` with lazy `AppRepository`, `PokectBankDatabase`, and `LoginPreferences`
- Depends on: Data layer
- Used by: NavGraph (passes repository to ViewModel factories), MainActivity (passes LoginPreferences to LoginViewModel)

**Utility Layer:**
- Purpose: Shared formatting functions
- Location: `app/src/main/java/com/pokect/bank/kids/util/`
- Contains: `CurrencyFormatter.kt` (pt-BR currency formatting), `DateFormatter.kt` (relative date labels: "Hoje"/"Ontem"/"dd/MM")
- Depends on: Java stdlib (java.text.NumberFormat, java.time.LocalDate)
- Used by: UI components

**Theme Layer:**
- Purpose: Material 3 design system configuration
- Location: `app/src/main/java/com/pokect/bank/kids/ui/theme/`
- Contains: `Color.kt` (light scheme + gradient presets), `Type.kt` (Nunito font family), `Shape.kt` (rounded shapes), `Theme.kt` (theme composable + `AnimationSpecs` object)
- Depends on: Compose Material 3
- Used by: All screens via `PokectBankTheme {}`

## Data Flow

### Primary Request Path (Deposit)

1. User taps deposit button → `HomeScreen` calls `viewModel.deposit(amount)` (`ui/screens/home/HomeScreen.kt`)
2. `HomeViewModel.deposit()` launches coroutine → calls `repository.deposit(amount)` (`ui/screens/home/HomeViewModel.kt:109-129`)
3. `AppRepository.deposit()` validates → reads current user via `userDao.getUser()` → computes updated user → `userDao.updateUser(updatedUser)` + `familyMemberDao.syncCurrentUser(...)` + `transactionDao.insertTransaction(...)` → broadcasts `UserUpdated` + `TransactionsChanged` events (`data/repository/AppRepository.kt:149-186`)
4. Room auto-emits new Flow values → AppRepository's init collectors update `_user` and `_transactions` StateFlows
5. `HomeViewModel` event collector receives `UserUpdated` → calls `refreshUserData()` → reads from repository → updates `_uiState`
6. Compose UI recomposes with new balance, transactions, XP values

### Goal Allocation Flow

1. User allocates coins to a goal → `GoalsViewModel.allocateToGoal(goalId, amount)` (`ui/screens/goals/GoalsViewModel.kt:68-77`)
2. `AppRepository.allocateToGoal()` validates → checks balance sufficiency → `userDao.updateUser(...)` (deduct balance) + `savingsGoalDao.updateGoal(...)` (add to goal) + `transactionDao.insertTransaction(...)` → broadcasts `GoalsChanged` + `UserUpdated` + `TransactionsChanged` (`data/repository/AppRepository.kt:260-296`)
3. `GoalsViewModel` event collector receives → calls `loadGoalsAndUser()` → reads fresh goals + user → updates `_uiState`

### Reward Redemption Flow

1. User taps reward → `RewardsViewModel.redeemReward(rewardId)` → shows confirmation modal (`ui/screens/rewards/RewardsViewModel.kt:104-112`)
2. User confirms → `confirmRedemption()` validates coins sufficiency → calls `repository.redeemReward(rewardId)` (`ui/screens/rewards/RewardsViewModel.kt:114-152`)
3. `AppRepository.redeemReward()` validates → reads reward + user → checks coins → `userDao.updateUser(...)` (deduct coins) + `rewardDao.updateReward(...)` (mark redeemed) + `familyMemberDao.syncCurrentUser(...)` + `transactionDao.insertTransaction(...)` → broadcasts `UserUpdated` + `RewardsChanged` + `TransactionsChanged` (`data/repository/AppRepository.kt:221-258`)
4. `RewardsViewModel` event collector receives `UserUpdated`/`RewardsChanged` → refreshes coins + rewards → confetti animation

### Cross-Screen Event Propagation

- **StateFlows** for persistent data (user, missions, goals, rewards, transactions, family members, weekly challenges) — all seven entities bridged from Room → MutableStateFlow
- **SharedFlow** for transient change notifications (`RepositoryEvent` sealed interface with 6 event types)
- ViewModels selectively react to relevant events using `distinctUntilChanged().collect {}` with `when` branches
- Compose `collectAsStateWithLifecycle()` for lifecycle-aware consumption in UI
- ViewModel-internal `MutableStateFlow.update {}` for atomic state transitions

### Login/Auth Flow (DataStore override path)

1. `MainActivity` creates `LoginViewModel(AppContainer.loginPreferences)` directly (not via NavGraph factory)
2. `LoginViewModel.init {}` collects `LoginPreferences.loginState` Flow — populates `isLoggedIn`, `selectedAvatar`, and sets `isCheckingLogin = false`
3. On login, `LoginViewModel.login()` calls `loginPrefs.setLoggedIn(avatar)` → DataStore persists → ViewModel sets `isLoggedIn = true`
4. `MainActivity` reads `authState.isLoggedIn` — if true, `NavHost` starts at `Screen.Home`; if false, starts at `Screen.Login`
5. On logout, `AppContainer.resetRepository()` clears DataStore + drops DB singleton → next launch re-seeds

**State Management:**
- Immutable `data class` UiState per screen, served via `StateFlow`
- Room is the source of truth; AppRepository's init collectors bridge Room Flows to `MutableStateFlow` so the old `StateFlow` API surface is preserved
- No `by` delegates or mutable shared state outside ViewModels and AppRepository
- Per-entity StateFlows in AppRepository update atomically via `.value = it` assignment (no lock needed for reads — Room Flow collection is the synchronization point)

## Key Abstractions

**UiState data classes:**
- Purpose: Immutable snapshot of all data a screen needs to render
- Examples:
  - `HomeUiState` — `ui/screens/home/HomeUiState.kt` (standalone file, includes helper functions `getGoalProgressPercent()`, `getXpProgressPercent()`, `getLevelTitle()`)
  - `GoalsUiState` — defined inline in `GoalsViewModel.kt` line 16
  - `MissionsUiState` — defined inline in `MissionsViewModel.kt` line 36
  - `RewardsUiState` — defined inline in `RewardsViewModel.kt` line 27
  - `RankingUiState` — defined inline in `RankingViewModel.kt` line 26
  - `LoginUiState` — defined inline in `LoginViewModel.kt` line 15
- Pattern: Data class with default values, exposed as `StateFlow<>` from ViewModel

**ViewModelFactory:**
- Purpose: Inject repository/preferences into ViewModels without DI framework
- Examples: `HomeViewModelFactory` (`ui/screens/home/HomeViewModelFactory.kt`), `GoalsViewModelFactory`, `MissionsViewModelFactory`, `RewardsViewModelFactory`, `RankingViewModelFactory`
- Pattern: Implements `ViewModelProvider.Factory`, takes `AppRepository` in constructor, checks `modelClass.isAssignableFrom`, uses `@Suppress("UNCHECKED_CAST")`

**AnimationSpecs:**
- Purpose: Centralized animation constants for UI consistency
- Location: `ui/theme/Theme.kt` (lines 89-127)
- Contains: `float`, `pulseGlow`, `bounceIn`, `coinSpin`, `wiggle`, `screenTransition`, `CONFETTI_DURATION`, `CONFETTI_PARTICLE_COUNT`
- Usage: Referenced by ViewModels for timing (`delay(AnimationSpecs.CONFETTI_DURATION.toLong())`)

**Repository DAO Bridge pattern:**
- Purpose: Convert Room's `Flow<T>` to `MutableStateFlow<T>` while preserving existing StateFlow API
- Location: `data/repository/AppRepository.kt` init block (lines 98-136)
- Pattern: `repositoryScope.launch { dao.observeXxx().catch{...}.collect { _stateFlow.value = it } }`
- Seven parallel collectors, each wrapped in `catch` for error resilience

**TypeConverters:**
- Purpose: Convert Kotlin enums and complex types for SQLite storage
- Location: `data/local/PokectBankConverters.kt`
- Covers: `MissionStatus`, `MissionDifficulty`, `MissionCategory`, `TransactionType`, `GoalColor` (enum→String), `List<String>` (comma-separated), `Instant` (epoch millis)

## Entry Points

**MainActivity:**
- Location: `app/src/main/java/com/pokect/bank/kids/MainActivity.kt`
- Triggers: Android system launch (declared in `AndroidManifest.xml` as LAUNCHER)
- Responsibilities: Initializes `AppContainer` with `applicationContext`, creates `PokectBankTheme`, manages `LoginViewModel` auth state, renders `Scaffold` with conditional bottom nav (hidden on login), hosts `PokectBankNavGraph`
- Auth gate: Three-state (checking → logged-out → logged-in) with loading spinner

**AndroidManifest:**
- Location: `app/src/main/AndroidManifest.xml`
- Config: `launchMode="singleTask"`, `screenOrientation="portrait"`, targetSdk 36, minSdk 26
- Single activity app — no services, broadcast receivers, or content providers

**Navigation start destination:**
- Conditional: `Screen.Login` if not authenticated, `Screen.Home` if logged in
- Located in `PokectBankNavGraph()` composable in `NavGraph.kt` line 38

**Bottom navigation:**
- 5 tabs: Home, Goals, Missions, Rewards, Ranking
- Login screen excluded from bottom nav (shown only when unauthenticated)
- Missions tab has a red dot indicator

## Architectural Constraints

- **Threading:** All repository operations run on `repositoryScope` (Dispatchers.IO). Room DAO calls are `suspend` and internally use Dispatchers.IO. ViewModel coroutines run on `viewModelScope` (Dispatchers.Main by default in Compose). DataStore writes are suspend functions on IO. No explicit `withContext` switching in ViewModels.
- **Global state:** `AppContainer` is a Kotlin `object` (singleton) — one `AppRepository`, one `PokectBankDatabase`, one `LoginPreferences` instance for the entire app process. Database uses `@Volatile` + `synchronized` for thread-safe singleton.
- **Circular imports:** No circular dependencies detected. Data layer has no dependency on UI layer. DI layer sits between data and UI. Utility layer is a leaf dependency.
- **Orientation:** Portrait-only (`screenOrientation="portrait"` in manifest).
- **Dark theme:** Not supported for v1. Only light color scheme defined in `PokectBankLightColorScheme`.
- **Data persistence:** Room SQLite database (`pokectbank.db`) with seed data callback. DataStore Preferences for login state. Data survives app restarts. Reset clears both.
- **Room autoGenerate PKs:** All entities use `@PrimaryKey(autoGenerate = true) val uid: Long = 0`, meaning Room assigns auto-increment IDs. The `id: String` field serves as the business key with a unique index.
- **API contract stability:** Repository public method signatures (`deposit`, `allocateToGoal`, `redeemReward`, etc.) are frozen — ViewModels must not be changed.

## Anti-Patterns

### God Repository

**What happens:** `AppRepository` handles all 7 entities — users, transactions, goals, missions, rewards, family members, weekly challenges. It's 359 lines long and contains validation logic, event broadcasting, and DAO bridging for every entity.
**Why it's wrong:** As the app grows, this class will become harder to maintain. A change to mission logic requires touching a file that also contains reward redemption and weekly challenge logic. Coupling increases risk of regressions.
**Do this instead:** Split into entity-specific repositories (e.g., `UserRepository`, `GoalRepository`) with a coordinating service if cross-entity operations are needed. The DAO interfaces are already separated — the repository should mirror this.

### UiState Classes Defined Inside ViewModels

**What happens:** Five of six UiState data classes (`GoalsUiState`, `MissionsUiState`, `RewardsUiState`, `RankingUiState`, `LoginUiState`) are defined inside their ViewModel files rather than in dedicated files. Only `HomeUiState` has its own file.
**Why it's wrong:** Makes it harder to test UiState independently, harder to find with IDE navigation, and inconsistent with the `HomeUiState` pattern.
**Do this instead:** Extract each UiState to its own file (e.g., `GoalsUiState.kt`) matching the `HomeUiState.kt` pattern.

### No Interface for Repository

**What happens:** ViewModels depend directly on the concrete `AppRepository` class. No interface or abstraction exists.
**Why it's wrong:** Makes unit testing ViewModels harder — they need a real Room database or a complex mock. The `AppRepository` constructor requires 7 DAOs and the database instance, making in-memory test setup verbose.
**Do this instead:** Extract an `AppRepository` interface with the public methods. ViewModels depend on the interface. Tests can use a fake implementation. The existing `AppRepository` becomes `RoomAppRepository : AppRepository`.

### UiState models intermixed with ViewModel logic

**What happens:** `RewardsViewModel.kt` defines `RewardUi` and `RewardsUiState` data classes alongside the ViewModel class and its methods. `RankingViewModel.kt` similarly defines `RankingMember` and `RankingUiState`.
**Why it's wrong:** Mixing data model definitions with logic makes files grow large (RewardsViewModel.kt is 171 lines), reduces discoverability, and makes it unclear where data model boundaries lie.
**Do this instead:** Extract `RewardUi`, `RankingMember`, and their UiState classes into dedicated files in the same package.

## Error Handling

**Strategy:** Repository write methods return `Result<Unit>` — success or failure with exception. ViewModels extract error messages via `result.exceptionOrNull()?.message` with Portuguese fallback messages.

**Patterns:**
- Repository: Guard clauses with early `return Result.failure(IllegalArgumentException(...))` for invalid input
- Repository: State errors return `Result.failure(IllegalStateException(...))` for insufficient coins/balance, goal not found
- Repository: `CancellationException` always re-thrown (standard coroutine pattern) — all write methods have `catch (e: CancellationException) { throw e }`
- Repository: Generic `catch (e: Exception) { Result.failure(e) }` fallback
- ViewModels: Check `result.isSuccess` — if true, update state; if false, set `error` field in UiState
- UI: LoginScreen uses `LaunchedEffect(uiState.error)` for snackbar; HomeScreen displays error via `uiState.error` field; other screens display inline

## Cross-Cutting Concerns

**Logging:** Only `android.util.Log.w()` used — for non-critical events (dropped event buffer in AppRepository, failed login persistence in AppContainer). No info/debug logging. No custom logging abstraction.
**Validation:** Input validation in repository write methods only. LoginViewModel performs its own validation (empty name/email/password checks) with inline Portuguese messages.
**Authentication:** Simplified "auth" via DataStore — boolean `is_logged_in` flag + `selected_avatar` int. No real passwords, tokens, or sessions. Login is a simulation with 1.5s delay.
**Reset mechanism:** `AppContainer.resetRepository()` clears DataStore login state, nullifies `_repository` and `_database` references, and the database file is deleted by `PokectBankDatabase.resetInstance()`. On next access, lazy initialization re-creates everything with fresh seed data.

---

*Architecture analysis: 2026-05-21*
