package com.pokect.bank.kids.data.repository.home

import com.pokect.bank.kids.data.network.ApiService
import com.pokect.bank.kids.data.network.UserProfileDto

class HomeRepository(
    private val apiService: ApiService
) {
    suspend fun getProfile(): UserProfileDto {
        return apiService.getProfile()
    }
}
