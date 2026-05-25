package com.pokect.bank.kids.di

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.pokect.bank.kids.data.local.PokectBankDatabase
import com.pokect.bank.kids.data.preferences.LoginPreferences
import com.pokect.bank.kids.data.repository.AppRepository
import android.util.Log
import com.pokect.bank.kids.data.network.RetrofitClient
import com.pokect.bank.kids.data.repository.home.HomeRepository
import com.pokect.bank.kids.data.repository.login.AuthRepository
import com.pokect.bank.kids.data.session.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "login_state")

object AppContainer {
    private lateinit var appContext: Context
    @Volatile
    private var _isResetting = false

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    @get:SuppressLint("StaticFieldLeak")
    val tokenManager by lazy { TokenManager(appContext) }
    private val privateApiService by lazy { RetrofitClient.getPrivateService(tokenManager) }
    private val publicApiService by lazy { RetrofitClient.getPublicService() }

    val homeRepository by lazy { HomeRepository(privateApiService) }
    val authRepository by lazy { AuthRepository(publicApiService, tokenManager) }

    // === Database with reset support ===
    private var _database: PokectBankDatabase? = null
    private val database: PokectBankDatabase
        get() {
            _database?.let { return it }
            if (_isResetting) throw IllegalStateException("AppContainer is resetting")
            _database = PokectBankDatabase.getInstance(appContext)
            return _database!!
        }

    // === Repository with reset support ===
    private var _repository: AppRepository? = null
    val repository: AppRepository
        get() {
            _repository?.let { return it }
            if (_isResetting) throw IllegalStateException("AppContainer is resetting")
            _repository = AppRepository(
                userDao = database.userDao(),
                transactionDao = database.transactionDao(),
                savingsGoalDao = database.savingsGoalDao(),
                missionDao = database.missionDao(),
                rewardDao = database.rewardDao(),
                familyMemberDao = database.familyMemberDao(),
                weeklyChallengeDao = database.weeklyChallengeDao(),
                database = database
            )
            return _repository!!
        }

    val loginPreferences: LoginPreferences by lazy { LoginPreferences(appContext.dataStore) }

    suspend fun performFullReset(context: Context) = withContext(Dispatchers.IO) {
        if (_isResetting) return@withContext
        _isResetting = true
        try {
            tokenManager.clearToken()
            loginPreferences.clearLogin()
            _repository?.resetData(context)
            _repository = null
            _database = null
            PokectBankDatabase.resetInstance(context)
            
            Log.d("AppContainer", "Full reset completed successfully")
        } catch (e: Exception) {
            Log.e("AppContainer", "Error during full reset", e)
            throw e
        } finally {
            _isResetting = false
        }
    }
}
