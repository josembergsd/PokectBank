package com.pokect.bank.kids.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pokect.bank.kids.data.local.PokectBankDatabase
import com.pokect.bank.kids.data.models.Mission
import com.pokect.bank.kids.data.models.MissionCategory
import com.pokect.bank.kids.data.models.MissionDifficulty
import com.pokect.bank.kids.data.models.MissionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
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
@OptIn(ExperimentalCoroutinesApi::class)
class MissionDaoTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var database: PokectBankDatabase
    private lateinit var dao: MissionDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PokectBankDatabase::class.java)
            .build()
        dao = database.missionDao()
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    private fun createMission(
        id: String,
        title: String = "Test Mission",
        status: MissionStatus = MissionStatus.AVAILABLE,
        category: MissionCategory = MissionCategory.DAILY
    ) = Mission(
        id = id,
        title = title,
        description = "Test description",
        xp = 50,
        coins = 10,
        progress = 0,
        maxProgress = 5,
        status = status,
        icon = "🎯",
        difficulty = MissionDifficulty.EASY,
        category = category
    )

    @Test
    fun `insert and observe missions`() = runTest(testDispatcher) {
        dao.insertMission(createMission("m1", "Mission 1"))
        dao.insertMission(createMission("m2", "Mission 2"))

        val missions = dao.observeMissions().first()
        assertEquals(2, missions.size)
        assertTrue(missions.any { it.id == "m1" })
        assertTrue(missions.any { it.id == "m2" })
    }

    @Test
    fun `insert mission emits on Flow`() = runTest(testDispatcher) {
        dao.insertMission(createMission("m1", "Guardar R\$5", MissionStatus.AVAILABLE))

        val missions = dao.observeMissions().first()
        assertEquals(1, missions.size)
        assertEquals("m1", missions[0].id)
        assertEquals("Guardar R\$5", missions[0].title)
        assertEquals(MissionStatus.AVAILABLE, missions[0].status)
    }

    @Test
    fun `insert missions with different statuses`() = runTest(testDispatcher) {
        dao.insertMission(createMission("m_avail", "Available", MissionStatus.AVAILABLE))
        dao.insertMission(createMission("m_prog", "In Progress", MissionStatus.IN_PROGRESS))
        dao.insertMission(createMission("m_comp", "Completed", MissionStatus.COMPLETED))
        dao.insertMission(createMission("m_lock", "Locked", MissionStatus.LOCKED))

        val missions = dao.observeMissions().first()
        assertEquals(4, missions.size)

        val byStatus = missions.groupBy { it.status }
        assertEquals(1, byStatus[MissionStatus.AVAILABLE]?.size ?: 0)
        assertEquals(1, byStatus[MissionStatus.IN_PROGRESS]?.size ?: 0)
        assertEquals(1, byStatus[MissionStatus.COMPLETED]?.size ?: 0)
        assertEquals(1, byStatus[MissionStatus.LOCKED]?.size ?: 0)
    }

    @Test
    fun `update mission changes fields`() = runTest(testDispatcher) {
        val mission = createMission("m1", "Original", MissionStatus.AVAILABLE)
        dao.insertMission(mission)

        // Read back to get auto-generated uid
        val inserted = dao.getMissionByOriginalId("m1")
        assertNotNull(inserted)
        assertEquals("Original", inserted.title)

        val updated = inserted.copy(title = "Updated", status = MissionStatus.IN_PROGRESS, progress = 3)
        dao.updateMission(updated)

        val result = dao.getMissionByOriginalId("m1")
        assertNotNull(result)
        assertEquals("Updated", result.title)
        assertEquals(MissionStatus.IN_PROGRESS, result.status)
        assertEquals(3, result.progress)
    }

    @Test
    fun `deleteAll clears all missions`() = runTest(testDispatcher) {
        dao.insertMission(createMission("m1"))
        dao.insertMission(createMission("m2"))
        dao.insertMission(createMission("m3"))

        dao.deleteAll()

        val missions = dao.observeMissions().first()
        assertTrue(missions.isEmpty())
    }
}
