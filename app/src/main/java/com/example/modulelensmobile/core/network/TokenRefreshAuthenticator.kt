package com.example.modulelensmobile.core.network

import com.example.modulelensmobile.core.datastore.TokenManager
import com.example.modulelensmobile.data.remote.dto.TokenRefreshResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

class TokenRefreshAuthenticator(
    private val tokenManager: TokenManager,
    private val baseUrl: String,
) : Authenticator {
    private val refreshClient = OkHttpClient()
    private val gson = Gson()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null
        if (response.request.header("Authorization").isNullOrBlank()) return null

        val refreshToken = runBlocking { tokenManager.refreshToken.firstOrNull() }
            ?: return null

        val refreshBody = gson
            .toJson(mapOf("refresh" to refreshToken))
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val refreshRequest = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/api/auth/token/refresh/")
            .post(refreshBody)
            .build()

        return try {
            refreshClient.newCall(refreshRequest).execute().use { refreshResponse ->
                if (!refreshResponse.isSuccessful) {
                    runBlocking { tokenManager.clearTokens() }
                    return null
                }

                val body = refreshResponse.body?.string().orEmpty()
                val accessToken = gson.fromJson(body, TokenRefreshResponse::class.java).access
                if (accessToken.isBlank()) return null

                runBlocking { tokenManager.saveAccessToken(accessToken) }
                response.request.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            count += 1
            priorResponse = priorResponse.priorResponse
        }
        return count
    }
}

