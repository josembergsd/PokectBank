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

<!-- GSD:stack-start source:codebase/STACK.md -->
## Technology Stack

## Languages
- Kotlin 2.1.0 — All application source code under `app/src/main/java/com/pokect/bank/`
- Java 17 (compile target) — JVM target for Kotlin compilation; no hand-written Java source files
- Gradle Kotlin DSL — Build scripts in `build.gradle.kts` and `app/build.gradle.kts`
## Runtime
- Android — `minSdk = 26` (Android 8.0 Oreo), `targetSdk = 36`, `compileSdk = 36`
- Gradle 9.3.1 (wrapper)
- Lockfile: Not applicable (Gradle uses `libs.versions.toml` with dependency verification available but not configured)
- Version Catalog: `gradle/libs.versions.toml`
## Frameworks
- Jetpack Compose BOM 2024.12.01 — UI framework
- AndroidX Navigation — `navigation-compose` 2.8.5 (screen-based navigation with `NavHost`)
- AndroidX Lifecycle — 2.8.7 (`lifecycle-runtime-compose`, `lifecycle-viewmodel-compose`)
- AndroidX Activity — `activity-compose` 1.9.3 (Compose integration with `ComponentActivity`)
- JUnit 4.13.2 — Unit test runner
- Kotlin Test — `org.jetbrains.kotlin:kotlin-test` 2.1.0
- Kotlinx Coroutines Test 1.8.1 — `kotlinx-coroutines-test` for coroutine testing
- Compose UI Test — `androidx.compose.ui:ui-test-junit4` + `ui-test-manifest` (debug)
- Android Gradle Plugin (AGP) 8.7.3
- Kotlin Compose Compiler Plugin — `org.jetbrains.kotlin.plugin.compose` 2.1.0 (bundled in Kotlin 2.0+)
- Gradle Toolchain Resolver — `org.gradle.toolchains.foojay-resolver-convention` 1.0.0
## Key Dependencies
| Dependency | Version | Purpose |
|---|---|---|
| `androidx.compose:compose-bom` | 2024.12.01 | Manages all Compose library versions |
| `androidx.compose.material3:material3` | (via BOM) | Material 3 UI components for all screens |
| `androidx.navigation:navigation-compose` | 2.8.5 | In-app navigation between 6 screens |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | 2.8.7 | ViewModel integration with Compose |
| `androidx.activity:activity-compose` | 1.9.3 | Compose host activity |
- No network libraries (Retrofit, OkHttp, Ktor — none detected)
- No database libraries (Room, DataStore, SQLite — none detected)
- No dependency injection framework (Hilt, Koin, Kodein — none detected)
- Manual DI via singleton `AppContainer` object at `app/src/main/java/com/pokect/bank/kids/di/AppContainer.kt`
- State management via Kotlin StateFlow/SharedFlow in in-memory repository
## Configuration
- No environment variable files (`.env`, `.env.*` — not present)
- Build configuration centralized in `gradle/libs.versions.toml` (version catalog)
- `gradle.properties`:
- `build.gradle.kts` — Root project: plugin declarations only
- `app/build.gradle.kts` — App module: SDK versions, Compose features, dependencies
- `settings.gradle.kts` — Repository configuration (Google, Maven Central), project name "PokectBank"
- `gradle/wrapper/gradle-wrapper.properties` — Gradle distribution URL (9.3.1)
## Platform Requirements
- Android Studio (or compatible IDE with Android SDK)
- JDK 17+ (JVM target in `app/build.gradle.kts`: `jvmTarget = "17"`)
- Android SDK Platform 36
- Gradle 9.3.1 (auto-downloaded via wrapper)
- Android 8.0+ (API 26+) device
- Portrait orientation only (`android:screenOrientation="portrait"` in `AndroidManifest.xml`)
- No network permission required (fully offline, in-memory state)
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
## Dependency Injection
## Concurrency Patterns
- **Mutex-based synchronization:** `kotlinx.coroutines.sync.Mutex` for thread-safe writes in `AppRepository`
- **Lock ordering documented:** `userMutex → rewardsMutex` (deadlock prevention pattern, lines 68-71 of `AppRepository.kt`)
- **StateFlow** for observable state (cold streams with initial values)
- **SharedFlow** for one-shot events with `BufferOverflow.DROP_OLDEST` and 16-element buffer (`RepositoryEvent`)
- **viewModelScope.launch** for all coroutine scoping in ViewModels
- CancellationException propagation preserved (never caught silently)
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->
## Architecture

## System Overview
```text
```
## Component Responsibilities
| Component | Responsibility | File |
|-----------|----------------|------|
| MainActivity | App entry point, Scaffold with bottom nav, auth gate | `app/src/main/java/com/pokect/bank/kids/MainActivity.kt` |
| PokectBankNavGraph | Route definitions, ViewModel creation via factories, screen transitions | `app/src/main/java/com/pokect/bank/kids/ui/navigation/NavGraph.kt` |
| Screen | Sealed class defining 6 routes, icons, and bottom nav tabs | `app/src/main/java/com/pokect/bank/kids/ui/navigation/Screen.kt` |
| AppContainer | Manual DI singleton, holds single AppRepository instance | `app/src/main/java/com/pokect/bank/kids/di/AppContainer.kt` |
| AppRepository | In-memory data store, all CRUD operations, event broadcast | `app/src/main/java/com/pokect/bank/kids/data/repository/AppRepository.kt` |
| RepositoryEvent | Sealed interface for event types broadcast by repository | `app/src/main/java/com/pokect/bank/kids/data/repository/RepositoryEvent.kt` |
| HomeViewModel | Home screen state: balance, level, missions, transactions, deposit flow | `app/src/main/java/com/pokect/bank/kids/ui/screens/home/HomeViewModel.kt` |
| GoalsViewModel | Goals CRUD, allocate funds, add-goal modal flow | `app/src/main/java/com/pokect/bank/kids/ui/screens/goals/GoalsViewModel.kt` |
| MissionsViewModel | Mission list with category filter | `app/src/main/java/com/pokect/bank/kids/ui/screens/missions/MissionsViewModel.kt` |
| RewardsViewModel | Reward shop, redeem flow, redemption confirmation modal | `app/src/main/java/com/pokect/bank/kids/ui/screens/rewards/RewardsViewModel.kt` |
| RankingViewModel | Family leaderboard sorted by XP, weekly challenge join | `app/src/main/java/com/pokect/bank/kids/ui/screens/ranking/RankingViewModel.kt` |
| LoginViewModel | Auth flow (login/signup tabs, 2-step wizard with avatar selection) | `app/src/main/java/com/pokect/bank/kids/ui/screens/login/LoginViewModel.kt` |
| PokectBankTheme | Material 3 theme: custom colors, Nunito typography, kid-friendly shapes, animation specs | `app/src/main/java/com/pokect/bank/kids/ui/theme/` |
## Pattern Overview
- Each screen is a triad: `*Screen.kt` (Composable) + `*ViewModel.kt` + `*UiState.kt` (immutable data class)
- ViewModels expose a single `StateFlow<UiState>` consumed via `collectAsStateWithLifecycle()`
- All state mutations flow through `MutableStateFlow.update {}` in ViewModels
- Repository exposes `StateFlow<>` for data and `SharedFlow<RepositoryEvent>` for change notifications
- All repository writes are `suspend` functions protected by per-entity `Mutex` locks
- No database, no network — all data is in-memory with seed data
- Manual DI via `object AppContainer` (no Hilt, no Koin)
- Each ViewModel has its own `ViewModelProvider.Factory` receiving the repository
## Layers
- Purpose: Jetpack Compose UI rendering, user interaction handling
- Location: `app/src/main/java/com/pokect/bank/kids/ui/`
- Contains: Screens (6 feature screens), shared components, navigation, theme (Color, Type, Shape, Theme, AnimationSpecs)
- Depends on: Data layer (`data/`) via ViewModels
- Used by: MainActivity sets the root composable
- Purpose: Single source of truth for all application state, business logic, seed data
- Location: `app/src/main/java/com/pokect/bank/kids/data/`
- Contains: Models (`data/models/`), Repository (`data/repository/AppRepository.kt`), Events (`data/repository/RepositoryEvent.kt`)
- Depends on: Kotlin stdlib, kotlinx.coroutines
- Used by: ViewModels (direct injection via factories)
- Purpose: Provide singleton repository instance to ViewModels
- Location: `app/src/main/java/com/pokect/bank/kids/di/AppContainer.kt`
- Contains: `object AppContainer` with lazy `AppRepository`
- Depends on: Data layer
- Used by: NavGraph (passed to ViewModel factories)
- Purpose: Shared formatting functions
- Location: `app/src/main/java/com/pokect/bank/kids/util/`
- Contains: `CurrencyFormatter.kt` (pt-BR currency), `DateFormatter.kt` (relative dates)
- Depends on: Java stdlib
- Used by: UI components
- Purpose: Material 3 design system configuration
- Location: `app/src/main/java/com/pokect/bank/kids/ui/theme/`
- Contains: `Color.kt` (light scheme + gradient presets), `Type.kt` (Nunito font family), `Shape.kt` (rounded shapes), `Theme.kt` (theme composable + AnimationSpecs object)
- Depends on: Compose Material 3
- Used by: All screens via `PokectBankTheme {}`
## Data Flow
### Primary Request Path (Deposit)
### Goal Allocation Flow
### Reward Redemption Flow
### Cross-Screen Event Propagation
- StateFlows for persistent data (user, missions, goals, rewards, transactions, family members, weekly challenges)
- SharedFlow for transient change notifications (RepositoryEvent)
- Compose `collectAsStateWithLifecycle()` for lifecycle-aware consumption in UI
- ViewModel-internal `MutableStateFlow.update {}` for atomic state transitions
## Key Abstractions
- Purpose: Immersive immutable snapshot of all data a screen needs to render
- Examples: `HomeUiState` (`ui/screens/home/HomeUiState.kt`), `GoalsUiState` (defined in `GoalsViewModel.kt`), `MissionsUiState` (defined in `MissionsViewModel.kt`), `RewardsUiState` (defined in `RewardsViewModel.kt`), `RankingUiState` (defined in `RankingViewModel.kt`), `LoginUiState` (defined in `LoginViewModel.kt`)
- Pattern: Data class with default values, exposed as `StateFlow<>` from ViewModel
- Purpose: Inject repository into ViewModels without DI framework
- Examples: `HomeViewModelFactory` (`ui/screens/home/HomeViewModelFactory.kt`), `GoalsViewModelFactory`, `MissionsViewModelFactory`, `RewardsViewModelFactory`, `RankingViewModelFactory`
- Pattern: Implements `ViewModelProvider.Factory`, takes `AppRepository` in constructor, checks `modelClass.isAssignableFrom`
- Purpose: Centralized animation constants for UI consistency
- Location: `ui/theme/Theme.kt` (lines 89-127)
- Contains: `float`, `pulseGlow`, `bounceIn`, `coinSpin`, `wiggle`, `screenTransition`, `CONFETTI_DURATION`, `CONFETTI_PARTICLE_COUNT`
## Entry Points
- Location: `app/src/main/java/com/pokect/bank/kids/MainActivity.kt`
- Triggers: Android system launch (declared in `AndroidManifest.xml` as LAUNCHER)
- Responsibilities: Creates `PokectBankTheme`, manages `LoginViewModel` auth state, renders `Scaffold` with conditional bottom nav (hidden on login), hosts `PokectBankNavGraph`
- Location: `app/src/main/AndroidManifest.xml`
- Config: `launchMode="singleTask"`, `screenOrientation="portrait"`, targetSdk 36, minSdk 26
- Conditional: `Screen.Login` if not authenticated, `Screen.Home` if logged in
- Located in `PokectBankNavGraph()` composable in `NavGraph.kt`
## Architectural Constraints
- **Threading:** All repository writes are single-threaded within their entity via `Mutex` locks. Lock ordering is enforced: `userMutex` before `rewardsMutex`. ViewModel coroutines run on `viewModelScope` (Dispatchers.Main by default in Compose).
- **Global state:** `AppContainer` is a Kotlin `object` (singleton) — one `AppRepository` instance for the entire app process. `AppRepository` holds all data in module-level `MutableStateFlow` fields.
- **Circular imports:** No circular dependencies detected. Data layer has no dependency on UI layer. DI layer sits between data and UI.
- **Orientation:** Portrait-only (`screenOrientation="portrait"` in manifest).
- **Dark theme:** Not supported for v1. Only light color scheme defined in `PokectBankLightColorScheme`.
- **Data persistence:** All data lives in memory only. No database, no SharedPreferences, no file storage. Restarting the app resets all data to seed values.
## Anti-Patterns
### God Repository
### UiState Classes Defined Inside ViewModels
### Missing Interface for Repository
## Error Handling
- Repository write methods return `Result<Unit>` — success or failure with exception
- Input validation uses `require()` inside `try/catch` with `CancellationException` re-thrown
- ViewModels check `result.isSuccess` and either update state or set `error` message
- Non-critical validation errors (e.g., "Insufficient coins") return `Result.failure(IllegalStateException("..."))`
- LoginViewModel uses snackbar for error display via `LaunchedEffect(uiState.error)`
- HomeScreen displays error via `uiState.error` in the UiState
## Cross-Cutting Concerns
<!-- GSD:architecture-end -->

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
