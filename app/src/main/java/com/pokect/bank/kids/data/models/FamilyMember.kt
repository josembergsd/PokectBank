package com.pokect.bank.kids.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "family_member", indices = [Index(value = ["id"], unique = true)])
data class FamilyMember(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val id: String,
    val name: String,
    val level: Int,
    val coins: Int,
    val xp: Int,
    val streak: Int,
    val isCurrentUser: Boolean = false
)
