package com.pokect.bank.kids.data.local

import com.pokect.bank.kids.data.models.GoalColor
import com.pokect.bank.kids.data.models.MissionCategory
import com.pokect.bank.kids.data.models.MissionDifficulty
import com.pokect.bank.kids.data.models.MissionStatus
import com.pokect.bank.kids.data.models.TransactionType
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

class PokectBankConvertersTest {

    private val converter = PokectBankConverters()

    // === MissionStatus ===

    @Test
    fun `missionStatus converter round-trip`() {
        val original = MissionStatus.IN_PROGRESS
        val dbValue = converter.fromMissionStatus(original)
        val restored = converter.toMissionStatus(dbValue)
        assertEquals(original, restored)
    }

    @Test
    fun `missionStatus AVAILABLE round-trip`() {
        val original = MissionStatus.AVAILABLE
        val dbValue = converter.fromMissionStatus(original)
        val restored = converter.toMissionStatus(dbValue)
        assertEquals(original, restored)
    }

    // === MissionDifficulty ===

    @Test
    fun `missionDifficulty converter round-trip`() {
        val original = MissionDifficulty.MEDIUM
        val dbValue = converter.fromMissionDifficulty(original)
        val restored = converter.toMissionDifficulty(dbValue)
        assertEquals(original, restored)
    }

    @Test
    fun `missionDifficulty all values round-trip`() {
        for (value in MissionDifficulty.entries) {
            val dbValue = converter.fromMissionDifficulty(value)
            val restored = converter.toMissionDifficulty(dbValue)
            assertEquals(value, restored)
        }
    }

    // === MissionCategory ===

    @Test
    fun `missionCategory converter round-trip`() {
        val original = MissionCategory.WEEKLY
        val dbValue = converter.fromMissionCategory(original)
        val restored = converter.toMissionCategory(dbValue)
        assertEquals(original, restored)
    }

    // === TransactionType ===

    @Test
    fun `transactionType converter round-trip`() {
        val original = TransactionType.DEPOSIT
        val dbValue = converter.fromTransactionType(original)
        val restored = converter.toTransactionType(dbValue)
        assertEquals(original, restored)
    }

    @Test
    fun `transactionType all values round-trip`() {
        for (value in TransactionType.entries) {
            val dbValue = converter.fromTransactionType(value)
            val restored = converter.toTransactionType(dbValue)
            assertEquals(value, restored)
        }
    }

    // === GoalColor ===

    @Test
    fun `goalColor converter round-trip`() {
        val original = GoalColor.PRIMARY
        val dbValue = converter.fromGoalColor(original)
        val restored = converter.toGoalColor(dbValue)
        assertEquals(original, restored)
    }

    // === List<String> ===

    @Test
    fun `stringList converter round-trip with badges`() {
        val original = listOf("🏆", "⭐", "🎯", "💰")
        val dbValue = converter.fromStringList(original)
        val restored = converter.toStringList(dbValue)
        assertEquals(original, restored)
    }

    @Test
    fun `stringList converter round-trip with empty list`() {
        val original = emptyList<String>()
        val dbValue = converter.fromStringList(original)
        val restored = converter.toStringList(dbValue)
        assertEquals(original, restored)
    }

    @Test
    fun `stringList converter round-trip with single item`() {
        val original = listOf("item1")
        val dbValue = converter.fromStringList(original)
        val restored = converter.toStringList(dbValue)
        assertEquals(original, restored)
    }

    // === Instant ===

    @Test
    fun `instant converter round-trip`() {
        val original = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        val dbValue = converter.fromInstant(original)
        val restored = converter.toInstant(dbValue)
        assertEquals(original, restored)
    }

    @Test
    fun `instant converter handles epoch zero`() {
        val original = Instant.ofEpochMilli(0)
        val dbValue = converter.fromInstant(original)
        val restored = converter.toInstant(dbValue)
        assertEquals(original, restored)
    }

    @Test
    fun `instant converter handles future date`() {
        val original = Instant.parse("2030-01-01T00:00:00Z")
        val dbValue = converter.fromInstant(original)
        val restored = converter.toInstant(dbValue)
        assertEquals(original, restored)
    }
}
