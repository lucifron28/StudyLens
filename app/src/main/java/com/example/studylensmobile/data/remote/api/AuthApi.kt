package com.example.studylensmobile.data.remote.api

import com.example.studylensmobile.data.remote.dto.LoginRequest
import com.example.studylensmobile.data.remote.dto.RegisterRequest
import com.example.studylensmobile.data.remote.dto.TokenRefreshResponse
import com.example.studylensmobile.data.remote.dto.TokenResponse
import com.example.studylensmobile.data.remote.dto.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/register/")
    suspend fun register(@Body request: RegisterRequest): Response<UserResponse>

    @POST("api/auth/token/")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @POST("api/auth/token/refresh/")
    suspend fun refreshToken(@Body request: Map<String, String>): Response<TokenRefreshResponse>

    @GET("api/auth/me/")
    suspend fun getCurrentUser(): Response<UserResponse>
}
