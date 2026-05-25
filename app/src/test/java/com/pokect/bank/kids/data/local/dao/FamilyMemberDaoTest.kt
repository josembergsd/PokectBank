package com.pokect.bank.kids.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pokect.bank.kids.data.local.PokectBankDatabase
import com.pokect.bank.kids.data.models.FamilyMember
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
class FamilyMemberDaoTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var database: PokectBankDatabase
    private lateinit var dao: FamilyMemberDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PokectBankDatabase::class.java)
            .build()
        dao = database.familyMemberDao()
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    private fun createMember(
        id: String,
        name: String = "Test Member",
        xp: Int = 1000,
        coins: Int = 50,
        level: Int = 3,
        isCurrentUser: Boolean = false
    ) = FamilyMember(
        id = id,
        name = name,
        level = level,
        coins = coins,
        xp = xp,
        streak = 5,
        isCurrentUser = isCurrentUser
    )

    @Test
    fun `insert and observe family members`() = runTest(testDispatcher) {
        dao.insertFamilyMember(createMember("fm1", "Alice", 1000, 50, 3))
        dao.insertFamilyMember(createMember("fm2", "Bob", 2000, 80, 5))

        val members = dao.observeFamilyMembers().first()
        assertEquals(2, members.size)
        assertTrue(members.any { it.id == "fm1" })
        assertTrue(members.any { it.id == "fm2" })
    }

    @Test
    fun `observeFamilyMembers orders by xp desc`() = runTest(testDispatcher) {
        dao.insertFamilyMember(createMember("fm1", "Low XP", xp = 500))
        dao.insertFamilyMember(createMember("fm2", "High XP", xp = 5000))
        dao.insertFamilyMember(createMember("fm3", "Mid XP", xp = 2500))

        val members = dao.observeFamilyMembers().first()
        assertEquals(3, members.size)
        // Should be sorted by xp descending
        assertEquals("High XP", members[0].name)
        assertEquals(5000, members[0].xp)
        assertEquals("Mid XP", members[1].name)
        assertEquals("Low XP", members[2].name)
    }

    @Test
    fun `syncCurrentUser updates current user fields`() = runTest(testDispatcher) {
        // Insert a current user and non-current users
        dao.insertFamilyMember(createMember("fm1", "Lucas", xp = 1000, coins = 50, level = 3, isCurrentUser = true))
        dao.insertFamilyMember(createMember("fm2", "Sofia", xp = 2000, coins = 80, level = 5, isCurrentUser = false))

        // Sync current user with new values
        dao.syncCurrentUser(xp = 4750, coins = 85, level = 5)

        val members = dao.getFamilyMembers()
        val currentUser = members.find { it.isCurrentUser }
        assertNotNull(currentUser)
        assertEquals(4750, currentUser.xp)
        assertEquals(85, currentUser.coins)
        assertEquals(5, currentUser.level)

        // Non-current user should be unchanged
        val otherUser = members.find { !it.isCurrentUser }
        assertNotNull(otherUser)
        assertEquals(2000, otherUser.xp)
    }

    @Test
    fun `syncCurrentUser with no current user does not affect others`() = runTest(testDispatcher) {
        dao.insertFamilyMember(createMember("fm1", "Alice", xp = 1000, coins = 50, level = 3, isCurrentUser = false))
        dao.insertFamilyMember(createMember("fm2", "Bob", xp = 2000, coins = 80, level = 5, isCurrentUser = false))

        // syncCurrentUser should update 0 rows when no isCurrentUser = 1
        dao.syncCurrentUser(xp = 9999, coins = 99, level = 10)

        val members = dao.getFamilyMembers()
        assertEquals(2, members.size)
        // Both should be unchanged
        val alice = members.find { it.id == "fm1" }
        assertNotNull(alice)
        assertEquals(1000, alice.xp)

        val bob = members.find { it.id == "fm2" }
        assertNotNull(bob)
        assertEquals(2000, bob.xp)
    }

    @Test
    fun `update family member changes fields`() = runTest(testDispatcher) {
        val member = createMember("fm1", "Original", xp = 1000, level = 3)
        dao.insertFamilyMember(member)

        val inserted = dao.getFamilyMemberByOriginalId("fm1")
        assertNotNull(inserted)

        val updated = inserted.copy(name = "Updated", xp = 5000, level = 7)
        dao.updateFamilyMember(updated)

        val result = dao.getFamilyMemberByOriginalId("fm1")
        assertNotNull(result)
        assertEquals("Updated", result.name)
        assertEquals(5000, result.xp)
        assertEquals(7, result.level)
    }

    @Test
    fun `deleteAll clears all family members`() = runTest(testDispatcher) {
        dao.insertFamilyMember(createMember("fm1"))
        dao.insertFamilyMember(createMember("fm2"))
        dao.insertFamilyMember(createMember("fm3"))

        dao.deleteAll()

        val members = dao.observeFamilyMembers().first()
        assertTrue(members.isEmpty())
    }
}
