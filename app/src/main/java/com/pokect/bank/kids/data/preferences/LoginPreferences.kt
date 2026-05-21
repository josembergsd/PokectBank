package com.pokect.bank.kids.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class LoginPrefsData(
    val isLoggedIn: Boolean = false,
    val selectedAvatar: Int = 1
)

class LoginPreferences(private val dataStore: DataStore<Preferences>) {

    val loginState: Flow<LoginPrefsData> = dataStore.data.map { prefs ->
        LoginPrefsData(
            isLoggedIn = prefs[IS_LOGGED_IN_KEY] ?: false,
            selectedAvatar = prefs[SELECTED_AVATAR_KEY] ?: 1
        )
    }

    suspend fun setLoggedIn(avatar: Int) {
        dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN_KEY] = true
            prefs[SELECTED_AVATAR_KEY] = avatar
        }
    }

    suspend fun clearLogin() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    companion object {
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        private val SELECTED_AVATAR_KEY = intPreferencesKey("selected_avatar")
    }
}
