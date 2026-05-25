package com.pokect.bank.kids.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "weekly_challenge", indices = [Index(value = ["id"], unique = true)])
data class WeeklyChallenge(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val id: String,
    val title: String,
    val description: String,
    val rewardXp: Int,
    val rewardCoins: Int,
    val endDate: Instant,
    val participants: List<String> = emptyList(),
    val hasJoined: Boolean = false
)
