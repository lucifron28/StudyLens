package com.example.modulelensmobile.data.repository

import com.example.modulelensmobile.core.datastore.TokenManager
import com.example.modulelensmobile.data.remote.api.AuthApi
import com.example.modulelensmobile.data.remote.dto.LoginRequest
import com.example.modulelensmobile.data.remote.dto.RegisterRequest
import com.example.modulelensmobile.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {
    fun isLoggedIn(): Flow<Boolean> = tokenManager.accessToken.map { !it.isNullOrEmpty() }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = authApi.login(LoginRequest(email = email, password = password))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.saveTokens(access = body.access, refresh = body.refresh)
                Result.success(Unit)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Invalid email or password."
                    else -> "Login failed (${response.code()}). Please try again."
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            val response = authApi.register(
                RegisterRequest(
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName
                )
            )
            if (response.isSuccessful) {
                // Backend returns user object on register; auto-login afterwards
                login(email, password)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Registration failed. Email may already be in use."
                    else -> "Registration failed (${response.code()}). Please try again."
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun logout() {
        tokenManager.clearTokens()
    }

    suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = authApi.getCurrentUser()
            if (response.isSuccessful) {
                val body = response.body()!!
                Result.success(
                    User(
                        id = body.id,
                        username = body.username ?: body.email,
                        email = body.email,
                        firstName = body.firstName,
                        lastName = body.lastName
                    )
                )
            } else {
                Result.failure(Exception("Could not fetch user (${response.code()})."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}
