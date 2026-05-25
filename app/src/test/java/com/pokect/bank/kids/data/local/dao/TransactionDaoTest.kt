package com.pokect.bank.kids.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pokect.bank.kids.data.local.PokectBankDatabase
import com.pokect.bank.kids.data.models.Transaction
import com.pokect.bank.kids.data.models.TransactionType
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
class TransactionDaoTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var database: PokectBankDatabase
    private lateinit var dao: TransactionDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PokectBankDatabase::class.java)
            .build()
        dao = database.transactionDao()
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    private fun createTransaction(
        id: String,
        type: TransactionType = TransactionType.DEPOSIT,
        title: String = "Test Transaction",
        amount: Double = 10.0
    ) = Transaction(
        id = id,
        type = type,
        title = title,
        amount = amount,
        date = "2026-05-21",
        icon = "💰"
    )

    @Test
    fun `insert and observe transactions`() = runTest(testDispatcher) {
        dao.insertTransaction(createTransaction("t1", TransactionType.DEPOSIT, "Deposit 1", 10.0))
        dao.insertTransaction(createTransaction("t2", TransactionType.MISSION, "Mission 1", 5.0))

        val transactions = dao.observeTransactions().first()
        assertEquals(2, transactions.size)
        // Ordered by date DESC — both have same date, order is by insertion
        assertTrue(transactions.any { it.id == "t1" })
        assertTrue(transactions.any { it.id == "t2" })
    }

    @Test
    fun `insert transaction emits on Flow`() = runTest(testDispatcher) {
        dao.insertTransaction(createTransaction("t1"))

        val transactions = dao.observeTransactions().first()
        assertEquals(1, transactions.size)
        assertEquals("t1", transactions[0].id)
        assertEquals(TransactionType.DEPOSIT, transactions[0].type)
        assertEquals(10.0, transactions[0].amount)
    }

    @Test
    fun `insert multiple transactions of different types`() = runTest(testDispatcher) {
        dao.insertTransaction(createTransaction("t_deposit", TransactionType.DEPOSIT))
        dao.insertTransaction(createTransaction("t_mission", TransactionType.MISSION))
        dao.insertTransaction(createTransaction("t_reward", TransactionType.REWARD))
        dao.insertTransaction(createTransaction("t_goal", TransactionType.GOAL))

        val transactions = dao.observeTransactions().first()
        assertEquals(4, transactions.size)
    }

    @Test
    fun `deleteAll clears all transactions`() = runTest(testDispatcher) {
        dao.insertTransaction(createTransaction("t1"))
        dao.insertTransaction(createTransaction("t2"))
        dao.insertTransaction(createTransaction("t3"))

        dao.deleteAll()

        val transactions = dao.observeTransactions().first()
        assertTrue(transactions.isEmpty())
    }
}
