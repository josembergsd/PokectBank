package com.pokect.bank.kids.data.di

import android.content.Context
import com.pokect.bank.kids.data.network.RetrofitClient
import com.pokect.bank.kids.data.repository.home.HomeRepository
import com.pokect.bank.kids.data.repository.login.AuthRepository
import com.pokect.bank.kids.data.session.TokenManager

class AppContainer(context: Context) {
    val tokenManager = TokenManager(context)
    private val privateApiService = RetrofitClient.getPrivateService(tokenManager)
    private val publicApiService = RetrofitClient.getPublicService()

    val homeRepository = HomeRepository(privateApiService, tokenManager)
    val authRepository = AuthRepository(publicApiService, tokenManager)
}