package com.pokect.bank.kids.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionType { DEPOSIT, WITHDRAW, REWARD, MISSION, GOAL }

@Entity(tableName = "transaction", indices = [Index(value = ["id"], unique = true)])
data class Transaction(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val id: String,
    val type: TransactionType,
    val title: String,
    val amount: Double,
    val date: String,
    val icon: String? = null
)
