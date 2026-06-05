package com.example.modulelensmobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val username: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class TokenResponse(
    val access: String,
    val refresh: String
)

data class UserResponse(
    val id: Int,
    val username: String?,
    val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String
)
