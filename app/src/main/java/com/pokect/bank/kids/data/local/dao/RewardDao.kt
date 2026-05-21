package com.pokect.bank.kids.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pokect.bank.kids.data.models.Reward
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {

    @Query("SELECT * FROM reward")
    fun observeRewards(): Flow<List<Reward>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: Reward)

    @Update
    suspend fun updateReward(reward: Reward)

    @Query("SELECT * FROM reward")
    suspend fun getRewards(): List<Reward>

    @Query("SELECT * FROM reward WHERE id = :originalId LIMIT 1")
    suspend fun getRewardByOriginalId(originalId: String): Reward?

    @Query("DELETE FROM reward")
    suspend fun deleteAll()
}
