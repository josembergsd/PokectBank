package com.pokect.bank.kids.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pokect.bank.kids.data.local.PokectBankDatabase
import com.pokect.bank.kids.data.models.User
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
        name = "Test User",
        level = 3,
        balance = 100.0,
        goal = 500.0,
        xp = 300,
        xpToNextLevel = 1000,
        totalXp = 3000,
        streak = 5,
        coins = 50,
        badges = listOf("⭐", "🎯"),
        selectedAvatar = 2
    )

    @Test
    fun `insert and observe user`() = runTest(testDispatcher) {
        val user = createTestUser()
        dao.insertUser(user)

        val observed = dao.observeUser().first()
        assertEquals("Test User", observed.name)
        assertEquals(100.0, observed.balance)
    }

    @Test
    fun `getUser returns inserted user`() = runTest(testDispatcher) {
        val user = createTestUser()
        dao.insertUser(user)

        val result = dao.getUser()
        assertNotNull(result)
        assertEquals("Test User", result.name)
    }

    @Test
    fun `update user changes fields`() = runTest(testDispatcher) {
        val user = createTestUser()
        dao.insertUser(user)

        // Read back to get auto-generated uid
        val inserted = dao.getUser()
        val updated = inserted.copy(balance = 250.0, level = 4)
        dao.updateUser(updated)

        val result = dao.getUser()
        assertEquals(250.0, result.balance)
        assertEquals(4, result.level)
    }

    @Test
    fun `getUser returns replaced data after insert with same id`() = runTest(testDispatcher) {
        val user = createTestUser()
        dao.insertUser(user)

        val replacement = user.copy(balance = 999.0)
        dao.insertUser(replacement)

        val result = dao.getUser()
        assertEquals(999.0, result.balance)
    }
}
