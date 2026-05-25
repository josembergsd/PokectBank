package com.pokect.bank.kids.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokect.bank.kids.data.preferences.LoginPreferences
import com.pokect.bank.kids.data.repository.login.AuthRepository
import com.google.gson.Gson
import com.pokect.bank.kids.data.network.ApiErrorResponse
import retrofit2.HttpException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class LoginUiState(
    val isLoginTab: Boolean = true,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val selectedAvatar: Int = 1,
    val step: Int = 1,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
    val isCheckingLogin: Boolean = true
)

class LoginViewModel(
    val authRepository: AuthRepository,
    private val loginPrefs: LoginPreferences? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        loginPrefs?.let { prefs ->
            viewModelScope.launch {
                prefs.loginState.collect { prefsData ->
                    _uiState.update { current ->
                        current.copy(
                            isLoggedIn = prefsData.isLoggedIn,
                            selectedAvatar = prefsData.selectedAvatar,
                            isCheckingLogin = false
                        )
                    }
                }
            }
        } ?: run {
            // No DataStore available — skip loading state
            _uiState.update { it.copy(isCheckingLogin = false) }
        }
    }

    fun toggleTab(isLogin: Boolean) {
        _uiState.update {
            it.copy(
                isLoginTab = isLogin,
                step = 1,
                name = "",
                email = "",
                password = "",
                error = null
            )
        }
    }

    fun updateName(value: String) {
        _uiState.update { it.copy(name = value, error = null) }
    }

    fun updateEmail(value: String) {
        _uiState.update { it.copy(email = value.trim(), error = null) }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(showPassword = !it.showPassword) }
    }

    fun selectAvatar(id: Int) {
        _uiState.update { it.copy(selectedAvatar = id) }
    }

    fun nextStep() {
        val state = _uiState.value
        if (state.isLoginTab) {
            login()
        } else {
            // Signup: validate then advance to step 2
            if (state.name.isBlank()) {
                _uiState.update { it.copy(error = "Qual é o seu nome?") }
                return
            }
            if (state.email.isBlank()) {
                _uiState.update { it.copy(error = "Digite seu e-mail para continuar") }
                return
            }
            if (state.password.isBlank()) {
                _uiState.update { it.copy(error = "Digite sua senha secreta") }
                return
            }
            _uiState.update { it.copy(step = 2) }
        }
    }

    fun previousStep() {
        _uiState.update { it.copy(step = 1) }
    }

    fun login() {
        val state = _uiState.value

        if (state.email.isBlank()) {
            _uiState.update { it.copy(error = "Digite seu e-mail para continuar") }
            return
        }
        if (state.password.isBlank()) {
            _uiState.update { it.copy(error = "Digite sua senha secreta") }
            return
        }
        if (!state.isLoginTab && state.name.isBlank()) {
            _uiState.update { it.copy(error = "Qual é o seu nome?") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val state = _uiState.value
            try {
                if (state.isLoginTab) {
                    authRepository.login(state.email, state.password)
                } else {
                    authRepository.register(
                        email = state.email,
                        password = state.password,
                        name = state.name,
                        avatarId = state.selectedAvatar
                    )
                }
                loginPrefs?.let { prefs ->
                    prefs.setLoggedIn(_uiState.value.selectedAvatar)
                    return@launch
                }
                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
            } catch (e: Exception) {
                val errorMessage = (e as? HttpException)?.response()?.errorBody()?.string()?.let {
                    try { 
                        Gson().fromJson(it, ApiErrorResponse::class.java).let { res -> res.message ?: res.error }
                    } catch(_: Exception) { null }
                } ?: e.message ?: "Erro inesperado"
                
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }

    fun forgotPassword() {
        _uiState.update { it.copy(error = "Link enviado para o email! 📧") }
        viewModelScope.launch {
            delay(3000)
            _uiState.update { it.copy(error = null) }
        }
    }
}
