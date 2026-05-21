package com.pokect.bank.kids.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class GoalColor { PRIMARY, SECONDARY, SUCCESS, WARNING }

@Entity(tableName = "savings_goal", indices = [Index(value = ["id"], unique = true)])
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val id: String,
    val title: String,
    val icon: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val daysLeft: Int,
    val color: GoalColor
)
