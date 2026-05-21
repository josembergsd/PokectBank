# Testing Patterns

**Analysis Date:** 2026-05-21

## Test Framework

**Runner:**
- JUnit 4.13.2 — Primary test runner
- Kotlin Test 2.1.0 (`org.jetbrains.kotlin:kotlin-test`) — Kotlin assertion library (preferred over JUnit assertions)
- kotlinx-coroutines-test 1.8.1 — Coroutine test dispatchers and `runTest`
- Robolectric 4.14.1 — Android framework simulation for unit tests (no emulator needed)
- Compose UI Test JUnit4 — `androidx.compose.ui:ui-test-junit4` (available but not actively used)
- Compose UI Test Manifest — `androidx.compose.ui:ui-test-manifest` (debug-only dependency)
- AndroidX Test Core 1.6.1 — `ApplicationProvider` for test contexts

**Assertion Library:**
- Kotlin Test (`kotlin.test.*`) is the primary assertion library: `assertEquals`, `assertTrue`, `assertFalse`, `assertNotNull`, `assertNull`
- JUnit assertions not used directly

**Run Commands:**
```bash
./gradlew test                    # Run all unit tests (requires connected device or Robolectric)
./gradlew testDebugUnitTest       # Run all debug unit tests
./gradlew test --tests "*Test"    # Run all test classes matching pattern
```

**Config:** No dedicated test configuration file. Dependencies declared in `app/build.gradle.kts` lines 78-85 and `gradle/libs.versions.toml`. Robolectric is configured implicitly via `@RunWith(RobolectricTestRunner::class)`.

## Test File Organization

**Location:**
- All tests under `app/src/test/java/com/pokect/bank/kids/`
- Mirror the source package structure exactly:
  - Source: `data/repository/AppRepository.kt` → Test: `data/repository/AppRepositoryTest.kt`
  - Source: `data/local/dao/UserDao.kt` → Test: `data/local/dao/UserDaoTest.kt`
  - Source: `ui/screens/home/HomeViewModel.kt` → Test: `ui/screens/home/HomeViewModelTest.kt`
  - Source: `util/CurrencyFormatter.kt` → Test: `ui/util/CurrencyFormatterTest.kt`
  - Source: `data/preferences/LoginPreferences.kt` → Test: `data/preferences/LoginPreferencesTest.kt`
  - Source: `di/AppContainer.kt` → Test: `di/AppContainerTest.kt`

**Naming:**
- All test files use `*Test.kt` suffix matching the class under test
- No `*Spec.kt` or `*Should.kt` naming
- Test helper files use descriptive names: `TestDatabaseHelper.kt`, `TransactionGroupingTestUtil.kt`

**Structure:**
```
app/src/test/java/com/pokect/bank/kids/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   ├── FamilyMemberDaoTest.kt
│   │   │   ├── MissionDaoTest.kt
│   │   │   ├── RewardDaoTest.kt
│   │   │   ├── SavingsGoalDaoTest.kt
│   │   │   ├── TransactionDaoTest.kt
│   │   │   ├── UserDaoTest.kt
│   │   │   └── WeeklyChallengeDaoTest.kt
│   │   ├── PokectBankConvertersTest.kt
│   │   └── TestDatabaseHelper.kt
│   ├── models/
│   │   ├── RewardModelTest.kt
│   │   └── WeeklyChallengeModelTest.kt
│   ├── preferences/
│   │   └── LoginPreferencesTest.kt
│   └── repository/
│       └── AppRepositoryTest.kt
├── di/
│   └── AppContainerTest.kt
└── ui/
    ├── screens/
    │   ├── goals/
    │   │   └── GoalsViewModelTest.kt
    │   ├── home/
    │   │   ├── components/
    │   │   │   └── ComponentsTest.kt          (placeholder)
    │   │   ├── HomeViewModelTest.kt
    │   │   └── TransactionGroupingTestUtil.kt  (helper)
    │   ├── missions/
    │   │   └── MissionsViewModelTest.kt
    │   ├── ranking/
    │   │   └── RankingViewModelTest.kt
    │   └── rewards/
    │       └── RewardsViewModelTest.kt
    └── util/
        └── CurrencyFormatterTest.kt
```

## Test Structure

**Suite Organization (DAO tests — `UserDaoTest.kt`):**
```kotlin
@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class UserDaoTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var database: PokectBankDatabase
    private lateinit var dao: UserDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PokectBankDatabase::class.java)
            .build()
        dao = database.userDao()
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    private fun createTestUser() = User(
        name = "Test User", level = 3, balance = 100.0,
        goal = 500.0, xp = 300, xpToNextLevel = 1000,
        totalXp = 3000, streak = 5, coins = 50,
        badges = listOf("⭐", "🎯"), selectedAvatar = 2
    )

    @Test
    fun `insert and observe user`() = runTest(testDispatcher) {
        val user = createTestUser()
        dao.insertUser(user)
        val observed = dao.observeUser().first()
        assertEquals("Test User", observed.name)
    }
}
```

**Suite Organization (Repository tests — `AppRepositoryTest.kt`):**
```kotlin
@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AppRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: PokectBankDatabase
    private lateinit var repository: AppRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PokectBankDatabase::class.java)
            .addCallback(PokectBankDatabase.SeedDatabaseCallback())
            .build()
        repository = AppRepository(
            userDao = database.userDao(),
            transactionDao = database.transactionDao(),
            savingsGoalDao = database.savingsGoalDao(),
            missionDao = database.missionDao(),
            rewardDao = database.rewardDao(),
            familyMemberDao = database.familyMemberDao(),
            weeklyChallengeDao = database.weeklyChallengeDao(),
            database = database
        )
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }
}
```

**Suite Organization (ViewModel tests — `HomeViewModelTest.kt`):**
```kotlin
@RunWith(RobolectricTestRunner::class)
class HomeViewModelTest {

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state matches repository seed data`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = HomeViewModel(repo)
        delay(500)
        val state = vm.uiState.value
        assertEquals("Lucas", state.userName)
    }
}
```

**Patterns:**
- **Setup:** All tests that touch Android/Room use `@RunWith(RobolectricTestRunner::class)`. DAO/repository tests use `@OptIn(ExperimentalCoroutinesApi::class)` for test dispatchers. `Dispatchers.setMain()` in `@Before`, `Dispatchers.resetMain()` in `@After`.
- **Database creation:** In-memory Room database (`Room.inMemoryDatabaseBuilder()`) for DAO and repository tests. Repository tests add `SeedDatabaseCallback()` for pre-seeded data. ViewModel tests use `TestDatabaseHelper.createTestRepository()` which creates a test database with seed callback.
- **Coroutine testing:** DAO tests use `runTest(testDispatcher)`. Repository tests use `runTest(testDispatcher)` with `StandardTestDispatcher()` for manual time control (`advanceUntilIdle()`). ViewModel tests use `runBlocking(Dispatchers.Unconfined)` with `delay(500)` for async operations to settle.
- **Teardown:** `database.close()` to release Room resources, `Dispatchers.resetMain()` to reset dispatcher state.
- **Section comments:** Test files use `// === Section Name ===` headers to group related tests (e.g., `// === Seed Data Tests ===`, `// === Event Flow Tests ===`, `// === Write Operation Tests ===`).

**Test Method Naming:**
- Backtick-enclosed descriptive names in Portuguese or English: `` fun `deposit increases balance, coins, and xp`() ``
- Describes the expected behavior in plain language
- Examples: `` `repository initializes with Lucas user` ``, `` `redeemReward fails with insufficient coins` ``, `` `initial state loads all goals from repository` ``

## Mocking

**Framework:** No mocking framework used (no Mockito, no MockK)

**Patterns:**
- **Real Room in-memory databases** are used instead of mocking DAOs. Created via `Room.inMemoryDatabaseBuilder()` in test setup. This provides realistic SQLite behavior.
- **Real DataStore** in temp files for `LoginPreferencesTest.kt`:
  ```kotlin
  testFile = File(context.filesDir, "test_${java.util.UUID.randomUUID()}.preferences_pb")
  val testDataStore = PreferenceDataStoreFactory.create(
      scope = testScope,
      produceFile = { testFile }
  )
  loginPreferences = LoginPreferences(testDataStore)
  ```
- **Real repository** is created and injected into ViewModels for ViewModel tests — no mocking. `TestDatabaseHelper.createTestRepository()` creates a full `AppRepository` backed by an in-memory Room database with seed data.
- **Test dispatchers** (`StandardTestDispatcher`, `UnconfinedTestDispatcher`) are set as `Dispatchers.Main` to control coroutine execution in tests.
- **Robolectric** provides Android framework shadows (Context, ApplicationProvider) instead of mocking the Android SDK.

**What to Mock:**
- DAOs are NOT mocked — tested against real in-memory SQLite
- Repository is NOT mocked — tested against real Room database
- Android Context is NOT mocked — provided by Robolectric `ApplicationProvider`

**What NOT to Mock:**
- Nothing is mocked in this codebase. All tests are integration-level tests using real components.

## Fixtures and Factories

**Test Data Factory Functions (per test class):**
```kotlin
// UserDaoTest.kt
private fun createTestUser() = User(
    name = "Test User", level = 3, balance = 100.0, goal = 500.0,
    xp = 300, xpToNextLevel = 1000, totalXp = 3000,
    streak = 5, coins = 50, badges = listOf("⭐", "🎯"), selectedAvatar = 2
)

// TransactionDaoTest.kt
private fun createTransaction(
    id: String,
    type: TransactionType = TransactionType.DEPOSIT,
    title: String = "Test Transaction",
    amount: Double = 10.0
) = Transaction(
    id = id, type = type, title = title, amount = amount,
    date = "2026-05-21", icon = "💰"
)

// SavingsGoalDaoTest.kt
private fun createGoal(id: String, title: String, currentAmount: Double = 0.0) = SavingsGoal(
    id = id, title = title, icon = "🎮", targetAmount = 500.0,
    currentAmount = currentAmount, daysLeft = 30, color = GoalColor.PRIMARY
)
```

**Shared Test Helper (`TestDatabaseHelper.kt`):**
```kotlin
object TestDatabaseHelper {
    fun createTestRepository(): AppRepository {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = PokectBankDatabase.getTestInstance(context)
        return AppRepository(
            userDao = database.userDao(),
            transactionDao = database.transactionDao(),
            savingsGoalDao = database.savingsGoalDao(),
            missionDao = database.missionDao(),
            rewardDao = database.rewardDao(),
            familyMemberDao = database.familyMemberDao(),
            weeklyChallengeDao = database.weeklyChallengeDao(),
            database = database
        )
    }
    fun createTestDatabase(): PokectBankDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return PokectBankDatabase.getTestInstance(context)
    }
}
```

**Seed Data:**
- `PokectBankDatabase.SeedDatabaseCallback()` — SQL-based seed inserts for all 7 entities (user Lucas, 5 transactions, 3 goals, 4 missions, 4 rewards, 5 family members, 1 weekly challenge)
- `PokectBankDatabase.getTestInstance()` — provides in-memory database with seed callback for tests
- Repository tests rely on seed data to test initial state verification

**Location:**
- Factory functions are private methods within each test class
- Shared helpers in dedicated files: `TestDatabaseHelper.kt`, `TransactionGroupingTestUtil.kt`

## Coverage

**Requirements:** No explicit coverage tool configured. No `jacoco` plugin, no `kover` plugin, no coverage thresholds enforced.

**View Coverage:**
```bash
# No coverage command configured. Would require adding jacoco or kover plugin.
```

**Manual Coverage Assessment:**
- All 7 Room DAOs have dedicated test classes (`UserDaoTest.kt`, `TransactionDaoTest.kt`, etc.)
- All 6 ViewModels have dedicated test classes (`HomeViewModelTest.kt`, `GoalsViewModelTest.kt`, etc.)
- `AppRepository` has comprehensive test coverage (`AppRepositoryTest.kt` — 567 lines, 32 tests)
- `LoginPreferences` has dedicated tests (`LoginPreferencesTest.kt` — 115 lines, 6 tests)
- `AppContainer` DI singleton has tests (`AppContainerTest.kt` — 87 lines, 5 tests)
- Type converters have dedicated tests (`PokectBankConvertersTest.kt` — 144 lines, 13 tests)
- Utility functions tested inline within ViewModel tests or in dedicated files (`CurrencyFormatterTest.kt`)
- **Compose UI composables are NOT tested** — `ComponentsTest.kt` is a placeholder that only verifies compilation (`assert(true)`)

## Test Types

**Unit Tests (no Android dependency):**
- `PokectBankConvertersTest.kt` — Tests Room type converters (enum ↔ String, List<String> ↔ CSV, Instant ↔ Long). No Robolectric needed. Uses plain JUnit with `kotlin.test` assertions.
- `RewardModelTest.kt` — Tests data model validation logic. Plain JUnit.
- `WeeklyChallengeModelTest.kt` — Tests data model validation logic. Plain JUnit.

**Integration Tests (Room + real SQLite, no emulator):**
- All 7 DAO test classes (`UserDaoTest.kt`, `TransactionDaoTest.kt`, `SavingsGoalDaoTest.kt`, `MissionDaoTest.kt`, `RewardDaoTest.kt`, `FamilyMemberDaoTest.kt`, `WeeklyChallengeDaoTest.kt`) — Use Robolectric + in-memory Room. Verify insert, query, update, observe, and deleteAll operations.
- `AppRepositoryTest.kt` — Full repository integration with all 7 DAOs. Tests seed data, write operations, event emission, cross-entity consistency, shutdown guard, and reset functionality. Uses `StandardTestDispatcher` with `advanceUntilIdle()` for precise coroutine control.
- `LoginPreferencesTest.kt` — Tests DataStore read/write/clear with temp files. Uses `UnconfinedTestDispatcher` + `PreferenceDataStoreFactory.create()`.

**ViewModel Tests (with real repository):**
- All 5 feature ViewModel tests (`HomeViewModelTest.kt`, `GoalsViewModelTest.kt`, `MissionsViewModelTest.kt`, `RewardsViewModelTest.kt`, `RankingViewModelTest.kt`) — Use Robolectric + real repository from `TestDatabaseHelper`. Verify initial state loads from seed data, UI state transitions, and event-driven refresh. Use `runBlocking(Dispatchers.Unconfined)` with `delay(500)` for async settlement.
- `AppContainerTest.kt` — Tests DI container lifecycle (init, repository creation, seed data, reset).

**E2E Tests:**
- Not used. No `androidTest/` directory, no Espresso, no UI Automator. All tests run as unit tests via Robolectric.

**Compose UI Tests:**
- Not implemented. `ComponentsTest.kt` at `ui/screens/home/components/ComponentsTest.kt` is an explicit placeholder:
  ```kotlin
  /**
   * Compose UI tests require Robolectric or instrumentation test runner.
   * These are placeholder tests that verify the composables compile.
   * Full UI tests should be added as androidTest when Robolectric is configured.
   */
  class ComponentsTest {
      @Test
      fun placeholder_composables_compile() {
          assert(true)
      }
  }
  ```

## Common Patterns

**Async Testing (Repository with StandardTestDispatcher):**
```kotlin
@Test
fun `deposit emits UserUpdated event`() = runTest(testDispatcher) {
    val events = mutableListOf<RepositoryEvent>()
    val job = launch(testDispatcher) {
        repository.events.collect { events.add(it) }
    }
    repository.deposit(10.0)
    advanceUntilIdle()
    assertTrue(events.contains(RepositoryEvent.UserUpdated))
    job.cancel()
}
```
- Use `launch(testDispatcher)` to collect events in background
- Call `advanceUntilIdle()` to process pending coroutines
- Always cancel the collection job to prevent leaks

**Async Testing (ViewModel with runBlocking + delay):**
```kotlin
@Test
fun `initial state matches repository seed data`() = runBlocking(Dispatchers.Unconfined) {
    val repo = TestDatabaseHelper.createTestRepository()
    val vm = HomeViewModel(repo)
    delay(500)
    val state = vm.uiState.value
    assertEquals("Lucas", state.userName)
}
```
- `runBlocking(Dispatchers.Unconfined)` for immediate execution
- `delay(500)` to allow ViewModel `init {}` coroutines to complete
- Read `vm.uiState.value` synchronously after delay

**Error Testing:**
```kotlin
@Test
fun `deposit rejects amount below 1`() = runTest(testDispatcher) {
    val initialUser = repository.getUser()
    val result = repository.deposit(0.5)
    assertTrue(result.isFailure)
    val unchangedUser = repository.getUser()
    assertEquals(initialUser.balance, unchangedUser.balance)
}

@Test
fun `allocateToGoal fails when goal not found`() = runTest(testDispatcher) {
    val result = repository.allocateToGoal("nonexistent", 10.0)
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is IllegalArgumentException)
}
```
- Test both success and failure paths
- Verify `Result.isSuccess` / `Result.isFailure`
- Check exception type with `result.exceptionOrNull() is IllegalArgumentException`
- Verify state is unchanged after failed operations

**Event Testing:**
```kotlin
@Test
fun `createGoal emits GoalsChanged event`() = runTest(testDispatcher) {
    val events = mutableListOf<RepositoryEvent>()
    val job = launch(testDispatcher) {
        repository.events.collect { events.add(it) }
    }
    repository.createGoal("Test Goal", "🎯", 100.0, 30)
    advanceUntilIdle()
    assertTrue(events.contains(RepositoryEvent.GoalsChanged))
    job.cancel()
}
```

**Round-Trip / Converter Testing:**
```kotlin
@Test
fun `missionStatus converter round-trip`() {
    val original = MissionStatus.IN_PROGRESS
    val dbValue = converter.fromMissionStatus(original)
    val restored = converter.toMissionStatus(dbValue)
    assertEquals(original, restored)
}
```

**Enum Completeness Testing:**
```kotlin
@Test
fun `transactionType all values round-trip`() {
    for (value in TransactionType.entries) {
        val dbValue = converter.fromTransactionType(value)
        val restored = converter.toTransactionType(dbValue)
        assertEquals(value, restored)
    }
}
```

**Multi-Operation State Verification:**
```kotlin
@Test
fun `multiple rapid deposits produce correct final balance`() = runBlocking(Dispatchers.Unconfined) {
    val repo = TestDatabaseHelper.createTestRepository()
    val vm = HomeViewModel(repo)
    delay(500)
    val initialBalance = vm.uiState.value.balance
    vm.deposit(5.0)
    delay(200)
    vm.deposit(5.0)
    delay(500)
    assertEquals(initialBalance + 10.0, vm.uiState.value.balance)
}
```

**Idempotency Testing:**
```kotlin
@Test
fun `joinWeeklyChallenge is idempotent`() = runBlocking(Dispatchers.Unconfined) {
    val repo = TestDatabaseHelper.createTestRepository()
    val vm = RankingViewModel(repo)
    delay(500)
    vm.joinWeeklyChallenge("wc1")
    vm.joinWeeklyChallenge("wc1")
    delay(500)
    val state = vm.uiState.value
    val participants = state.weeklyChallenge?.participants ?: emptyList()
    assertEquals(3, participants.size)
    assertEquals(1, participants.count { it == "fm1" })
}
```

---

*Testing analysis: 2026-05-21*
