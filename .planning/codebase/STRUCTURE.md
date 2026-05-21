# Codebase Structure

**Analysis Date:** 2026-05-21

## Directory Layout

```
PokectBank/
├── app/                                 # Android application module
│   ├── build.gradle.kts                 # App build config (plugins, SDK, deps)
│   ├── proguard-rules.pro               # ProGuard rules (empty, minification disabled)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml       # App manifest (single activity, portrait)
│       │   ├── java/com/pokect/bank/kids/
│       │   │   ├── MainActivity.kt       # App entry point, Scaffold, auth gate
│       │   │   ├── data/
│       │   │   │   ├── local/
│       │   │   │   │   ├── PokectBankDatabase.kt     # Room database (7 entities, seed callback)
│       │   │   │   │   ├── PokectBankConverters.kt   # Room TypeConverters
│       │   │   │   │   └── dao/
│       │   │   │   │       ├── UserDao.kt               # User CRUD + Flow observer
│       │   │   │   │       ├── TransactionDao.kt        # Transaction CRUD + Flow
│       │   │   │   │       ├── SavingsGoalDao.kt        # SavingsGoal CRUD + Flow
│       │   │   │   │       ├── MissionDao.kt            # Mission CRUD + Flow
│       │   │   │   │       ├── RewardDao.kt             # Reward CRUD + Flow
│       │   │   │   │       ├── FamilyMemberDao.kt       # FamilyMember CRUD + Flow + syncCurrentUser
│       │   │   │   │       └── WeeklyChallengeDao.kt    # WeeklyChallenge CRUD + Flow
│       │   │   │   ├── models/
│       │   │   │   │   ├── User.kt                 # Room entity (uid, id, name, level, balance, goal, xp, coins, badges, selectedAvatar)
│       │   │   │   │   ├── Transaction.kt           # Room entity (uid, id, type, title, amount, date, icon) + TransactionType enum
│       │   │   │   │   ├── SavingsGoal.kt           # Room entity (uid, id, title, icon, targetAmount, currentAmount, daysLeft, color) + GoalColor enum
│       │   │   │   │   ├── Mission.kt               # Room entity (uid, id, title, description, xp, coins, progress, maxProgress, status, icon, difficulty, category) + 3 enums
│       │   │   │   │   ├── Reward.kt                # Room entity (uid, id, title, description, icon, cost, isNew, redeemed)
│       │   │   │   │   ├── FamilyMember.kt          # Room entity (uid, id, name, level, coins, xp, streak, isCurrentUser)
│       │   │   │   │   ├── WeeklyChallenge.kt       # Room entity (uid, id, title, description, rewardXp, rewardCoins, endDate, participants, hasJoined)
│       │   │   │   │   └── AvatarOption.kt           # Plain data class (not entity) — avatar list for login screen
│       │   │   │   ├── preferences/
│       │   │   │   │   └── LoginPreferences.kt      # DataStore wrapper (is_logged_in, selected_avatar)
│       │   │   │   └── repository/
│       │   │   │       ├── AppRepository.kt          # All business logic, DAO bridge, event broadcast
│       │   │   │       └── RepositoryEvent.kt        # Sealed interface (6 event types)
│       │   │   ├── di/
│       │   │   │   └── AppContainer.kt           # Manual DI singleton (database, repository, loginPrefs)
│       │   │   ├── ui/
│       │   │   │   ├── components/
│       │   │   │   │   └── AvatarDisplay.kt       # Shared composable for avatar rendering
│       │   │   │   ├── navigation/
│       │   │   │   │   ├── NavGraph.kt            # NavHost with 6 composable routes + transitions
│       │   │   │   │   └── Screen.kt              # Sealed class (6 routes, icons, bottomNavTabs)
│       │   │   │   ├── screens/
│       │   │   │   │   ├── home/
│       │   │   │   │   │   ├── HomeScreen.kt              # Home screen composable (174+ lines)
│       │   │   │   │   │   ├── HomeViewModel.kt           # Home VM (deposit, confetti, reset)
│       │   │   │   │   │   ├── HomeUiState.kt             # Home UiState data class
│       │   │   │   │   │   ├── HomeViewModelFactory.kt    # Factory injecting AppRepository
│       │   │   │   │   │   └── components/
│       │   │   │   │   │       ├── PiggyBankCard.kt           # Balance/progress card (animated)
│       │   │   │   │   │       ├── LevelCard.kt               # Level + XP display
│       │   │   │   │   │       ├── MissionSummaryCard.kt      # 2 in-progress mission previews
│       │   │   │   │   │       ├── TransactionHistoryCard.kt  # Expandable transaction list
│       │   │   │   │   │       ├── DepositModal.kt            # Deposit amount picker + quicks
│       │   │   │   │   │       ├── ConfettiOverlay.kt         # Post-deposit confetti animation
│       │   │   │   │   │       ├── HomeTopBar.kt              # Top bar (greeting, avatar, coins)
│       │   │   │   │   │       └── ResetSection.kt            # Reset confirmation UI
│       │   │   │   │   ├── goals/
│       │   │   │   │   │   ├── GoalsScreen.kt            # Goals list + add goal button
│       │   │   │   │   │   ├── GoalsViewModel.kt         # Goals VM (create, allocate, UiState inline)
│       │   │   │   │   │   ├── GoalsViewModelFactory.kt  # Factory injecting AppRepository
│       │   │   │   │   │   └── components/
│       │   │   │   │   │       ├── GoalCard.kt               # Goal progress card
│       │   │   │   │   │       ├── AddGoalModal.kt           # New goal form modal
│       │   │   │   │   │       └── EmojiPickerGrid.kt        # Emoji selection grid
│       │   │   │   │   ├── missions/
│       │   │   │   │   │   ├── MissionsScreen.kt         # Mission list with filter
│       │   │   │   │   │   ├── MissionsViewModel.kt      # Missions VM (filter, UiState inline)
│       │   │   │   │   │   ├── MissionsViewModelFactory.kt
│       │   │   │   │   │   └── components/
│       │   │   │   │   │       ├── MissionCard.kt            # Individual mission card
│       │   │   │   │   │       ├── MissionCategoryFilter.kt  # Filter chips (All/Daily/Weekly/Special)
│       │   │   │   │   │       └── MiniConfettiOverlay.kt    # Small confetti effect
│       │   │   │   │   ├── rewards/
│       │   │   │   │   │   ├── RewardsScreen.kt          # Reward shop grid
│       │   │   │   │   │   ├── RewardsViewModel.kt       # Rewards VM (redeem, confetti, UiState inline)
│       │   │   │   │   │   └── RewardsViewModelFactory.kt
│       │   │   │   │   ├── ranking/
│       │   │   │   │   │   ├── RankingScreen.kt          # Family leaderboard + challenge
│       │   │   │   │   │   ├── RankingViewModel.kt       # Ranking VM (UiState inline)
│       │   │   │   │   │   └── RankingViewModelFactory.kt
│       │   │   │   │   └── login/
│       │   │   │   │       ├── LoginScreen.kt            # Login/signup form (2-step wizard)
│       │   │   │   │       └── LoginViewModel.kt         # Login VM (UiState inline, DataStore)
│       │   │   │   └── theme/
│       │   │   │       ├── Color.kt                  # Light color scheme + gradient presets
│       │   │   │       ├── Type.kt                   # Nunito typography
│       │   │   │       ├── Shape.kt                  # Rounded shapes
│       │   │   │       └── Theme.kt                  # PokectBankTheme + AnimationSpecs
│       │   │   └── util/
│       │   │       ├── CurrencyFormatter.kt           # formatCurrency(amount: Double) → pt-BR string
│       │   │       └── DateFormatter.kt               # formatDateLabel(dateStr: String) → "Hoje"/"Ontem"/"dd/MM"
│       │   └── res/
│       │       ├── drawable/                      # Drawable resources (launcher icons)
│       │       ├── font/                           # Nunito font files
│       │       ├── mipmap-*/                       # Launcher icons (multiple densities)
│       │       ├── values/                         # strings.xml, colors.xml, themes.xml
│       │       └── xml/                            # Backup rules
│       └── test/java/com/pokect/bank/kids/
│           ├── data/
│           │   ├── local/
│           │   │   ├── TestDatabaseHelper.kt              # Creates in-memory test DB+repo
│           │   │   ├── PokectBankConvertersTest.kt        # TypeConverter roundtrip tests
│           │   │   └── dao/
│           │   │       ├── UserDaoTest.kt
│           │   │       ├── TransactionDaoTest.kt
│           │   │       ├── SavingsGoalDaoTest.kt
│           │   │       ├── MissionDaoTest.kt
│           │   │       ├── RewardDaoTest.kt
│           │   │       ├── FamilyMemberDaoTest.kt
│           │   │       └── WeeklyChallengeDaoTest.kt
│           │   ├── models/
│           │   │   ├── RewardModelTest.kt
│           │   │   └── WeeklyChallengeModelTest.kt
│           │   ├── preferences/
│           │   │   └── LoginPreferencesTest.kt
│           │   └── repository/
│           │       └── AppRepositoryTest.kt               # 567 lines, comprehensive
│           ├── di/
│           │   └── AppContainerTest.kt
│           └── ui/
│               ├── screens/
│               │   ├── home/
│               │   │   ├── HomeViewModelTest.kt
│               │   │   ├── TransactionGroupingTestUtil.kt
│               │   │   └── components/
│               │   │       └── ComponentsTest.kt          # Placeholder: explains absence
│               │   ├── goals/
│               │   │   └── GoalsViewModelTest.kt
│               │   ├── missions/
│               │   │   └── MissionsViewModelTest.kt
│               │   ├── ranking/
│               │   │   └── RankingViewModelTest.kt
│               │   └── rewards/
│               │       └── RewardsViewModelTest.kt
│               └── util/
│                   └── CurrencyFormatterTest.kt
├── build.gradle.kts                     # Root build config (plugin declarations only)
├── settings.gradle.kts                  # Repository config, project name "PokectBank"
├── gradle.properties                    # Kotlin code style, JVM args
├── gradle/libs.versions.toml            # Version catalog (all dependency versions)
├── gradle/wrapper/                      # Gradle wrapper (9.3.1)
├── gradlew                              # Gradle wrapper script (Unix)
├── gradlew.bat                          # Gradle wrapper script (Windows)
├── gradle/gradle-daemon-jvm.properties  # Gradle daemon JVM config
├── local.properties                     # Android SDK path (local, not committed)
├── AGENTS.md                            # GSD project context + conventions
├── .gitignore                           # Git ignore rules
├── .idea/                               # IntelliJ/Android Studio project config
└── .planning/                           # GSD planning artifacts
    └── codebase/                        # Codebase map documents
        ├── ARCHITECTURE.md
        ├── STRUCTURE.md
        ├── STACK.md
        ├── CONVENTIONS.md
        ├── CONCERNS.md
        ├── INTEGRATIONS.md
        └── TESTING.md
```

## Directory Purposes

**`app/src/main/java/com/pokect/bank/kids/data/local/`:**
- Purpose: Room database implementation — database class, type converters, and DAO interfaces
- Contains: `PokectBankDatabase.kt` (RoomDatabase subclass with seed data callback), `PokectBankConverters.kt` (TypeConverter class), `dao/` subdirectory with 7 DAO interfaces (one per entity)
- Key files: `PokectBankDatabase.kt`, `dao/UserDao.kt`

**`app/src/main/java/com/pokect/bank/kids/data/models/`:**
- Purpose: Room entity definitions and supporting enums/data classes
- Contains: 7 `@Entity` data classes (User, Transaction, SavingsGoal, Mission, Reward, FamilyMember, WeeklyChallenge) + 5 enums (TransactionType, GoalColor, MissionStatus, MissionDifficulty, MissionCategory) + 1 plain data class (AvatarOption)
- Key files: `User.kt`, `Mission.kt`, `Transaction.kt`

**`app/src/main/java/com/pokect/bank/kids/data/preferences/`:**
- Purpose: DataStore Preferences for persistent session state (login)
- Contains: `LoginPreferences.kt` — wraps DataStore to persist `isLoggedIn` (boolean) and `selectedAvatar` (int) across app restarts
- Key files: `LoginPreferences.kt`

**`app/src/main/java/com/pokect/bank/kids/data/repository/`:**
- Purpose: Business logic hub — all CRUD operations, DAO-to-StateFlow bridging, cross-screen event broadcasting
- Contains: `AppRepository.kt` (359 lines — all write operations, event broadcast, seed data migration), `RepositoryEvent.kt` (6 event types sealed interface)
- Key files: `AppRepository.kt`

**`app/src/main/java/com/pokect/bank/kids/di/`:**
- Purpose: Manual dependency injection — singleton holder for database, repository, and preferences
- Contains: `AppContainer.kt` (object with lazy database, repository, loginPreferences, reset support)
- Key files: `AppContainer.kt`

**`app/src/main/java/com/pokect/bank/kids/ui/navigation/`:**
- Purpose: In-app navigation configuration
- Contains: `NavGraph.kt` (NavHost with 6 composable routes + enter/exit transitions), `Screen.kt` (sealed class of 6 screen routes with icons)
- Key files: `NavGraph.kt`

**`app/src/main/java/com/pokect/bank/kids/ui/screens/home/`:**
- Purpose: Home/dashboard screen — balance overview, missions, transactions, deposit flow, reset flow
- Contains: `HomeScreen.kt` (main composable), `HomeViewModel.kt` (VM with deposit/confetti/reset logic), `HomeUiState.kt` (standalone UiState), `HomeViewModelFactory.kt`, `components/` (8 screen-local composables)
- Key files: `HomeScreen.kt`, `HomeViewModel.kt`

**`app/src/main/java/com/pokect/bank/kids/ui/screens/goals/`:**
- Purpose: Savings goals — list, create, allocate funds
- Contains: `GoalsScreen.kt`, `GoalsViewModel.kt` (includes UiState class, create/allocate logic), `GoalsViewModelFactory.kt`, `components/` (GoalCard, AddGoalModal, EmojiPickerGrid)
- Key files: `GoalsViewModel.kt`, `GoalsScreen.kt`

**`app/src/main/java/com/pokect/bank/kids/ui/screens/missions/`:**
- Purpose: Mission list with category filtering
- Contains: `MissionsScreen.kt`, `MissionsViewModel.kt` (includes UiState + MissionFilterCategory enum), `MissionsViewModelFactory.kt`, `components/` (MissionCard, MissionCategoryFilter, MiniConfettiOverlay)
- Key files: `MissionsViewModel.kt`, `MissionsScreen.kt`

**`app/src/main/java/com/pokect/bank/kids/ui/screens/rewards/`:**
- Purpose: Reward shop — browse, redeem with coin check, confirmation modal, confetti
- Contains: `RewardsScreen.kt`, `RewardsViewModel.kt` (includes RewardUi, RewardsUiState, redeem flow), `RewardsViewModelFactory.kt`
- Key files: `RewardsViewModel.kt`, `RewardsScreen.kt`

**`app/src/main/java/com/pokect/bank/kids/ui/screens/ranking/`:**
- Purpose: Family leaderboard sorted by XP, weekly challenge join
- Contains: `RankingScreen.kt`, `RankingViewModel.kt` (includes RankingMember, RankingUiState), `RankingViewModelFactory.kt`
- Key files: `RankingViewModel.kt`, `RankingScreen.kt`

**`app/src/main/java/com/pokect/bank/kids/ui/screens/login/`:**
- Purpose: Login/signup form — 2-step wizard with avatar selection, DataStore persistence
- Contains: `LoginScreen.kt`, `LoginViewModel.kt` (includes LoginUiState, tab switching, validation, DataStore save)
- Key files: `LoginViewModel.kt`, `LoginScreen.kt`

**`app/src/main/java/com/pokect/bank/kids/ui/theme/`:**
- Purpose: Material 3 design system — colors, typography, shapes, animation specs
- Contains: `Color.kt` (light color scheme), `Type.kt` (Nunito font family), `Shape.kt` (rounded shapes), `Theme.kt` (theme composable + AnimationSpecs object)
- Key files: `Theme.kt`, `Color.kt`

**`app/src/main/java/com/pokect/bank/kids/ui/components/`:**
- Purpose: Shared reusable composables (cross-screen)
- Contains: `AvatarDisplay.kt` — shared avatar rendering component used by login and home screens
- Key files: `AvatarDisplay.kt`

**`app/src/main/java/com/pokect/bank/kids/util/`:**
- Purpose: Top-level utility functions for formatting
- Contains: `CurrencyFormatter.kt` (pt-BR currency), `DateFormatter.kt` (relative date labels)
- Key files: `CurrencyFormatter.kt`

**`app/src/test/java/com/pokect/bank/kids/`:**
- Purpose: Unit tests mirroring source structure
- Contains: Mirror of the source tree — `data/local/`, `data/local/dao/`, `data/models/`, `data/preferences/`, `data/repository/`, `di/`, `ui/screens/*/`, `ui/util/`. Test runner: Robolectric (for Room tests), StandardTestDispatcher (for coroutine tests)
- Key files: `data/repository/AppRepositoryTest.kt` (567 lines, most comprehensive test file), `data/local/TestDatabaseHelper.kt` (test setup helper)

## Key File Locations

**Entry Points:**
- `app/src/main/java/com/pokect/bank/kids/MainActivity.kt`: Single Android activity — app lifecycle, theme, Scaffold, auth gate
- `app/src/main/AndroidManifest.xml`: App manifest — declares MainActivity as LAUNCHER, portrait-only, singleTask

**Configuration:**
- `app/build.gradle.kts`: App module build config — SDK versions (compileSdk=36, minSdk=26, targetSdk=36), Compose, Room KSP, DataStore, test dependencies
- `gradle/libs.versions.toml`: Centralized version catalog for all dependencies
- `build.gradle.kts`: Root project — plugin declarations only
- `settings.gradle.kts`: Repository configuration (Google, Maven Central)
- `gradle.properties`: Kotlin code style (official), JVM args

**Core Logic:**
- `data/repository/AppRepository.kt`: All business operations — deposit, createGoal, allocateToGoal, redeemReward, joinWeeklyChallenge, resetData. Bridges Room DAO Flows to StateFlows. Event broadcasting.
- `data/local/PokectBankDatabase.kt`: Room database definition — 7 entities, 7 DAOs, seed data callback (SQL statements), double-checked-locked singleton, in-memory test instance helper, destructive migration fallback
- `data/preferences/LoginPreferences.kt`: DataStore wrapper — persists `isLoggedIn` and `selectedAvatar`, provides Flow-based observation

**Data Models:**
- `data/models/User.kt`: User entity — name, level, balance, goal, xp, coins, badges, selectedAvatar
- `data/models/Transaction.kt`: Transaction entity + TransactionType enum (DEPOSIT, WITHDRAW, REWARD, MISSION, GOAL)
- `data/models/SavingsGoal.kt`: SavingsGoal entity + GoalColor enum (PRIMARY, SECONDARY, SUCCESS, WARNING)
- `data/models/Mission.kt`: Mission entity + 3 enums (MissionStatus, MissionDifficulty, MissionCategory)
- `data/models/Reward.kt`: Reward entity — has cost (coins), isNew flag, redeemed flag
- `data/models/FamilyMember.kt`: FamilyMember entity — name, level, coins, xp, streak, isCurrentUser flag
- `data/models/WeeklyChallenge.kt`: WeeklyChallenge entity — rewardXp, rewardCoins, endDate, participants list, hasJoined
- `data/models/AvatarOption.kt`: Plain data class — 6 avatar options for login screen avatar selection

**Navigation:**
- `ui/navigation/NavGraph.kt`: NavHost composable — 6 routes with slide+fade transitions, ViewModel creation via factories
- `ui/navigation/Screen.kt`: Sealed class — 6 screen definitions (Home, Goals, Missions, Rewards, Ranking, Login) with routes, titles, and icons. `bottomNavTabs` list excludes Login.

**Dependency Injection:**
- `di/AppContainer.kt`: Singleton object — `init(context)` to set application context, lazy `database` and `repository` with reset support (`resetRepository()` clears DataStore + drops DB instance)

**Testing:**
- `data/local/TestDatabaseHelper.kt`: Test utility — creates in-memory Room database + AppRepository for test setup
- `data/repository/AppRepositoryTest.kt`: Most comprehensive test file (567 lines) — covers seed data assertions, deposit, goal creation, reward redemption, join challenge, reset
- DAO tests: One per entity — `UserDaoTest.kt`, `TransactionDaoTest.kt`, etc. — testing CRUD operations with in-memory DB

## Naming Conventions

**Files:**
- PascalCase: `HomeScreen.kt`, `HomeViewModel.kt`, `HomeUiState.kt`, `HomeViewModelFactory.kt` — screen name prefix shared by all files in the screen's directory
- Test files: `*Test.kt` suffix, mirroring the class under test — e.g., `AppRepositoryTest.kt`, `HomeViewModelTest.kt`
- Build files: `build.gradle.kts` (Kotlin DSL), `libs.versions.toml` (version catalog)
- Resource files: `strings.xml`, `themes.xml` in `res/values/`
- DAO files: `EntityNameDao.kt` — e.g., `UserDao.kt`, `TransactionDao.kt`

**Directories:**
- Feature/domain grouping under `ui/screens/<feature>/` — e.g., `home/`, `goals/`, `missions/`, `rewards/`, `ranking/`, `login/`
- Each screen directory has a `components/` subdirectory for screen-local composables — e.g., `home/components/DepositModal.kt`
- Data layer grouping: `data/local/` (Room), `data/local/dao/` (DAOs), `data/models/` (entities), `data/preferences/` (DataStore), `data/repository/` (business logic)
- Test directory mirrors source structure identically under `app/src/test/`

**Source root:**
- `app/src/main/java/com/pokect/bank/kids/` — main source root
- `app/src/test/java/com/pokect/bank/kids/` — test source root

## Where to Add New Code

**New Feature (new screen):**
- Primary code: `app/src/main/java/com/pokect/bank/kids/ui/screens/<featurename>/`
  - Create: `<Feature>Screen.kt`, `<Feature>ViewModel.kt` (with UiState inline or as separate file), `<Feature>ViewModelFactory.kt`
  - If screen has local composables: `components/` subdirectory
- Tests: Mirror at `app/src/test/java/com/pokect/bank/kids/ui/screens/<featurename>/<Feature>ViewModelTest.kt`
- Navigation: Add route to `Screen.kt` sealed class, add composable route in `NavGraph.kt`
- If feature has data: Add entity to `data/models/`, DAO to `data/local/dao/`, register in `PokectBankDatabase.kt`

**New Database Entity:**
- Model: `app/src/main/java/com/pokect/bank/kids/data/models/<NewEntity>.kt` — use `@Entity` annotation with `@PrimaryKey(autoGenerate = true) val uid: Long = 0` and unique index on `id: String`
- DAO: `app/src/main/java/com/pokect/bank/kids/data/local/dao/<NewEntity>Dao.kt` — include `observe*()` Flow method and CRUD methods
- Database: Register entity in `PokectBankDatabase.kt` `@Database(entities = [...])` array, add abstract DAO accessor method
- Repository: Inject DAO into `AppRepository` constructor, add bridge collector in `init{}`, expose `StateFlow`, add read/write methods
- DI: Update `AppContainer.kt` to pass new DAO to repository constructor
- Seed: Add SQL INSERT statements to `SeedDatabaseCallback.onCreate()`
- TypeConverters: If entity has enums or complex types, add converters to `PokectBankConverters.kt`
- Tests: Mirror DAO test (`<NewEntity>DaoTest.kt`), model test, and update `AppRepositoryTest.kt`

**New Shared Component:**
- Cross-screen: `app/src/main/java/com/pokect/bank/kids/ui/components/<ComponentName>.kt`
- Screen-local: `app/src/main/java/com/pokect/bank/kids/ui/screens/<feature>/components/<ComponentName>.kt`
- Use Compose parameter conventions: `modifier: Modifier = Modifier` as trailing parameter, callback lambdas with defaults `onClick: () -> Unit = {}`

**New Utility Function:**
- `app/src/main/java/com/pokect/bank/kids/util/<FunctionName>.kt` — top-level function, public visibility
- Tests: `app/src/test/java/com/pokect/bank/kids/ui/util/<FunctionName>Test.kt`

**New DataStore Preference:**
- Model: Add fields to `data/preferences/LoginPreferences.kt` or create new preferences class
- Keys: Define `*PreferencesKey` constants in companion object
- Apply in ViewModel: Collect Flow or call suspend setter

**Adding to an existing screen:**
- UI changes: Modify the screen's composable file or add new components in its `components/` directory
- State changes: Add fields to the UiState data class, add methods to the ViewModel
- If UiState is inline in the ViewModel file and grows large (>50 lines), extract to a separate `<Feature>UiState.kt` file following the `HomeUiState.kt` pattern

## Special Directories

**`app/build/`:**
- Purpose: Gradle build output — compiled classes, APKs, processed resources
- Generated: Yes (by Gradle)
- Committed: No (in `.gitignore`)

**`.gradle/`:**
- Purpose: Gradle cache — checksums, execution history, build output cleanup
- Generated: Yes (by Gradle)
- Committed: No (in `.gitignore`)

**`.idea/`:**
- Purpose: IntelliJ/Android Studio IDE configuration — run configurations, code style, compiler settings, deployment targets
- Generated: Partially (some files auto-generated by IDE)
- Committed: Partially (project-level config like `compiler.xml`, `gradle.xml` committed; `caches/` may be ignored)

**`gradle/wrapper/`:**
- Purpose: Gradle wrapper binaries — ensures consistent Gradle version (9.3.1) across developer machines
- Generated: No (committed deliberately — includes `gradle-wrapper.jar` and `gradle-wrapper.properties`)
- Committed: Yes

**`.planning/`:**
- Purpose: GSD workflow artifacts — codebase map, phase plans, specifications
- Generated: Yes (by GSD commands)
- Committed: Yes (part of the Git history for project continuity)

**`app/src/main/res/font/`:**
- Purpose: Custom font files — Nunito font family for the Material 3 theme
- Generated: No (bundled asset)
- Committed: Yes

---

*Structure analysis: 2026-05-21*
