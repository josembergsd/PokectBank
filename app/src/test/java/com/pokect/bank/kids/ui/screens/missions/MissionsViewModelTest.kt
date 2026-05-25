package com.pokect.bank.kids.ui.screens.missions

import com.pokect.bank.kids.data.local.TestDatabaseHelper
import com.pokect.bank.kids.data.models.MissionCategory
import com.pokect.bank.kids.data.models.MissionStatus
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

@RunWith(RobolectricTestRunner::class)
class MissionsViewModelTest {

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads all missions from repository`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = MissionsViewModel(repo)
        delay(500)
        val state = vm.uiState.value

        assertEquals(4, state.allMissions.size)
        assertEquals(4, state.filteredMissions.size)
    }

    @Test
    fun `filter ALL returns all missions`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = MissionsViewModel(repo)
        delay(500)

        vm.selectFilter(MissionFilterCategory.ALL)

        assertEquals(4, vm.uiState.value.filteredMissions.size)
    }

    @Test
    fun `filter DAILY returns only DAILY category missions`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = MissionsViewModel(repo)
        delay(500)

        vm.selectFilter(MissionFilterCategory.DAILY)

        val filtered = vm.uiState.value.filteredMissions
        assertTrue(filtered.all { it.category == MissionCategory.DAILY })
        Unit
    }

    @Test
    fun `filter WEEKLY returns only WEEKLY category missions`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = MissionsViewModel(repo)
        delay(500)

        vm.selectFilter(MissionFilterCategory.WEEKLY)

        val filtered = vm.uiState.value.filteredMissions
        assertTrue(filtered.all { it.category == MissionCategory.WEEKLY })
        Unit
    }

    @Test
    fun `filter SPECIAL returns only SPECIAL category missions`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = MissionsViewModel(repo)
        delay(500)

        vm.selectFilter(MissionFilterCategory.SPECIAL)

        val filtered = vm.uiState.value.filteredMissions
        assertTrue(filtered.all { it.category == MissionCategory.SPECIAL })
        Unit
    }

    @Test
    fun `Mission model has required fields`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        delay(500)
        val mission = repo.getMissions().first()

        assertTrue(mission.icon.isNotEmpty())
        assertTrue(mission.description.isNotEmpty())
        assertTrue(mission.difficulty.name.isNotEmpty())
        assertTrue(mission.progress >= 0)
        assertTrue(mission.xp > 0)
        assertTrue(mission.coins >= 0)
        Unit
    }

    @Test
    fun `MissionStatus has 4 states`() {
        assertEquals(4, MissionStatus.entries.size)
        assertTrue(MissionStatus.entries.contains(MissionStatus.LOCKED))
        assertTrue(MissionStatus.entries.contains(MissionStatus.AVAILABLE))
        assertTrue(MissionStatus.entries.contains(MissionStatus.IN_PROGRESS))
        assertTrue(MissionStatus.entries.contains(MissionStatus.COMPLETED))
        Unit
    }

    @Test
    fun `MissionCategory has 3 values`() {
        assertEquals(3, MissionCategory.entries.size)
        assertTrue(MissionCategory.entries.contains(MissionCategory.DAILY))
        assertTrue(MissionCategory.entries.contains(MissionCategory.WEEKLY))
        assertTrue(MissionCategory.entries.contains(MissionCategory.SPECIAL))
        Unit
    }

    @Test
    fun `MissionFilterCategory has ALL plus 3 categories`() {
        assertEquals(4, MissionFilterCategory.entries.size)
    }

    @Test
    fun `MissionsChanged event reloads missions`() = runBlocking(Dispatchers.Unconfined) {
        val repo = TestDatabaseHelper.createTestRepository()
        val vm = MissionsViewModel(repo)
        delay(500)
        val initialCount = vm.uiState.value.allMissions.size

        // Join weekly challenge which creates a new mission and emits MissionsChanged
        repo.joinWeeklyChallenge("wc1")

        delay(500)

        assertEquals(initialCount + 1, vm.uiState.value.allMissions.size)
    }
}
