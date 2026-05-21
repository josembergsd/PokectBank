package com.pokect.bank.kids.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pokect.bank.kids.data.local.PokectBankDatabase
import com.pokect.bank.kids.data.models.GoalColor
import com.pokect.bank.kids.data.models.SavingsGoal
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
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class SavingsGoalDaoTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var database: PokectBankDatabase
    private lateinit var dao: SavingsGoalDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PokectBankDatabase::class.java)
            .build()
        dao = database.savingsGoalDao()
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    private fun createGoal(id: String, title: String, currentAmount: Double = 0.0) = SavingsGoal(
        id = id,
        title = title,
        icon = "🎮",
        targetAmount = 500.0,
        currentAmount = currentAmount,
        daysLeft = 30,
        color = GoalColor.PRIMARY
    )

    @Test
    fun `insert and observe goals`() = runTest(testDispatcher) {
        dao.insertGoal(createGoal("g1", "Goal 1"))
        dao.insertGoal(createGoal("g2", "Goal 2"))
        dao.insertGoal(createGoal("g3", "Goal 3"))

        val goals = dao.observeGoals().first()
        assertEquals(3, goals.size)
    }

    @Test
    fun `getGoals returns all inserted goals`() = runTest(testDispatcher) {
        dao.insertGoal(createGoal("g1", "Goal 1"))
        dao.insertGoal(createGoal("g2", "Goal 2"))

        val goals = dao.getGoals()
        assertEquals(2, goals.size)
    }

    @Test
    fun `update goal changes currentAmount`() = runTest(testDispatcher) {
        val goal = createGoal("g1", "Videogame", currentAmount = 0.0)
        dao.insertGoal(goal)

        // Read back to get auto-generated uid
        val inserted = dao.getGoals().first()
        assertEquals("g1", inserted.id)
        val updated = inserted.copy(currentAmount = 50.0)
        dao.updateGoal(updated)

        val goals = dao.getGoals()
        assertEquals(50.0, goals.first().currentAmount)
    }

    @Test
    fun `deleteAll clears all goals`() = runTest(testDispatcher) {
        dao.insertGoal(createGoal("g1", "Goal 1"))
        dao.insertGoal(createGoal("g2", "Goal 2"))
        dao.insertGoal(createGoal("g3", "Goal 3"))

        dao.deleteAll()

        val goals = dao.observeGoals().first()
        assertTrue(goals.isEmpty())
    }
}
