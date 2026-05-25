package com.pokect.bank.kids.data.network

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("name") val name: String,
    @SerializedName("avatarId") val avatarId: Int
)

data class LoginResponse(
    @SerializedName("token") val token: String
)

data class UserProfileDto(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar") val avatarId: Int
)

data class ApiErrorResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null
)
