package com.pokect.bank.kids.ui.screens.goals

import com.pokect.bank.kids.data.local.TestDatabaseHelper
import com.pokect.bank.kids.data.models.GoalColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class GoalsViewModelTest {

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads all goals from repository`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = GoalsViewModel(repo)
        delay(500)
        val state = vm.uiState.value

        assertEquals(3, state.goals.size)
    }

    @Test
    fun `goals have required fields`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        delay(500)
        val goal = repo.getGoals().first()

        assertTrue(goal.id.isNotEmpty())
        assertTrue(goal.title.isNotEmpty())
        assertTrue(goal.icon.isNotEmpty())
        assertTrue(goal.targetAmount > 0)
        assertTrue(goal.currentAmount >= 0)
        assertTrue(goal.daysLeft >= 0)
        Unit
    }

    @Test
    fun `GoalColor has 4 category values`() {
        assertEquals(4, GoalColor.entries.size)
        assertTrue(GoalColor.entries.contains(GoalColor.PRIMARY))
        assertTrue(GoalColor.entries.contains(GoalColor.SECONDARY))
        assertTrue(GoalColor.entries.contains(GoalColor.SUCCESS))
        assertTrue(GoalColor.entries.contains(GoalColor.WARNING))
        Unit
    }

    @Test
    fun `progress percent calculates correctly for incomplete goals`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = GoalsViewModel(repo)
        delay(500)
        val goal = vm.uiState.value.goals.first { it.currentAmount < it.targetAmount }

        val progress = goal.currentAmount / goal.targetAmount
        assertTrue(progress in 0.0..1.0)
        Unit
    }

    @Test
    fun `progress percent is 1 for completed goals`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        delay(500)
        val completedGoal = repo.getGoals().find { it.currentAmount >= it.targetAmount }

        if (completedGoal != null) {
            val progress = completedGoal.currentAmount / completedGoal.targetAmount
            assertEquals(1.0, progress, 0.01)
        }
    }

    @Test
    fun `showAddGoalModal updates state`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = GoalsViewModel(repo)
        delay(500)

        assertFalse(vm.uiState.value.showAddGoalModal)

        vm.showAddGoalModal()

        assertTrue(vm.uiState.value.showAddGoalModal)
        Unit
    }

    @Test
    fun `hideAddGoalModal updates state`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = GoalsViewModel(repo)
        delay(500)
        vm.showAddGoalModal()

        vm.hideAddGoalModal()

        assertFalse(vm.uiState.value.showAddGoalModal)
        Unit
    }

    @Test
    fun `createGoal adds new goal to list`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = GoalsViewModel(repo)
        delay(500)
        val initialCount = vm.uiState.value.goals.size

        vm.createGoal(
            name = "Nova Meta",
            icon = "🎮",
            targetAmount = 1000.0,
            daysLeft = 30
        )

        delay(500)

        assertEquals(initialCount + 1, vm.uiState.value.goals.size)
    }

    @Test
    fun `createGoal assigns correct properties to new goal`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = GoalsViewModel(repo)
        delay(500)

        vm.createGoal(
            name = "Minha Meta",
            icon = "🚲",
            targetAmount = 500.0,
            daysLeft = 14
        )

        delay(500)

        val newGoal = vm.uiState.value.goals.last()

        assertEquals("Minha Meta", newGoal.title)
        assertEquals("🚲", newGoal.icon)
        assertEquals(500.0, newGoal.targetAmount)
        assertEquals(0.0, newGoal.currentAmount)
        assertEquals(14, newGoal.daysLeft)
    }

    @Test
    fun `createGoal hides modal after creation`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = GoalsViewModel(repo)
        delay(500)
        vm.showAddGoalModal()

        vm.createGoal("Test", "🎮", 100.0, 30)

        delay(500)

        assertFalse(vm.uiState.value.showAddGoalModal)
        Unit
    }

    @Test
    fun `createGoal cycles through GoalColor`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = GoalsViewModel(repo)
        delay(500)

        val colorsUsed = mutableListOf<GoalColor>()
        repeat(5) {
            vm.createGoal("Meta $it", "🎮", 100.0, 30)
            delay(500)
            colorsUsed.add(repo.getGoals().last().color)
        }

        // With 3 seed + 5 new goals, at least 3 distinct colors should be used
        assertTrue(colorsUsed.take(4).toSet().size >= 3)
        Unit
    }

    // === User Sync Tests ===

    @Test
    fun `initial state includes user from repository`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = GoalsViewModel(repo)
        delay(500)

        val state = vm.uiState.value
        assertNotNull(state.user)
        assertEquals("Lucas", state.user?.name)
    }

    @Test
    fun `allocateToGoal calls repository`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        // Deposit to have balance
        repo.deposit(50.0)
        delay(500)
        val vm = GoalsViewModel(repo)
        delay(500)
        vm.showAddGoalModal()

        vm.allocateToGoal("g1", 10.0)

        delay(500)

        assertFalse(vm.uiState.value.showAddGoalModal)
        assertNull(vm.uiState.value.error)
        Unit
    }

    @Test
    fun `allocateToGoal sets error on failure`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = GoalsViewModel(repo)
        delay(500)
        vm.showAddGoalModal()

        vm.allocateToGoal("nonexistent", 10.0)

        delay(500)

        assertNotNull(vm.uiState.value.error)
        Unit
    }
}
