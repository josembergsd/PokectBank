package com.pokect.bank.kids.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pokect.bank.kids.data.local.PokectBankDatabase
import com.pokect.bank.kids.data.models.WeeklyChallenge
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
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyChallengeDaoTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var database: PokectBankDatabase
    private lateinit var dao: WeeklyChallengeDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PokectBankDatabase::class.java)
            .build()
        dao = database.weeklyChallengeDao()
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    private fun createChallenge(
        id: String,
        title: String = "Test Challenge",
        hasJoined: Boolean = false,
        participants: List<String> = emptyList()
    ) = WeeklyChallenge(
        id = id,
        title = title,
        description = "Test description",
        rewardXp = 200,
        rewardCoins = 40,
        endDate = Instant.now().plus(Duration.ofDays(7)),
        participants = participants,
        hasJoined = hasJoined
    )

    @Test
    fun `insert and observe challenges`() = runTest(testDispatcher) {
        dao.insertChallenge(createChallenge("wc1", "Challenge 1"))
        dao.insertChallenge(createChallenge("wc2", "Challenge 2"))

        val challenges = dao.observeChallenges().first()
        assertEquals(2, challenges.size)
        assertTrue(challenges.any { it.id == "wc1" })
        assertTrue(challenges.any { it.id == "wc2" })
    }

    @Test
    fun `insert challenge emits on Flow`() = runTest(testDispatcher) {
        val endDate = Instant.now().plus(Duration.ofDays(7))
        val challenge = createChallenge("wc1", "Weekly Challenge", participants = listOf("fm2", "fm4"))
        dao.insertChallenge(challenge)

        val challenges = dao.observeChallenges().first()
        assertEquals(1, challenges.size)
        assertEquals("wc1", challenges[0].id)
        assertEquals("Weekly Challenge", challenges[0].title)
        assertEquals(200, challenges[0].rewardXp)
        assertEquals(40, challenges[0].rewardCoins)
        assertEquals(listOf("fm2", "fm4"), challenges[0].participants)
        assertFalse(challenges[0].hasJoined)
    }

    @Test
    fun `update challenge changes hasJoined and participants`() = runTest(testDispatcher) {
        val challenge = createChallenge("wc1", "Test", hasJoined = false, participants = listOf("fm2"))
        dao.insertChallenge(challenge)

        // Read back to get auto-generated uid
        val inserted = dao.getChallenges().first()
        assertEquals("wc1", inserted.id)
        assertFalse(inserted.hasJoined)

        val updated = inserted.copy(hasJoined = true, participants = listOf("fm2", "fm4"))
        dao.updateChallenge(updated)

        val result = dao.getChallenges().first()
        assertTrue(result.hasJoined)
        assertEquals(listOf("fm2", "fm4"), result.participants)
    }

    @Test
    fun `update challenge changes title and rewards`() = runTest(testDispatcher) {
        val challenge = createChallenge("wc1", "Original", hasJoined = false)
        dao.insertChallenge(challenge)

        val inserted = dao.getChallengeByOriginalId("wc1")
        assertNotNull(inserted)

        val updated = inserted.copy(title = "Updated", rewardXp = 300, rewardCoins = 60)
        dao.updateChallenge(updated)

        val result = dao.getChallengeByOriginalId("wc1")
        assertNotNull(result)
        assertEquals("Updated", result.title)
        assertEquals(300, result.rewardXp)
        assertEquals(60, result.rewardCoins)
    }

    @Test
    fun `deleteAll clears all challenges`() = runTest(testDispatcher) {
        dao.insertChallenge(createChallenge("wc1"))
        dao.insertChallenge(createChallenge("wc2"))

        dao.deleteAll()

        val challenges = dao.observeChallenges().first()
        assertTrue(challenges.isEmpty())
    }
}
