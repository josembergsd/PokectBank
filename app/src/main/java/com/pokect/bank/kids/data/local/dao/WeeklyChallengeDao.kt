package com.pokect.bank.kids.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pokect.bank.kids.data.models.WeeklyChallenge
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyChallengeDao {

    @Query("SELECT * FROM weekly_challenge")
    fun observeChallenges(): Flow<List<WeeklyChallenge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: WeeklyChallenge)

    @Update
    suspend fun updateChallenge(challenge: WeeklyChallenge)

    @Query("SELECT * FROM weekly_challenge")
    suspend fun getChallenges(): List<WeeklyChallenge>

    @Query("SELECT * FROM weekly_challenge WHERE id = :originalId LIMIT 1")
    suspend fun getChallengeByOriginalId(originalId: String): WeeklyChallenge?

    @Query("DELETE FROM weekly_challenge")
    suspend fun deleteAll()
}
