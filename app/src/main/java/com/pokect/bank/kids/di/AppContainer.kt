package com.pokect.bank.kids.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.pokect.bank.kids.data.local.PokectBankDatabase
import com.pokect.bank.kids.data.preferences.LoginPreferences
import com.pokect.bank.kids.data.repository.AppRepository

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "login_state")

object AppContainer {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val database: PokectBankDatabase by lazy {
        PokectBankDatabase.getInstance(appContext)
    }

    val repository: AppRepository by lazy {
        AppRepository(
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

    val loginPreferences: LoginPreferences by lazy { LoginPreferences(appContext.dataStore) }
}
