package com.pokect.bank.kids.data.repository.login

import com.pokect.bank.kids.data.network.ApiService
import com.pokect.bank.kids.data.network.LoginRequest
import com.pokect.bank.kids.data.network.RegisterRequest
import com.pokect.bank.kids.data.session.TokenManager

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun login(email: String, password: String) {
        val response = apiService.login(
            LoginRequest(email, password)
        )
        tokenManager.saveToken(response.token)
    }

    suspend fun register(
        email: String,
        password: String,
        name: String,
        avatarId: Int
    ) {
        apiService.signup(
            RegisterRequest(
                email, password, name, avatarId
            )
        )

        this.login(email, password)
    }
}
