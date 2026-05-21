package com.pokect.bank.kids.data.local

import androidx.room.TypeConverter
import com.pokect.bank.kids.data.models.GoalColor
import com.pokect.bank.kids.data.models.MissionCategory
import com.pokect.bank.kids.data.models.MissionDifficulty
import com.pokect.bank.kids.data.models.MissionStatus
import com.pokect.bank.kids.data.models.TransactionType
import java.time.Instant

class PokectBankConverters {

    // --- MissionStatus ---
    @TypeConverter
    fun fromMissionStatus(value: MissionStatus): String = value.name

    @TypeConverter
    fun toMissionStatus(value: String): MissionStatus = MissionStatus.valueOf(value)

    // --- MissionDifficulty ---
    @TypeConverter
    fun fromMissionDifficulty(value: MissionDifficulty): String = value.name

    @TypeConverter
    fun toMissionDifficulty(value: String): MissionDifficulty = MissionDifficulty.valueOf(value)

    // --- MissionCategory ---
    // Room stores the enum name ("DAILY", "WEEKLY", "SPECIAL"), not the label property
    @TypeConverter
    fun fromMissionCategory(value: MissionCategory): String = value.name

    @TypeConverter
    fun toMissionCategory(value: String): MissionCategory = MissionCategory.valueOf(value)

    // --- TransactionType ---
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    // --- GoalColor ---
    @TypeConverter
    fun fromGoalColor(value: GoalColor): String = value.name

    @TypeConverter
    fun toGoalColor(value: String): GoalColor = GoalColor.valueOf(value)

    // --- List<String> ---
    // Used for User.badges and WeeklyChallenge.participants.
    // Assumption: current IDs (fixed keys like "fm2", "fm4") contain no commas.
    // If IDs ever include commas, this converter will need a different delimiter strategy.
    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(",")

    @TypeConverter
    fun toStringList(value: String): List<String> =
        value.split(",").map { it.trim() }.filter { it.isNotBlank() }

    // --- Instant ---
    @TypeConverter
    fun fromInstant(value: Instant): Long = value.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long): Instant = Instant.ofEpochMilli(value)
}
