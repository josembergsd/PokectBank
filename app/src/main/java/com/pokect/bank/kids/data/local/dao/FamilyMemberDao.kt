package com.pokect.bank.kids.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pokect.bank.kids.data.models.FamilyMember
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {

    @Query("SELECT * FROM family_member ORDER BY xp DESC")
    fun observeFamilyMembers(): Flow<List<FamilyMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMember(member: FamilyMember)

    @Update
    suspend fun updateFamilyMember(member: FamilyMember)

    @Query("SELECT * FROM family_member")
    suspend fun getFamilyMembers(): List<FamilyMember>

    @Query("SELECT * FROM family_member WHERE id = :originalId LIMIT 1")
    suspend fun getFamilyMemberByOriginalId(originalId: String): FamilyMember?

    @Query("UPDATE family_member SET xp = :xp, coins = :coins, level = :level WHERE isCurrentUser = 1")
    suspend fun syncCurrentUser(xp: Int, coins: Int, level: Int)

    @Query("DELETE FROM family_member")
    suspend fun deleteAll()
}
