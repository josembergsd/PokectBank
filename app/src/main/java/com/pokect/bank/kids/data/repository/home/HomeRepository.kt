package com.pokect.bank.kids.data.repository.home

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokect.bank.kids.data.network.ApiService
import com.pokect.bank.kids.data.network.UserProfileDto
import com.pokect.bank.kids.data.session.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class HomeRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun getProfile(token: String): UserProfileDto {
        return apiService.getProfile(token)
    }
}
