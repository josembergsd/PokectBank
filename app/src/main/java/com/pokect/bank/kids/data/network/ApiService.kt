package com.pokect.bank.kids.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("/auth/register")
    suspend fun signup(
        @Body request: RegisterRequest
    )

    @GET("/user/me")
    suspend fun getProfile(@Header("Authorization") token: String): UserProfileDto

}
