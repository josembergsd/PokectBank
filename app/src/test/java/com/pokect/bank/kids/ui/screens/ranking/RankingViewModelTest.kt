package com.pokect.bank.kids.ui.screens.ranking

import com.pokect.bank.kids.data.local.TestDatabaseHelper
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class RankingViewModelTest {

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has 5 members sorted by XP descending`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = RankingViewModel(repo)
        delay(500)
        val state = vm.uiState.value

        assertEquals(5, state.members.size)
        assertEquals("Mamãe", state.members[0].member.name)
        assertEquals(9200, state.members[0].member.xp)
        assertEquals("Pedro", state.members[4].member.name)
        assertEquals(2100, state.members[4].member.xp)
    }

    @Test
    fun `positions are assigned correctly`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = RankingViewModel(repo)
        delay(500)
        val state = vm.uiState.value

        assertEquals(1, state.members[0].position)
        assertEquals(2, state.members[1].position)
        assertEquals(3, state.members[2].position)
        assertEquals(4, state.members[3].position)
        assertEquals(5, state.members[4].position)
    }

    @Test
    fun `currentUser is identified`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = RankingViewModel(repo)
        delay(500)
        val state = vm.uiState.value

        assertNotNull(state.currentUser)
        assertEquals("Lucas", state.currentUser?.name)
        assertTrue(state.currentUser?.isCurrentUser == true)
        Unit
    }

    @Test
    fun `weeklyChallenge is loaded`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = RankingViewModel(repo)
        delay(500)
        val state = vm.uiState.value

        assertNotNull(state.weeklyChallenge)
        assertEquals("🏆 Desafio da semana", state.weeklyChallenge?.title)
    }

    @Test
    fun `joinWeeklyChallenge sets hasJoined to true`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = RankingViewModel(repo)
        delay(500)

        vm.joinWeeklyChallenge("wc1")

        delay(500)

        val state = vm.uiState.value
        assertTrue(state.weeklyChallenge?.hasJoined == true)
        Unit
    }

    @Test
    fun `joinWeeklyChallenge adds user to participants`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = RankingViewModel(repo)
        delay(500)

        vm.joinWeeklyChallenge("wc1")

        delay(500)

        val state = vm.uiState.value
        assertTrue(state.weeklyChallenge?.participants?.contains("fm1") == true)
        Unit
    }

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
}
