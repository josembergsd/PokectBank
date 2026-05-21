package com.pokect.bank.kids.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pokect.bank.kids.data.models.SavingsGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {

    @Query("SELECT * FROM savings_goal")
    fun observeGoals(): Flow<List<SavingsGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoal)

    @Update
    suspend fun updateGoal(goal: SavingsGoal)

    @Query("SELECT * FROM savings_goal WHERE id = :originalId LIMIT 1")
    suspend fun getGoalByOriginalId(originalId: String): SavingsGoal?

    @Query("DELETE FROM savings_goal")
    suspend fun deleteAll()
}
