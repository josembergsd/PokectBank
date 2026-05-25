package com.pokect.bank.kids.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pokect.bank.kids.data.local.PokectBankDatabase
import com.pokect.bank.kids.data.models.Reward
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class RewardDaoTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var database: PokectBankDatabase
    private lateinit var dao: RewardDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PokectBankDatabase::class.java)
            .build()
        dao = database.rewardDao()
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    private fun createReward(
        id: String,
        title: String = "Test Reward",
        cost: Int = 50,
        redeemed: Boolean = false
    ) = Reward(
        id = id,
        title = title,
        description = "Test description",
        icon = "🛡️",
        cost = cost,
        isNew = true,
        redeemed = redeemed
    )

    @Test
    fun `insert and observe rewards`() = runTest(testDispatcher) {
        dao.insertReward(createReward("r1", "Reward 1"))
        dao.insertReward(createReward("r2", "Reward 2"))

        val rewards = dao.observeRewards().first()
        assertEquals(2, rewards.size)
        assertTrue(rewards.any { it.id == "r1" })
        assertTrue(rewards.any { it.id == "r2" })
    }

    @Test
    fun `insert reward emits on Flow`() = runTest(testDispatcher) {
        dao.insertReward(createReward("r1", "Shield", 50))

        val rewards = dao.observeRewards().first()
        assertEquals(1, rewards.size)
        assertEquals("r1", rewards[0].id)
        assertEquals("Shield", rewards[0].title)
        assertEquals(50, rewards[0].cost)
        assertFalse(rewards[0].redeemed)
    }

    @Test
    fun `update reward redeemed flag`() = runTest(testDispatcher) {
        val reward = createReward("r1", "Shield", 50, redeemed = false)
        dao.insertReward(reward)

        // Read back to get auto-generated uid
        val inserted = dao.getRewards().first()
        assertEquals("r1", inserted.id)
        assertFalse(inserted.redeemed)

        val updated = inserted.copy(redeemed = true)
        dao.updateReward(updated)

        val result = dao.getRewards().first()
        assertTrue(result.redeemed)
    }

    @Test
    fun `update reward changes title and cost`() = runTest(testDispatcher) {
        val reward = createReward("r1", "Original", 50)
        dao.insertReward(reward)

        val inserted = dao.getRewardByOriginalId("r1")
        assertNotNull(inserted)

        val updated = inserted.copy(title = "Updated", cost = 100)
        dao.updateReward(updated)

        val result = dao.getRewardByOriginalId("r1")
        assertNotNull(result)
        assertEquals("Updated", result.title)
        assertEquals(100, result.cost)
    }

    @Test
    fun `deleteAll clears all rewards`() = runTest(testDispatcher) {
        dao.insertReward(createReward("r1"))
        dao.insertReward(createReward("r2"))
        dao.insertReward(createReward("r3"))

        dao.deleteAll()

        val rewards = dao.observeRewards().first()
        assertTrue(rewards.isEmpty())
    }
}
