package com.pokect.bank.kids.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.pokect.bank.kids.data.repository.AppRepository

object TestDatabaseHelper {

    fun createTestRepository(): AppRepository {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = PokectBankDatabase.getTestInstance(context)
        return AppRepository(
            userDao = database.userDao(),
            transactionDao = database.transactionDao(),
            savingsGoalDao = database.savingsGoalDao(),
            missionDao = database.missionDao(),
            rewardDao = database.rewardDao(),
            familyMemberDao = database.familyMemberDao(),
            weeklyChallengeDao = database.weeklyChallengeDao(),
            database = database
        )
    }

    fun createTestDatabase(): PokectBankDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return PokectBankDatabase.getTestInstance(context)
    }
}
