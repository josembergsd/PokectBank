package com.pokect.bank.kids.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MissionStatus { LOCKED, AVAILABLE, IN_PROGRESS, COMPLETED }
enum class MissionDifficulty { EASY, MEDIUM, HARD }
enum class MissionCategory(val label: String) {
    DAILY("Diárias"),
    WEEKLY("Semanais"),
    SPECIAL("Especiais")
}

@Entity(tableName = "mission", indices = [Index(value = ["id"], unique = true)])
data class Mission(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val id: String,
    val title: String,
    val description: String,
    val xp: Int,
    val coins: Int,
    val progress: Int,
    val maxProgress: Int,
    val status: MissionStatus,
    val icon: String,
    val difficulty: MissionDifficulty,
    val category: MissionCategory = MissionCategory.DAILY
)
