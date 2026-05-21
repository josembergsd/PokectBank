package com.pokect.bank.kids.data.network

import android.util.Log
import com.pokect.bank.kids.data.session.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            tokenManager.token.first()
        }

        val request = if (!token.isNullOrBlank()) {
            chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            throw RuntimeException("Session token not find, this route is private")
        }

        return chain.proceed(request)
    }

}