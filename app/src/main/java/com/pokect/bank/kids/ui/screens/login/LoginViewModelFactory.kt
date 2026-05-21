package com.pokect.bank.kids.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pokect.bank.kids.data.repository.login.AuthRepository

class LoginViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
