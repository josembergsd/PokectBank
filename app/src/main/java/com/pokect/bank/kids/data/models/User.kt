package com.pokect.bank.kids.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user", indices = [Index(value = ["id"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val id: String = "user-1",
    val name: String,
    val level: Int,
    val balance: Double,
    val goal: Double,
    val xp: Int,
    val xpToNextLevel: Int,
    val totalXp: Int,
    val streak: Int,
    val coins: Int,
    val badges: List<String>,
    val selectedAvatar: Int
)
