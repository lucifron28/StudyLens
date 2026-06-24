package com.example.studylensmobile.data.repository

import com.example.studylensmobile.core.datastore.TokenManager
import com.example.studylensmobile.data.remote.networkFailure
import com.example.studylensmobile.data.remote.api.AuthApi
import com.example.studylensmobile.data.remote.dto.LoginRequest
import com.example.studylensmobile.data.remote.dto.RegisterRequest
import com.example.studylensmobile.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {
    fun isLoggedIn(): Flow<Boolean> = tokenManager.accessToken.map { !it.isNullOrEmpty() }

    suspend fun login(emailOrUsername: String, password: String): Result<Unit> {
        return try {
            val identifier = emailOrUsername.trim()
            val request = if (identifier.contains("@")) {
                LoginRequest(email = identifier, password = password)
            } else {
                LoginRequest(username = identifier, password = password)
            }
            val response = authApi.login(request)
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.failure(Exception("Login failed: empty server response."))
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
            networkFailure(e)
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
                login(email, password)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Registration failed. Email may already be in use."
                    else -> "Registration failed (${response.code()}). Please try again."
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            networkFailure(e)
        }
    }

    suspend fun logout() {
        tokenManager.clearTokens()
    }

    suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = authApi.getCurrentUser()
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Result.failure(Exception("Could not fetch user: empty server response."))
                Result.success(
                    User(
                        id = body.id,
                        username = body.username ?: body.email,
                        email = body.email,
                        firstName = body.firstName,
                        lastName = body.lastName,
                        profileImageUrl = body.profileImageUrl
                    )
                )
            } else {
                Result.failure(Exception("Could not fetch user (${response.code()})."))
            }
        } catch (e: Exception) {
            networkFailure(e)
        }
    }

    suspend fun uploadProfileImage(imageFile: File): Result<User> {
        return try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
            val response = authApi.uploadProfileImage(body)
            if (response.isSuccessful) {
                val responseBody = response.body()
                    ?: return Result.failure(Exception("Image uploaded but server response was empty."))
                Result.success(
                    User(
                        id = responseBody.id,
                        username = responseBody.username ?: responseBody.email,
                        email = responseBody.email,
                        firstName = responseBody.firstName,
                        lastName = responseBody.lastName,
                        profileImageUrl = responseBody.profileImageUrl
                    )
                )
            } else {
                Result.failure(Exception("Could not upload profile image (${response.code()})."))
            }
        } catch (e: Exception) {
            networkFailure(e)
        }
    }
}
