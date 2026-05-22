<!-- GSD:project-start source:PROJECT.md -->
## Project

**PokectBank**

Um app Android de banco infantil (estudo pessoal) em Kotlin/Jetpack Compose que simula educação financeira para crianças, com cofrinho, metas de economia, missões, recompensas e ranking familiar. Atualmente os dados são 100% em memória e se perdem ao reiniciar.

**Core Value:** Criança consegue simular depósitos, metas e recompensas com os dados persistindo entre sessões.

### Constraints

- **API do repositório:** Métodos públicos (deposit, allocateToGoal, redeemReward, etc.) não podem mudar de assinatura — ViewModels não serão alterados
- **Stack:** Room + DataStore Preferences — sem bibliotecas externas adicionais além das oficiais Android Jetpack
- **Seed data:** Manter exatamente os mesmos dados de exemplo do AppRepository atual (usuário demo com R$47.50, 4 metas, 5 missões, 6 recompensas, 5 membros da família)
- **Plataforma:** Android 8.0+ (minSdk 26), portrait only
- **Idioma:** Continuar em pt-BR (strings hardcoded não serão migradas nesta fase)
<!-- GSD:project-end -->

<!-- GSD:stack-start source:.planning/codebase/STACK.md -->
## Technology Stack

### Languages
- Kotlin 2.1.0 — All application source code under `app/src/main/java/com/pokect/bank/kids/`
- Java 17 (compile target) — JVM target for Kotlin compilation; no hand-written Java source files
- Gradle Kotlin DSL — Build scripts in `build.gradle.kts` and `app/build.gradle.kts`

### Runtime
- Android — `minSdk = 26` (Android 8.0 Oreo), `targetSdk = 36`, `compileSdk = 36`
- Gradle 9.3.1 (wrapper)
- Version Catalog: `gradle/libs.versions.toml` (centralized dependency management)

### Frameworks
**Core UI:**
- Jetpack Compose BOM 2024.12.01 — UI framework (Material 3, icons extended, ui-text 1.11.1)
- AndroidX Navigation — `navigation-compose` 2.8.5 (6 routes in `NavGraph.kt`)
- AndroidX Lifecycle — 2.8.7 (`lifecycle-runtime-compose`, `lifecycle-viewmodel-compose`)
- AndroidX Activity — `activity-compose` 1.9.3 (`ComponentActivity`)

**Persistence:**
- Room 2.8.4 — SQLite ORM (`room-runtime`, `room-ktx`, `room-compiler` via KSP)
  - 7 entities: `User`, `Transaction`, `SavingsGoal`, `Mission`, `Reward`, `FamilyMember`, `WeeklyChallenge`
  - 7 DAOs with Flow-based reactive observation
  - Database: `pokectbank.db` (version 1, `fallbackToDestructiveMigration()`)
  - Seed data via `SeedDatabaseCallback` with SQL INSERT on create
- DataStore Preferences 1.2.1 — Key-value storage for login state (`login_state`)
  - Properties: `is_logged_in` (boolean), `selected_avatar` (int)
  - Wrapper: `data/preferences/LoginPreferences.kt`

**Code Generation:**
- KSP 2.1.0-1.0.29 — Kotlin Symbol Processing for Room annotation processing

**Build:**
- Android Gradle Plugin (AGP) 8.7.3
- Kotlin Compose Compiler Plugin — `org.jetbrains.kotlin.plugin.compose` 2.1.0

**Testing:**
| Framework | Version | Purpose |
|---|---|---|
| JUnit | 4.13.2 | Unit test runner |
| Kotlin Test | 2.1.0 | Assertion library (preferred over JUnit assertions) |
| Kotlinx Coroutines Test | 1.8.1 | `runTest`, `StandardTestDispatcher`, `UnconfinedTestDispatcher` |
| Compose UI Test JUnit4 | (via BOM) | Compose UI testing (not actively used yet) |
| Robolectric | 4.14.1 | Android framework simulation on JVM (no emulator needed) |
| AndroidX Test Core | 1.6.1 | `ApplicationProvider` for test contexts |

### Key Dependencies
| Dependency | Version | Purpose |
|---|---|---|
| `compose-bom` | 2024.12.01 | Manages all Compose library versions |
| `room-runtime` + `room-ktx` | 2.8.4 | Room database framework + coroutines extensions |
| `room-compiler` (KSP) | 2.8.4 | Annotation processor for Room entities/DAOs |
| `datastore-preferences` | 1.2.1 | Key-value persistence for login state |
| `navigation-compose` | 2.8.5 | In-app navigation between 6 screens |
| `lifecycle-viewmodel-compose` | 2.8.7 | ViewModel integration with Compose |
| `activity-compose` | 1.9.3 | Compose host activity |

- No network libraries (Retrofit, OkHttp, Ktor — none detected)
- No dependency injection framework (Hilt, Koin, Kodein — none detected)
- Manual DI via singleton `AppContainer` object at `app/src/main/java/com/pokect/bank/kids/di/AppContainer.kt`
- State management via Room → StateFlow bridging in `AppRepository.init{}` + SharedFlow events

### Configuration
- Build config centralized in `gradle/libs.versions.toml` (version catalog)
- `build.gradle.kts` (root): Plugin declarations only
- `app/build.gradle.kts`: SDK versions, Compose feature flag, KSP config, dependencies
- `settings.gradle.kts`: Repositories (Google, Maven Central), project name "PokectBank"
- `gradle.properties`: JVM args (2048m), Kotlin official style, AndroidX enabled
- `app/proguard-rules.pro`: Keeps `@Composable` methods (minification disabled in release)
- Room config: `PokectBankDatabase.kt` (version 1, `exportSchema = false`)
- DataStore config: `LoginPreferences.kt` (store name `login_state`)

### Platform Requirements
- Android 8.0+ (API 26+) device
- Portrait orientation only (`screenOrientation="portrait"`)
- No network permission required (fully offline, local SQLite + DataStore)
- JDK 17+ for development
- Android SDK Platform 36
- Gradle 9.3.1 (auto-downloaded via wrapper)
<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->
## Conventions

## Naming Patterns
- PascalCase for all Kotlin source files. Examples: `HomeViewModel.kt`, `HomeScreen.kt`, `AppRepository.kt`, `CurrencyFormatter.kt`
- Screens and their supporting classes share the screen name prefix: `HomeScreen.kt`, `HomeViewModel.kt`, `HomeUiState.kt`, `HomeViewModelFactory.kt`
- Test files: `*Test.kt` suffix, mirroring the class under test. Example: `AppRepositoryTest.kt`, `HomeViewModelTest.kt`
- Build files: `build.gradle.kts` (Kotlin DSL)
- Source root: `app/src/main/java/com/pokect/bank/kids/`
- Feature/domain grouping by directory: `data/models/`, `data/repository/`, `ui/screens/<feature>/`, `ui/navigation/`, `ui/theme/`, `ui/components/`, `util/`, `di/`
- Per-screen subdirectories under `ui/screens/<feature>/` with a `components/` subdirectory for screen-local composables
- camelCase. Examples: `formatCurrency()`, `getGoalProgressPercent()`, `showDepositModal()`, `loadInitialData()`
- Top-level utility functions use camelCase: `formatCurrency(amount: Double)`, `formatDateLabel(dateStr: String)`, `getGroupedTransactions(transactions: List<Transaction>)`
- camelCase for local variables and parameters. Examples: `selectedAvatar`, `depositAmount`, `showConfetti`
- Private backing fields use underscore prefix convention: `_uiState` → `uiState`, `_user` → `user`, `_events` → `events`
- No Hungarian notation or type prefixes
- PascalCase for all types. Examples: `HomeUiState`, `MissionsUiState`, `RankingUiState`, `MissionStatus`, `MissionCategory`
- UI state classes use `*UiState` suffix convention (e.g., `HomeUiState`, `GoalsUiState`, `RewardsUiState`)
- Repository event sealed interface: members use past-tense object names (`UserUpdated`, `GoalsChanged`)
- PascalCase for singleton objects and companion objects. Examples: `AppContainer`, `AnimationSpecs`, `GradientPresets`, `KidShapes`
- Companion objects hold static lists: `AvatarOption.options`, `Screen` sealed class with object members
## Code Style
- Kotlin official code style (`kotlin.code.style=official` in `gradle.properties`)
- Column width: no explicit limit configured
- Indentation: 4 spaces (Kotlin convention)
- Trailing commas: not used in data class constructors or function calls
- Line comments use `//` with an initial space for inline comments; `// ====================` separators for code sections
- No dedicated Kotlin linter configured (no detekt, ktlint, or ktslint config files detected)
- No `.editorconfig` file present
- `@Suppress("UNCHECKED_CAST")` used in ViewModelFactory classes where unchecked casts are unavoidable
- Section separator comments using `// === Section Name ===` with multiline equals bars
- Model/UiState fields: no doc comments (self-documenting via naming)
- Public repository methods: no doc comments
- Theme files use `// === Section Name ===` block comments with detailed inline comments explaining hex colors
- Compose animation specs: extensive usage-example comments in `Theme.kt` (`AnimationSpecs` object)
- `/** */` JSDoc-style block comments used for top-level utility functions: `formatDateLabel()` has KDoc describing return values
## Import Organization
- No path aliases defined. Full package paths used in all imports.
- Explicit FQN used in `NavGraph.kt` for type annotations within composable blocks when needed
## Error Handling
- Repository write operations return `Result<Unit>` with typed failures
- Guard clauses with early returns: `if (amount < 1.0) return@withLock Result.failure(IllegalArgumentException(...))`
- Validation errors: `Result.failure(IllegalArgumentException("message"))` for invalid input (e.g., negative amounts, blank titles)
- State errors: `Result.failure(IllegalStateException("message"))` for invalid state (e.g., insufficient coins, goal not found)
- `CancellationException` always re-thrown: `catch (e: CancellationException) { throw e }` — standard coroutine pattern present in `AppRepository.kt` methods (lines 158-159, 284-285)
- Generic catch-all fallback: `catch (e: Exception) { Result.failure(e) }`
- ViewModels extract error messages via `result.exceptionOrNull()?.message ?: "fallback message"`
- UI errors surfaced via `error: String?` field in UiState data classes
- Error messages in Portuguese: `"Erro ao guardar moedinhas"`, `"Moedas insuficientes"`, `"Erro ao resgatar prêmio"`
- Login validation uses inline Portuguese messages: `"Digite seu e-mail para continuar"`
## Logging
- Warning level for non-critical operational events: `Log.w("AppRepository", "Dropped $event — buffer full")` in `AppRepository.kt` line 63
- No debug or info logging used elsewhere
- No custom logging abstraction
- ViewModels do not log (errors go to UiState)
## Comments
- Architectural decisions: `// Lock ordering (MUST be enforced by all methods acquiring multiple mutexes)` in `AppRepository.kt`
- Section organization: `// === Write operations ===`, `// === Seed data ===`
- Thread safety: `// Read reward outside locks — StateFlow snapshot is safe for lookup`
- API intent documentation: `Theme.kt` contains extensive usage-example comments for animation specs
- Test placeholders: `ComponentsTest.kt` explains why Compose UI tests are not present
- Used sparingly: `formatDateLabel()` has `/** */` block describing return values (`app/src/main/java/com/pokect/bank/kids/util/DateFormatter.kt` line 6)
- Not used on data classes, ViewModels, or repository methods
## Function Design
- Most functions are small (1-20 lines). Repository methods are longer (20-50 lines) due to validation + mutation + transaction recording logic.
- ViewModels: methods typically 5-15 lines
- Screens/Composables: `HomeScreen.kt` is 174 lines, component-level composables tend to be 50-150 lines
- ViewModels accept `AppRepository` as a constructor dependency (manual DI)
- Compose functions use named parameters with `Modifier` as a trailing default parameter: `modifier: Modifier = Modifier`
- Callback lambdas use trailing placement and default values: `onNavigateToMissions: () -> Unit = {}`
- Boolean flags used in UiState data classes (`showDepositModal: Boolean = false`)
- Repository writes: `Result<Unit>` (success/failure with no payload)
- Repository reads: `suspend fun getX(): T` (returns snapshot values directly)
- ViewModel public methods: `Unit` (no return value — state exposed via StateFlow)
## Module Design
- No explicit module or visibility modifiers; all classes/data classes/functions use `public` (Kotlin default)
- Top-level functions (utilities) are public: `formatCurrency()`, `formatDateLabel()`
- Private backing fields for mutable state: `private val _uiState = MutableStateFlow(...)`
- Not used. Each file exports its own class. No `index.kt` or aggregator files.
- ViewModel state: `private val _uiState = MutableStateFlow(...)` paired with `val uiState: StateFlow<...> = _uiState.asStateFlow()`
- Repository state: same `_` backing field → `asStateFlow()` pattern for all data stores
- UI collection: `val uiState by viewModel.uiState.collectAsStateWithLifecycle()`
## ViewModel Factory Pattern

Each ViewModel has a dedicated `*ViewModelFactory` class implementing `ViewModelProvider.Factory`:
- `HomeViewModelFactory.kt` at `ui/screens/home/HomeViewModelFactory.kt`
- `GoalsViewModelFactory.kt` at `ui/screens/goals/GoalsViewModelFactory.kt`
- `MissionsViewModelFactory.kt` at `ui/screens/missions/MissionsViewModelFactory.kt`
- `RewardsViewModelFactory.kt` at `ui/screens/rewards/RewardsViewModelFactory.kt`
- `RankingViewModelFactory.kt` at `ui/screens/ranking/RankingViewModelFactory.kt`

Factory pattern (`HomeViewModelFactory.kt`):
```
class HomeViewModelFactory(
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```
Exception: `LoginViewModel` is created inline in `NavGraph.kt` and `MainActivity.kt` using anonymous `ViewModelProvider.Factory` since it takes `LoginPreferences` instead of `AppRepository`.

## Dependency Injection

**Pattern:** Manual DI via `object AppContainer` (Kotlin singleton) at `di/AppContainer.kt`

**Characteristics:**
- `AppContainer.init(context: Context)` must be called first (from `MainActivity.onCreate()`) to set the application context
- Database uses lazy initialization with reset support (private nullable `_database` with getter that rebuilds if null)
- Repository uses lazy initialization with reset support (private nullable `_repository` with getter that rebuilds if null)
- `loginPreferences` uses `by lazy` delegation
- `resetRepository()` nulls out both `_database` and `_repository`, triggers cleanup coroutine for DataStore
- **⚠️ Risk:** Getters on `_database`/`_repository` are NOT thread-safe (`!!` force-unwrap, no `@Volatile`, no `synchronized`). Two threads could create duplicate instances. See CONCERNS section.

## Room DAO Pattern

DAOs are `interface` with `@Dao` annotation:
```
@Dao
interface UserDao {
    @Query("SELECT * FROM user LIMIT 1")
    fun observeUser(): Flow<User>
    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getUser(): User
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    @Update
    suspend fun updateUser(user: User)
}
```
- Observation methods return `Flow<T>` (no suspend)
- One-shot reads use `suspend fun`
- `@Insert(onConflict = OnConflictStrategy.REPLACE)` for upsert behavior
- `deleteAll()` with `@Query("DELETE FROM ...")` for cleanup

## DataStore Preferences Pattern

```
class LoginPreferences(private val dataStore: DataStore<Preferences>) {
    val loginState: Flow<LoginPrefsData> = dataStore.data.map { prefs ->
        LoginPrefsData(isLoggedIn = prefs[IS_LOGGED_IN_KEY] ?: false, selectedAvatar = prefs[SELECTED_AVATAR_KEY] ?: 1)
    }
    suspend fun setLoggedIn(avatar: Int) { dataStore.edit { ... } }
    suspend fun clearLogin() { dataStore.edit { prefs -> prefs.clear() } }
}
```
- Companion object holds `Preferences.Key` constants with `snake_case` key names
- Data class `LoginPrefsData` for typed preference bundle
- Extension property for `DataStore<Preferences>` on `Context` in `AppContainer.kt`

## Compose Component Patterns

**Screen composables:**
```
@Composable
fun HomeScreen(viewModel: HomeViewModel, onNavigateToMissions: () -> Unit = {}, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Box/Column layout with conditional modals/overlays
}
```
**Component composables:**
```
@Composable
fun PiggyBankCard(balance: Double, goal: Double, goalProgressPercent: Float, onDepositClick: () -> Unit, modifier: Modifier = Modifier) { ... }
```
- Cards use `CardDefaults.cardColors(containerColor = Color.Transparent)` for gradient backgrounds
- Custom shapes from `KidShapes` object
- Animations use `rememberInfiniteTransition` with `animateFloat` and `infiniteRepeatable`
- Progress bars use `ShinyProgressBar` with horizontal shine effect via `graphicsLayer`

## Concurrency Patterns

- **Repository:** `CoroutineScope(SupervisorJob() + Dispatchers.IO)` for internal bridge operations. Bridges Room DAO Flows → StateFlows using 7 `launch {}` collectors in `init {}`. `_isShutdown: AtomicBoolean` for lifecycle guard.
- **⚠️ CRITICAL:** The old Mutex-based synchronization was **removed** during Room migration. Concurrent `deposit()` calls can race on the same user row — both read the same snapshot, both compute `balance + amount`, and the second write overwrites the first (lost coins). This is a data integrity bug that needs to be re-addressed (see CONCERNS).
- **StateFlow** for observable state (bridged from Room DAO Flows)
- **SharedFlow** for one-shot events with `BufferOverflow.DROP_OLDEST` and 16-element buffer (`RepositoryEvent`)
- **viewModelScope.launch** for all coroutine scoping in ViewModels
- `distinctUntilChanged()` on event flows to prevent redundant UI updates
- CancellationException propagation preserved (never caught silently)
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:.planning/codebase/ARCHITECTURE.md -->
## Architecture

## System Overview
```text
┌──────────────────────────────────────────────────────────────────────────────┐
│                              UI Layer (Compose)                              │
├─────────────┬──────────┬───────────┬──────────┬───────────┬─────────────────┤
│  HomeScreen │GoalsScreen│MissionsS. │RewardsS. │RankingS.  │  LoginScreen    │
│  +HomeVM    │+GoalsVM   │+MissionsVM│+RewardsVM│+RankingVM │ +LoginVM        │
└──────┬──────┴─────┬─────┴─────┬─────┴────┬─────┴─────┬─────┴────────┬────────┘
       │            │           │          │           │              │
       │   via ViewModelProvider.Factory   │           │    via LoginPreferences
       │                                   │           │    (DataStore)
       ▼            ▼           ▼          ▼           ▼              ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                          Data Layer                                          │
│  AppRepository — Room DAO Flow → StateFlow bridging (7 collectors in init)   │
│  ┌───────────────────────────────────────────────────────────────────┐       │
│  │  StateFlow<User>  StateFlow<List<SavingsGoal>>  SharedFlow<Event> │       │
│  │  StateFlow<Mission>  StateFlow<List<Reward>>                      │       │
│  │  StateFlow<FamilyM.> StateFlow<List<Transaction>>                 │       │
│  │  StateFlow<Challenge>   LoginPreferences → Flow<LoginPrefsData>   │       │
│  └───────────────────────────────────────────────────────────────────┘       │
│       │                                                                      │
│       ▼                                                                      │
│  Room DAOs (7 interfaces) + DataStore Preferences                            │
│  UserDao | TransactionDao | SavingsGoalDao | MissionDao                      │
│  RewardDao | FamilyMemberDao | WeeklyChallengeDao                            │
└──────────────────────────────────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│  SQLite Database (Room) + DataStore file                                     │
│  pokectbank.db (7 tables) + login_state.preferences_pb                       │
└──────────────────────────────────────────────────────────────────────────────┘
```

## Component Responsibilities
| Component | Responsibility | File |
|-----------|----------------|------|
| MainActivity | App entry point, Scaffold with bottom nav, auth gate (checking→logged-out→logged-in), initializes AppContainer | `app/src/main/java/com/pokect/bank/kids/MainActivity.kt` |
| PokectBankNavGraph | Route definitions, ViewModel creation via factories, screen transitions | `app/src/main/java/com/pokect/bank/kids/ui/navigation/NavGraph.kt` |
| Screen | Sealed class defining 6 routes, icons, and bottom nav tabs | `app/src/main/java/com/pokect/bank/kids/ui/navigation/Screen.kt` |
| AppContainer | Manual DI singleton, holds single AppRepository, PokectBankDatabase, and LoginPreferences instance | `app/src/main/java/com/pokect/bank/kids/di/AppContainer.kt` |
| AppRepository | All CRUD operations, bridges Room DAO Flows to StateFlows, event broadcast via SharedFlow | `app/src/main/java/com/pokect/bank/kids/data/repository/AppRepository.kt` |
| RepositoryEvent | Sealed interface for event types broadcast by repository (6 event types) | `app/src/main/java/com/pokect/bank/kids/data/repository/RepositoryEvent.kt` |
| PokectBankDatabase | Room database — 7 entities, 7 DAOs, seed data callback, in-memory test instance | `app/src/main/java/com/pokect/bank/kids/data/local/PokectBankDatabase.kt` |
| PokectBankConverters | Room TypeConverters — enums, List<String>, Instant ↔ Long | `app/src/main/java/com/pokect/bank/kids/data/local/PokectBankConverters.kt` |
| LoginPreferences | DataStore wrapper — persists login state (isLoggedIn, selectedAvatar) | `app/src/main/java/com/pokect/bank/kids/data/preferences/LoginPreferences.kt` |
| HomeViewModel | Home screen state: balance, level, missions, transactions, deposit flow | `app/src/main/java/com/pokect/bank/kids/ui/screens/home/HomeViewModel.kt` |
| GoalsViewModel | Goals CRUD, allocate funds, add-goal modal flow | `app/src/main/java/com/pokect/bank/kids/ui/screens/goals/GoalsViewModel.kt` |
| MissionsViewModel | Mission list with category filter | `app/src/main/java/com/pokect/bank/kids/ui/screens/missions/MissionsViewModel.kt` |
| RewardsViewModel | Reward shop, redeem flow, redemption confirmation modal | `app/src/main/java/com/pokect/bank/kids/ui/screens/rewards/RewardsViewModel.kt` |
| RankingViewModel | Family leaderboard sorted by XP, weekly challenge join | `app/src/main/java/com/pokect/bank/kids/ui/screens/ranking/RankingViewModel.kt` |
| LoginViewModel | Auth flow (login/signup tabs, 2-step wizard with avatar selection) | `app/src/main/java/com/pokect/bank/kids/ui/screens/login/LoginViewModel.kt` |
| PokectBankTheme | Material 3 theme: custom colors, Nunito typography, kid-friendly shapes, animation specs | `app/src/main/java/com/pokect/bank/kids/ui/theme/` |

## Pattern Overview
- Each screen is a triad: `*Screen.kt` (Composable) + `*ViewModel.kt` + `*UiState` (immutable data class)
- ViewModels expose a single `StateFlow<UiState>` consumed via `collectAsStateWithLifecycle()`
- All state mutations flow through `MutableStateFlow.update {}` in ViewModels
- Repository exposes `StateFlow<>` for data (bridged from Room DAO Flows in init{}) and `SharedFlow<RepositoryEvent>` for change notifications
- All repository writes are `suspend` functions that return `Result<Unit>`
- Room provides SQLite persistence with seed data via `SeedDatabaseCallback.onCreate()`
- DataStore stores login session (boolean + avatar index) across app restarts
- Manual DI via `object AppContainer` (no Hilt, no Koin)
- Each ViewModel has its own `ViewModelProvider.Factory` receiving the repository

## Layers
**UI Layer:**
- Purpose: Jetpack Compose UI rendering, user interaction handling
- Location: `app/src/main/java/com/pokect/bank/kids/ui/`
- Contains: Screens (6 feature screens), shared components, navigation, theme (Color, Type, Shape, Theme, AnimationSpecs)
- Depends on: Data layer (`data/`) via ViewModels
- Used by: MainActivity sets the root composable

**Data Layer:**
- Purpose: Single source of truth for all application state, business logic, seed data, persistence
- Location: `app/src/main/java/com/pokect/bank/kids/data/`
- Contains: Models (`data/models/`), Repository (`data/repository/AppRepository.kt`), Events (`data/repository/RepositoryEvent.kt`), Room DAOs (`data/local/dao/`), Database + Converters (`data/local/`), DataStore (`data/preferences/LoginPreferences.kt`)
- Depends on: Kotlin stdlib, kotlinx.coroutines, Room, DataStore
- Used by: ViewModels (direct injection via factories)

**DI Layer:**
- Purpose: Provide singleton repository instance to ViewModels
- Location: `app/src/main/java/com/pokect/bank/kids/di/AppContainer.kt`
- Contains: `object AppContainer` with lazy database, repository, and loginPreferences; reset support
- Depends on: Data layer
- Used by: NavGraph (passed to ViewModel factories)

**Utility Layer:**
- Purpose: Shared formatting functions
- Location: `app/src/main/java/com/pokect/bank/kids/util/`
- Contains: `CurrencyFormatter.kt` (pt-BR currency), `DateFormatter.kt` (relative dates)
- Depends on: Java stdlib
- Used by: UI components

**Theme Layer:**
- Purpose: Material 3 design system configuration
- Location: `app/src/main/java/com/pokect/bank/kids/ui/theme/`
- Contains: `Color.kt` (light scheme + gradient presets), `Type.kt` (Nunito font family), `Shape.kt` (rounded shapes), `Theme.kt` (theme composable + AnimationSpecs object)
- Depends on: Compose Material 3
- Used by: All screens via `PokectBankTheme {}`

## Data Flow

### Primary Request Path (Deposit)
1. User taps deposit → `HomeScreen` calls `viewModel.deposit(amount)`
2. `HomeViewModel.deposit()` launches coroutine → calls `repository.deposit(amount)`
3. `AppRepository.deposit()` validates → reads current user via `userDao.getUser()` → computes updated user → `userDao.updateUser()` + `familyMemberDao.syncCurrentUser()` + `transactionDao.insertTransaction()` → broadcasts `UserUpdated` + `TransactionsChanged`
4. Room auto-emits new Flow values → AppRepository's init collectors update `_user` and `_transactions` StateFlows
5. `HomeViewModel` event collector receives `UserUpdated` → calls `refreshUserData()` → updates `_uiState`
6. Compose UI recomposes with new balance, transactions, XP

### Goal Allocation Flow
1. User allocates coins to goal → `GoalsViewModel.allocateToGoal(goalId, amount)`
2. `AppRepository.allocateToGoal()` validates → checks balance → `userDao.updateUser()` (deduct) + `savingsGoalDao.updateGoal()` + `transactionDao.insertTransaction()` → broadcasts `GoalsChanged` + `UserUpdated` + `TransactionsChanged`
3. `GoalsViewModel` event collector receives → calls `loadGoalsAndUser()` → updates `_uiState`

### Reward Redemption Flow
1. User taps reward → `RewardsViewModel.redeemReward(rewardId)` → confirmation modal
2. User confirms → `confirmRedemption()` validates coins → calls `repository.redeemReward(rewardId)`
3. `AppRepository.redeemReward()` validates → reads reward + user → checks coins → `userDao.updateUser()` (deduct) + `rewardDao.updateReward()` (mark redeemed) + `familyMemberDao.syncCurrentUser()` + `transactionDao.insertTransaction()` → broadcasts events
4. `RewardsViewModel` event collector receives → refreshes coins + rewards → confetti

### Login/Auth Flow
1. `MainActivity` creates `LoginViewModel(AppContainer.loginPreferences)` for auth gate
2. `LoginViewModel.init{}` collects `LoginPreferences.loginState` Flow → populates `isLoggedIn`, `selectedAvatar`
3. On login, `LoginViewModel.login()` calls `loginPrefs.setLoggedIn(avatar)` → DataStore persists → `isLoggedIn = true`
4. `MainActivity` reads `authState.isLoggedIn` — if true, `NavHost` starts at `Screen.Home`; if false, at `Screen.Login`
5. On logout, `AppContainer.resetRepository()` clears DataStore + drops DB → next launch re-seeds

### Cross-Screen Event Propagation
- StateFlows for persistent data (user, missions, goals, rewards, transactions, family members, weekly challenges)
- SharedFlow for transient change notifications (RepositoryEvent)
- Compose `collectAsStateWithLifecycle()` for lifecycle-aware consumption in UI
- ViewModel-internal `MutableStateFlow.update {}` for atomic state transitions

## Key Abstractions

**UiState data classes:**
- Purpose: Immutable snapshot of all data a screen needs to render
- Examples: `HomeUiState` (standalone file), `GoalsUiState` (inline in GoalsViewModel), `MissionsUiState` (inline), `RewardsUiState` (inline), `RankingUiState` (inline), `LoginUiState` (inline)
- Pattern: Data class with default values, exposed as `StateFlow<>` from ViewModel

**ViewModelFactory:**
- Purpose: Inject repository into ViewModels without DI framework
- Examples: 5 factory classes (Home, Goals, Missions, Rewards, Ranking)
- Pattern: Implements `ViewModelProvider.Factory`, takes `AppRepository` in constructor, checks `modelClass.isAssignableFrom`

**AnimationSpecs:**
- Purpose: Centralized animation constants for UI consistency
- Location: `ui/theme/Theme.kt`
- Contains: `float`, `pulseGlow`, `bounceIn`, `coinSpin`, `wiggle`, `screenTransition`, `CONFETTI_DURATION`, `CONFETTI_PARTICLE_COUNT`

**Repository DAO Bridge pattern:**
- Purpose: Convert Room's `Flow<T>` to `MutableStateFlow<T>` while preserving existing StateFlow API
- Location: `data/repository/AppRepository.kt` init block
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

**AndroidManifest:**
- Location: `app/src/main/AndroidManifest.xml`
- Config: `launchMode="singleTask"`, `screenOrientation="portrait"`, targetSdk 36, minSdk 26

**Navigation:**
- Conditional start destination: `Screen.Login` if not authenticated, `Screen.Home` if logged in
- 5 bottom tabs: Home, Goals, Missions, Rewards, Ranking (Login excluded)
- Missions tab has a red dot indicator

## Architectural Constraints
- **Threading:** All repository operations run on `repositoryScope` (Dispatchers.IO). Room DAO calls are `suspend`. ViewModel coroutines run on `viewModelScope` (Dispatchers.Main). ⚠️ No Mutex synchronization on writes — concurrent deposits can lose coins (race condition).
- **Global state:** `AppContainer` is a Kotlin `object` (singleton). Database uses `@Volatile` + `synchronized` for thread-safe singleton.
- **Circular imports:** No circular dependencies detected. Data layer has no dependency on UI layer.
- **Orientation:** Portrait-only (`screenOrientation="portrait"` in manifest).
- **Dark theme:** Not supported for v1. Only light color scheme defined.
- **Data persistence:** Room SQLite database (`pokectbank.db`) with seed data callback. DataStore Preferences for login state. Data survives app restarts. Reset clears both.
- **API contract stability:** Repository public method signatures (`deposit`, `allocateToGoal`, `redeemReward`, etc.) are frozen — ViewModels must not be changed.

## Anti-Patterns

### God Repository
**What:** `AppRepository` handles all 7 entities — users, transactions, goals, missions, rewards, family members, weekly challenges. 359+ lines with validation, event broadcasting, and DAO bridging for every entity.
**Fix:** Split into entity-specific repositories (e.g., `UserRepository`, `GoalRepository`). DAOs are already separated.

### UiState Classes Defined Inside ViewModels
**What:** Five of six UiState classes are defined inside ViewModel files. Only `HomeUiState` has its own file.
**Fix:** Extract each to its own file (e.g., `GoalsUiState.kt`) matching the `HomeUiState.kt` pattern.

### Missing Interface for Repository
**What:** ViewModels depend directly on concrete `AppRepository`. No interface/abstraction exists.
**Fix:** Extract an `AppRepository` interface. ViewModels depend on interface. Tests can use a fake implementation.

### Non-Thread-Safe Lazy Init in AppContainer
**What:** `_database`/`_repository` getters are NOT thread-safe (`!!` force-unwrap, no `@Volatile`, no `synchronized`). Two threads could create duplicate instances.
**Fix:** Use `by lazy` delegation or add `@Volatile` + `synchronized`.

### Duplicate LoginViewModel Instances
**What:** `LoginViewModel` is created twice — in `MainActivity.kt` (auth gate) and `NavGraph.kt` (LoginScreen). Separate instances with separate StateFlows. Auth gate relies on DataStore propagation timing.
**Fix:** Extract a single LoginViewModel scoped to the Activity and pass it down.

## Error Handling
- Repository write methods return `Result<Unit>` — success or failure with exception
- Guard clauses with early returns: `Result.failure(IllegalArgumentException(...))` for invalid input
- State errors: `Result.failure(IllegalStateException(...))` for insufficient coins, goal not found
- `CancellationException` always re-thrown: `catch (e: CancellationException) { throw e }`
- Generic catch-all fallback: `catch (e: Exception) { Result.failure(e) }`
- ViewModels extract error messages via `result.exceptionOrNull()?.message ?: "fallback"`
- UI errors surfaced via `error: String?` field in UiState data classes
- Error messages in Portuguese: `"Erro ao guardar moedinhas"`, `"Moedas insuficientes"`

## Cross-Cutting Concerns
**Logging:** `android.util.Log.w()` used for non-critical events (dropped events, DAO flow errors, login state clear failures). No custom logging abstraction.
**Authentication:** Simplified "auth" via DataStore — boolean `is_logged_in` flag + `selected_avatar` int. No real passwords or tokens.
**Reset mechanism:** `AppContainer.resetRepository()` clears DataStore login state + nullifies DB/repository references. `PokectBankDatabase.resetInstance()` deletes the database file. Fresh seed data on next access.
**Validation:** Input validation in repository write methods. LoginViewModel performs its own validation with inline Portuguese messages.
<!-- GSD:architecture-end -->

<!-- GSD:structure-start source:.planning/codebase/STRUCTURE.md -->
## Directory Structure

### Key Source Locations

**Entry Points:**
- `app/src/main/java/com/pokect/bank/kids/MainActivity.kt` — Single Activity, Scaffold, auth gate
- `app/src/main/AndroidManifest.xml` — portrait-only, singleTask, minSdk 26

**Core Logic:**
- `data/repository/AppRepository.kt` — All business operations, DAO→StateFlow bridging
- `data/local/PokectBankDatabase.kt` — Room database: 7 entities, 7 DAOs, seed callback
- `data/preferences/LoginPreferences.kt` — DataStore wrapper for login state

**Data Models:** `data/models/`
- `User.kt`, `Transaction.kt`, `SavingsGoal.kt`, `Mission.kt`, `Reward.kt`, `FamilyMember.kt`, `WeeklyChallenge.kt`, `AvatarOption.kt`

**Room DAOs:** `data/local/dao/`
- `UserDao.kt`, `TransactionDao.kt`, `SavingsGoalDao.kt`, `MissionDao.kt`, `RewardDao.kt`, `FamilyMemberDao.kt`, `WeeklyChallengeDao.kt`

**Navigation:** `ui/navigation/` — `NavGraph.kt`, `Screen.kt`
**DI:** `di/AppContainer.kt` — singleton, lazy DB/repo/prefs, reset support
**Theme:** `ui/theme/` — `Color.kt`, `Type.kt`, `Shape.kt`, `Theme.kt`
**Utilities:** `util/` — `CurrencyFormatter.kt`, `DateFormatter.kt`

### Where to Add New Code

**New Screen:**
1. Create `ui/screens/<feature>/<Feature>Screen.kt`, `<Feature>ViewModel.kt`, `<Feature>ViewModelFactory.kt`
2. Add route to `Screen.kt` sealed class
3. Add composable route in `NavGraph.kt`
4. Mirror tests at `app/src/test/java/com/pokect/bank/kids/ui/screens/<feature>/`

**New Entity:**
1. Model: `data/models/<Entity>.kt` with `@Entity`, `@PrimaryKey(autoGenerate = true) val uid: Long = 0`
2. DAO: `data/local/dao/<Entity>Dao.kt` with `observe*()` Flow + CRUD
3. Database: Register in `@Database(entities = [...])` + add abstract DAO accessor
4. Repository: Inject DAO, add bridge collector in `init{}`, add write/read methods
5. DI: Update `AppContainer.kt` to pass new DAO
6. Seed: Add SQL INSERT to `SeedDatabaseCallback`
7. Converters: Add to `PokectBankConverters.kt` if needed

**New Shared Component:**
- Cross-screen: `ui/components/<Component>.kt`
- Screen-local: `ui/screens/<feature>/components/<Component>.kt`
<!-- GSD:structure-end -->

<!-- GSD:testing-start source:.planning/codebase/TESTING.md -->
## Testing

### Frameworks
- JUnit 4.13.2 — runner
- Kotlin Test 2.1.0 — assertions (`assertEquals`, `assertTrue`, etc.)
- Kotlinx Coroutines Test 1.8.1 — `runTest`, `StandardTestDispatcher`, `UnconfinedTestDispatcher`
- Robolectric 4.14.1 — Android framework on JVM
- AndroidX Test Core 1.6.1 — `ApplicationProvider`

### Test Types
| Type | Runs on | Examples |
|------|---------|----------|
| DAO tests | Robolectric + in-memory Room | `UserDaoTest.kt`, `TransactionDaoTest.kt` (7 files) |
| Repository tests | Robolectric + in-memory Room + seed callback | `AppRepositoryTest.kt` (567 lines, 32 tests) |
| ViewModel tests | Robolectric + real repository from `TestDatabaseHelper` | `HomeViewModelTest.kt`, `GoalsViewModelTest.kt`, etc. (5 files) |
| Converter tests | Plain JUnit (no Android dep) | `PokectBankConvertersTest.kt` |
| Model tests | Plain JUnit | `RewardModelTest.kt`, `WeeklyChallengeModelTest.kt` |
| DataStore tests | Temp files + `PreferenceDataStoreFactory` | `LoginPreferencesTest.kt` |
| DI tests | Robolectric | `AppContainerTest.kt` |
| Compose UI | Not implemented | `ComponentsTest.kt` — placeholder (`assert(true)`) |

### Common Test Patterns
- **Setup:** `Dispatchers.setMain(testDispatcher)` in `@Before`, `Dispatchers.resetMain()` in `@After`
- **DAO tests:** `runTest(testDispatcher)` with `UnconfinedTestDispatcher`
- **Repository tests:** `runTest(testDispatcher)` with `StandardTestDispatcher` + `advanceUntilIdle()`
- **ViewModel tests:** `runBlocking(Dispatchers.Unconfined)` with `delay(500)` for async settlement
- **No mocking framework** — all tests use real Room in-memory DB and real DataStore on temp files
- **Shared helper:** `TestDatabaseHelper.createTestRepository()` in `data/local/TestDatabaseHelper.kt`

### Test Coverage Gaps
- No Compose UI tests (placeholder only)
- No `LoginViewModel` tests
- No concurrent operation tests (race condition verification)
- `RewardsViewModel`, `MissionsViewModel`, `RankingViewModel` tests are minimal
<!-- GSD:testing-end -->

<!-- GSD:concerns-start source:.planning/codebase/CONCERNS.md -->
## Concerns & Known Issues

### Critical
- **Race condition on concurrent writes:** The old Mutex-based synchronization was removed during Room migration. Concurrent `deposit()` calls can lose coins — both read the same snapshot, both compute `balance + amount`, second write overwrites first. Room's `@Transaction` or explicit mutex locking needed. (`AppRepository.kt`)
- **Thread-unsafe lazy init in AppContainer:** `_database`/`_repository` getters use `!!` force-unwrap without `@Volatile` or `synchronized`. Two threads can create duplicate instances. (`di/AppContainer.kt:26-52`)
- **Duplicate LoginViewModel instances:** Created twice — in `MainActivity.kt` and `NavGraph.kt`. Auth gate relies on DataStore propagation timing, creating a fragile indirection.

### Known Bugs
- **`confirmRedemption` overwrites fresh coins:** After redemption, `refreshCoins()` updates state, but a subsequent `_uiState.update {}` captures a stale snapshot, briefly showing pre-redemption coin count. (`RewardsViewModel.kt:132-140`)
- **No error display on 4 screens:** Goals, Missions, Rewards, and Ranking screens set `UiState.error` but have no `SnackbarHost` or error banner — failures are invisible to the user.
- **StateFlow bridge errors silently swallowed:** Room DAO Flow errors are caught and logged at WARN but not propagated to UI. ViewModels show stale frozen data. (`AppRepository.kt:103-135`)

### Security
- **`allowBackup=true`:** Room DB + DataStore backed up to Google Drive. Set to `false` for children's banking app.
- **No ProGuard/R8 obfuscation:** `isMinifyEnabled = false` in release builds.
- **Plaintext password in StateFlow:** `LoginUiState.password` held as `String` in memory throughout login flow. (`LoginViewModel.kt:19`)

### Fragile Areas
- **StateFlow Bridge (7 collectors):** If any Room Flow stops emitting (DB corruption), StateFlow silently freezes with no retry or error propagation.
- **Database reset flow (4-class chain):** `HomeViewModel` → `AppRepository` → `AppContainer` → `PokectBankDatabase`. If any step fails silently, corrupted state may persist.
- **No database migrations:** `fallbackToDestructiveMigration()` + `exportSchema = false`. Any schema change wipes all user data.

### Tech Debt
- God Repository: 359 lines handling all 7 entities
- UiState classes inside ViewModels (5 of 6)
- No repository interface → tight coupling
- Comma-based `List<String>` converter (breaks if IDs contain commas)
- No `LoginViewModel` tests
- No concurrent operation tests
<!-- GSD:concerns-end -->

<!-- GSD:skills-start source:skills/ -->
## Project Skills

No project skills found. Add skills to any of: `.claude/skills/`, `.agents/skills/`, `.cursor/skills/`, `.github/skills/`, or `.codex/skills/` with a `SKILL.md` index file.
<!-- GSD:skills-end -->

<!-- GSD:workflow-start source:GSD defaults -->
## GSD Workflow Enforcement

Before using Edit, Write, or other file-changing tools, start work through a GSD command so planning artifacts and execution context stay in sync.

Use these entry points:
- `/gsd-quick` for small fixes, doc updates, and ad-hoc tasks
- `/gsd-debug` for investigation and bug fixing
- `/gsd-execute-phase` for planned phase work

Do not make direct repo edits outside a GSD workflow unless the user explicitly asks to bypass it.
<!-- GSD:workflow-end -->



<!-- GSD:profile-start -->
## Developer Profile

> Profile not yet configured. Run `/gsd-profile-user` to generate your developer profile.
> This section is managed by `generate-claude-profile` -- do not edit manually.
<!-- GSD:profile-end -->
