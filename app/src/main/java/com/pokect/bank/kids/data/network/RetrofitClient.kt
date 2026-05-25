package com.pokect.bank.kids.data.network

import com.pokect.bank.kids.data.session.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun createRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    fun getPublicService(): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        return createRetrofit(client).create(ApiService::class.java)
    }

    fun getPrivateService(tokenManager: TokenManager): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()
        return createRetrofit(client).create(ApiService::class.java)
    }
}
