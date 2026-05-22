package com.pokect.bank.kids.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pokect.bank.kids.data.models.Mission
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {

    @Query("SELECT * FROM mission")
    fun observeMissions(): Flow<List<Mission>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: Mission)

    @Update
    suspend fun updateMission(mission: Mission)

    @Query("SELECT * FROM mission WHERE id = :originalId LIMIT 1")
    suspend fun getMissionByOriginalId(originalId: String): Mission?

    @Query("DELETE FROM mission")
    suspend fun deleteAll()
}
