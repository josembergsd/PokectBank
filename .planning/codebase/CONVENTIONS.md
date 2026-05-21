# Coding Conventions

**Analysis Date:** 2026-05-21

## Naming Patterns

**Files:**
- PascalCase for all Kotlin source files. Examples: `HomeViewModel.kt`, `HomeScreen.kt`, `AppRepository.kt`, `CurrencyFormatter.kt`
- Screens and supporting classes share the screen name prefix: `HomeScreen.kt`, `HomeViewModel.kt`, `HomeUiState.kt`, `HomeViewModelFactory.kt`
- Test files use `*Test.kt` suffix, mirroring the class under test: `AppRepositoryTest.kt`, `HomeViewModelTest.kt`, `UserDaoTest.kt`
- Build files: `build.gradle.kts` (Kotlin DSL)

**Directories:**
- Feature/domain grouping: `data/models/`, `data/repository/`, `data/local/`, `data/local/dao/`, `data/preferences/`, `ui/screens/<feature>/`, `ui/navigation/`, `ui/theme/`, `ui/components/`, `util/`, `di/`
- Per-screen subdirectories under `ui/screens/<feature>/` with a `components/` subdirectory for screen-local composables: `ui/screens/home/components/`, `ui/screens/missions/components/`, `ui/screens/goals/components/`
- Test directories mirror the source package structure exactly: `app/src/test/java/com/pokect/bank/kids/`

**Functions:**
- camelCase. Examples: `formatCurrency()`, `getGoalProgressPercent()`, `showDepositModal()`, `loadInitialData()`, `confirmRedemption()`
- Top-level utility functions use camelCase: `formatCurrency(amount: Double)`, `formatDateLabel(dateStr: String)`, `getGroupedTransactions(transactions: List<Transaction>)`
- ViewModel public methods are camelCase verbs: `deposit()`, `createGoal()`, `redeemReward()`, `toggleHistory()`, `showResetConfirmation()`
- Private ViewModel methods are camelCase: `loadInitialData()`, `refreshUserData()`, `refreshTransactions()`

**Variables:**
- camelCase for local variables and parameters. Examples: `selectedAvatar`, `depositAmount`, `showConfetti`, `initialBalance`, `currentUser`
- Private backing fields use underscore prefix convention: `_uiState` → `uiState`, `_user` → `user`, `_events` → `events`, `_isShutdown`
- No Hungarian notation or type prefixes

**Types:**
- PascalCase for all types. Examples: `HomeUiState`, `MissionsUiState`, `RankingUiState`, `MissionStatus`, `MissionCategory`
- UI state classes use `*UiState` suffix convention: `HomeUiState`, `GoalsUiState`, `RewardsUiState`, `LoginUiState`, `RankingUiState`
- Repository event sealed interface members use past-tense object names: `UserUpdated`, `GoalsChanged`, `TransactionsChanged`
- PascalCase for singleton objects and companion objects: `AppContainer`, `AnimationSpecs`, `GradientPresets`, `KidShapes`
- Companion objects hold static lists: `AvatarOption.options`, `Screen` sealed class with object members
- Enum classes use UPPER_SNAKE_CASE values: `LOCKED`, `AVAILABLE`, `IN_PROGRESS`, `COMPLETED`, `EASY`, `MEDIUM`, `HARD`, `DAILY`, `WEEKLY`, `SPECIAL`

**Room Entities:**
- `@Entity` annotation with `tableName` in snake_case: `"savings_goal"`, `"family_member"`, `"weekly_challenge"`
- Primary key: `uid: Long` with `@PrimaryKey(autoGenerate = true)` defaulting to `0`
- Business ID: `id: String` with unique index

## Code Style

**Formatting:**
- Kotlin official code style (`kotlin.code.style=official` in `gradle.properties` at `gradle.properties` line 6)
- Indentation: 4 spaces (Kotlin convention)
- No explicit column width limit configured
- Trailing commas: not used in data class constructors or function calls
- Line comments use `//` with an initial space: `// This is a comment`
- Section separator comments: `// === Section Name ===` with equals bars for major sections in longer files (`AppRepository.kt`, `Color.kt`, `RewardsViewModel.kt`, `Theme.kt`)

**Linting:**
- No dedicated Kotlin linter configured (no detekt, ktlint, or ktslint config files detected)
- No `.editorconfig` file present
- Code quality relies on Kotlin compiler warnings and manual review

**Annotations:**
- `@Suppress("UNCHECKED_CAST")` used in all ViewModelFactory classes where unchecked casts are unavoidable (`HomeViewModelFactory.kt`, `NavGraph.kt`, `MainActivity.kt`)
- `@OptIn(ExperimentalCoroutinesApi::class)` used in test files for coroutines test APIs
- `@RunWith(RobolectricTestRunner::class)` on all Android-dependent tests
- `@Volatile` on singleton database instance field (`PokectBankDatabase.kt`)

## Import Organization

**Order:**
1. Android framework imports (`android.*`, `androidx.*`)
2. Project-specific imports (`com.pokect.bank.kids.*`)
3. Kotlin stdlib and coroutines (`kotlin.*`, `kotlinx.coroutines.*`)
4. Java stdlib (`java.*`)
- Within each group, imports appear roughly alphabetical

**Path Aliases:**
- No path aliases defined. Full package paths used in all imports.
- Explicit FQN used in `NavGraph.kt` for ViewModel type annotations within composable blocks when needed: `com.pokect.bank.kids.ui.screens.home.HomeViewModel`

**Import Style:**
- Wildcard/star imports for layout packages: `import androidx.compose.foundation.layout.*`
- Specific imports for most other packages: `import androidx.compose.material3.Card`, `import androidx.compose.runtime.Composable`

## Error Handling

**Repository Layer (`AppRepository.kt`):**
- Write operations return `Result<Unit>` with typed failures
- `Result.failure(IllegalArgumentException("message"))` for invalid input (e.g., negative amounts, blank titles, goal not found)
- `Result.failure(IllegalStateException("message"))` for invalid state (e.g., insufficient coins, already joined, shutdown guard)
- Guard clauses with early returns before try/catch block: `if (_isShutdown.get()) return Result.failure(...)`
- `CancellationException` always re-thrown explicitly: `catch (e: CancellationException) { throw e }` — present in every write method
- Generic catch-all fallback: `catch (e: Exception) { Result.failure(e) }`
- Error messages in English for repository layer: `"Deposit amount must be at least 1.0"`, `"Insufficient coins"`, `"Goal not found: $goalId"`

**ViewModel Layer:**
- Extract error messages via: `result.exceptionOrNull()?.message ?: "fallback message in Portuguese"`
- UI errors surfaced via `error: String?` field in UiState data classes
- Error messages in Portuguese in fallbacks: `"Erro ao guardar moedinhas"`, `"Erro ao criar meta"`, `"Erro ao resgatar prêmio"`
- Inline validation (LoginViewModel) also uses Portuguese: `"Digite seu e-mail para continuar"`, `"Qual é o seu nome?"`
- ViewModels set both `error` and reset modal/loading state on failure
- Some ViewModels catch `CancellationException` explicitly for cleanup (`HomeViewModel.confirmReset()`)

**Top-level Utilities:**
- `DateFormatter.kt` uses `runCatching {}` with `.getOrDefault()` for safe date parsing fallback

**State Restoration Error:**
- `LoginPreferences.kt` catches `IOException` during DataStore writes and sets error state rather than crashing
- `AppContainer.resetRepository()` catches exceptions during `clearLogin()` and logs a warning

**Error Reset Pattern:**
- Most viewmodel methods that accept user input also clear `error`: `_uiState.update { it.copy(name = value, error = null) }`

## Logging

**Framework:** `android.util.Log`

**Patterns:**
- `Log.w("AppRepository", ...)` for warning-level events:
  - Dropped events when SharedFlow buffer is full: `"Dropped $event — buffer full"` (`AppRepository.kt` line 94)
  - DAO Flow bridge errors: `"User Flow error"`, `"Transactions Flow error"` etc. in catch blocks (`AppRepository.kt` lines 103-135)
- `Log.w("LoginViewModel", ...)` for DataStore persistence failures (`LoginViewModel.kt` line 137)
- `Log.w("AppContainer", ...)` for login state clear failures during reset (`AppContainer.kt` line 61)
- No debug or info logging used
- No custom logging abstraction or wrapper
- ViewModels do not log (errors go to UiState instead)
- No file-based or remote logging — purely `Logcat`

## Comments

**When to Comment:**
- **Section organization:** `// === Section Name ===` with multiline equals bars for logical grouping in longer files (`AppRepository.kt`, `Color.kt`, `RewardsViewModel.kt`, `Theme.kt`)
- **Architectural decisions:** `// Lock ordering (MUST be enforced by all methods acquiring multiple mutexes)` — notes about thread safety
- **Thread safety notes:** `// Read reward outside locks — StateFlow snapshot is safe for lookup`
- **API intent documentation:** `Theme.kt` contains extensive usage-example comments for animation specs in a KDoc block showing conceptual Compose code
- **Test placeholders:** `ComponentsTest.kt` explains why Compose UI tests are not fully implemented: `"These are placeholder tests that verify the composables compile"`
- **Converter assumptions:** `PokectBankConverters.kt` documents the string list converter delimiter assumption: `"If IDs ever include commas, this converter will need a different delimiter strategy"`
- **TypeConverter comments:** `// Room stores the enum name ("DAILY", "WEEKLY", "SPECIAL"), not the label property`
- **UI-animation timing:** `// Delay before showing confetti after successful redemption — allows modal dismissal animation to complete`

**KDoc / Block Comments:**
- `/** */` block comments used for top-level utility functions: `formatDateLabel()` has KDoc describing returns values (`DateFormatter.kt` line 6)
- `AnimationSpecs` object has extensive KDoc describing each animation spec with conceptual usage examples (`Theme.kt` lines 30-88)
- Components test class has `/** */` KDoc explaining test strategy (`ComponentsTest.kt` lines 5-9)

**When NOT to Comment:**
- Not used on data classes or their fields (self-documenting via naming)
- Not used on ViewModel public methods
- Not used on Repository public methods
- Not used on Compose `@Composable` functions (parameters are self-documenting)

## Function Design

**Size:**
- Most functions are small (1–20 lines). Repository write methods are longer (20–50 lines) due to validation + mutation + transaction recording logic.
- ViewModel methods: typically 5–15 lines
- Screens/Composables: `HomeScreen.kt` is 229 lines, component-level composables tend to be 50–150 lines (`PiggyBankCard.kt` is 326 lines including sub-components)

**Parameters:**
- ViewModels accept `AppRepository` as a constructor dependency (manual DI). Exception: `LoginViewModel` accepts `LoginPreferences?`
- Compose functions use named parameters with `Modifier` as a trailing default parameter: `modifier: Modifier = Modifier`
- Callback lambdas use trailing placement and default empty values: `onNavigateToMissions: () -> Unit = {}`, `onNavigateToLogin: () -> Unit = {}`
- Boolean flags used sparingly in UiState data classes: `showDepositModal: Boolean = false`, `isLoading: Boolean = false`

**Return Values:**
- Repository writes: `Result<Unit>` (success/failure with no payload)
- Repository reads: `suspend fun getX(): T` (returns snapshot values directly, suspending functions)
- ViewModel public methods: `Unit` (no return value — state exposed via StateFlow)
- DAO methods: `suspend fun` for one-shot reads/writes, `fun observeX(): Flow<T>` for continuous observation
- Private helper functions: return type inferred or explicit

**Compose Function Conventions:**
- `@Composable` annotations on all UI functions
- Private composables extracted for sub-components (e.g., `GoalProgressSection`, `FloatingCoinsRow`, `ShinyProgressBar`, `DepositCtaButton` in `PiggyBankCard.kt`)
- `Modifier` parameter always named `modifier` with default `Modifier`
- ViewModel observed via `val uiState by viewModel.uiState.collectAsStateWithLifecycle()`

## Module Design

**Exports:**
- No explicit module or visibility modifiers; all classes/data classes/functions use `public` (Kotlin default)
- Top-level functions (utilities) are `public`: `formatCurrency()`, `formatDateLabel()`, `getGroupedTransactions()`
- Some composable sub-components are `private`: `GoalProgressSection`, `FloatingCoinsRow`, `ShinyProgressBar`, `DepositCtaButton` in `PiggyBankCard.kt`

**State Exposure Pattern:**
- ViewModel state: `private val _uiState = MutableStateFlow(UiState())` paired with `val uiState: StateFlow<UiState> = _uiState.asStateFlow()`
- Repository state: same `_` backing field → `asStateFlow()` pattern for all data stores
- Repository events: `private val _events = MutableSharedFlow<RepositoryEvent>(...)` → `val events: SharedFlow<RepositoryEvent> = _events.asSharedFlow()`
- UI collection: `val uiState by viewModel.uiState.collectAsStateWithLifecycle()`
- LoginPreferences: `val loginState: Flow<LoginPrefsData>` exposed as public property

**Barrel Files:**
- Not used. Each file exports its own class. No `index.kt` or aggregator files.
- All imports reference individual files directly by full path.

**UiState Class Placement:**
- `HomeUiState` is its own file: `ui/screens/home/HomeUiState.kt`
- `GoalsUiState` defined inside `GoalsViewModel.kt`
- `RewardsUiState` (and `RewardUi`) defined inside `RewardsViewModel.kt`
- `MissionsUiState` defined inside `MissionsViewModel.kt`
- `RankingUiState` defined inside `RankingViewModel.kt`
- `LoginUiState` defined inside `LoginViewModel.kt`

**Data Model Placement:**
- All `@Entity` data classes in `data/models/` directory, one file per entity
- Enums co-located with their primary entity (e.g., `MissionStatus` with `Mission.kt`, `TransactionType` with `Transaction.kt`)

**Top-Level Helper Functions:**
- `HomeViewModel.kt` has a `private fun HomeUiState.withUser(user: User): HomeUiState` extension function defined at file bottom (`HomeViewModel.kt` lines 181-193)

## ViewModel Factory Pattern

Each ViewModel has a dedicated `*ViewModelFactory` class implementing `ViewModelProvider.Factory`:
- `HomeViewModelFactory.kt` at `ui/screens/home/HomeViewModelFactory.kt`
- `GoalsViewModelFactory.kt` at `ui/screens/goals/GoalsViewModelFactory.kt`
- `MissionsViewModelFactory.kt` at `ui/screens/missions/MissionsViewModelFactory.kt`
- `RewardsViewModelFactory.kt` at `ui/screens/rewards/RewardsViewModelFactory.kt`
- `RankingViewModelFactory.kt` at `ui/screens/ranking/RankingViewModelFactory.kt`

Factory pattern (`HomeViewModelFactory.kt`):
```kotlin
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

**No DI Framework:** No Hilt, Koin, Kodein, or Dagger

## Concurrency Patterns

**Repository (`AppRepository.kt`):**
- Owns a `CoroutineScope(SupervisorJob() + Dispatchers.IO)` for internal bridge operations
- Bridges Room DAO Flows → StateFlows using `launch { dao.observeX().collect { _x.value = it } }` in `init {}`
- `_isShutdown: AtomicBoolean` for lifecycle guard preventing writes after reset
- `MutableSharedFlow` for events with `extraBufferCapacity = 16`, `BufferOverflow.DROP_OLDEST`
- `tryEmit()` with `Log.w` on buffer overflow

**ViewModel:**
- `viewModelScope.launch` for all coroutine scoping
- `MutableStateFlow.update {}` for atomic state transitions
- `distinctUntilChanged()` on event flows to prevent redundant UI updates
- `CancellationException` propagation preserved (not caught silently)

**Test Dispatchers:**
- DAO/Repository tests: `StandardTestDispatcher` or `UnconfinedTestDispatcher` set as `Dispatchers.Main`
- ViewModel tests: `Dispatchers.Unconfined` set as `Dispatchers.Main`, using `runBlocking`

## DataStore Preferences Pattern (`LoginPreferences.kt`)

```kotlin
class LoginPreferences(private val dataStore: DataStore<Preferences>) {
    val loginState: Flow<LoginPrefsData> = dataStore.data.map { prefs ->
        LoginPrefsData(
            isLoggedIn = prefs[IS_LOGGED_IN_KEY] ?: false,
            selectedAvatar = prefs[SELECTED_AVATAR_KEY] ?: 1
        )
    }
    suspend fun setLoggedIn(avatar: Int) { dataStore.edit { ... } }
    suspend fun clearLogin() { dataStore.edit { prefs -> prefs.clear() } }
    companion object {
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        private val SELECTED_AVATAR_KEY = intPreferencesKey("selected_avatar")
    }
}
```

- Companion object holds `Preferences.Key` constants with `snake_case` key names
- Data class `LoginPrefsData` for typed preference bundle
- Extension property for `DataStore<Preferences>` on `Context` via `preferencesDataStore(name = "login_state")` in `AppContainer.kt`

## Room DAO Pattern (`UserDao.kt`)

```kotlin
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

- DAOs are `interface` with `@Dao` annotation
- Observation methods return `Flow<T>` (no suspend)
- One-shot reads use `suspend fun`
- `@Insert(onConflict = OnConflictStrategy.REPLACE)` for upsert behavior
- `deleteAll()` with `@Query("DELETE FROM ...")` for cleanup

## Compose Component Patterns

**Screen composables** (`HomeScreen.kt`):
```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToMissions: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ... Box/Column layout with conditional modals/overlays
}
```

**Component composables** (`PiggyBankCard.kt`):
```kotlin
@Composable
fun PiggyBankCard(
    balance: Double,
    goal: Double,
    goalProgressPercent: Float,
    depositAmount: Double,
    onDepositClick: () -> Unit,
    modifier: Modifier = Modifier
) { ... }
```

- Cards use `CardDefaults.cardColors(containerColor = Color.Transparent)` for gradient backgrounds
- Custom shapes from `KidShapes` object
- Animations use `rememberInfiniteTransition` with `animateFloat` and `infiniteRepeatable`
- Progress bars use `ShinyProgressBar` with horizontal shine effect via `graphicsLayer`

---

*Convention analysis: 2026-05-21*
