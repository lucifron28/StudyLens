package com.example.studylensmobile.core.network

import com.example.studylensmobile.core.datastore.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        
        if (path.contains("/api/auth/register/") || 
            path.contains("/api/auth/token/") || 
            path.contains("/api/auth/token/refresh/")) {
            return chain.proceed(request)
        }

        val token = runBlocking { tokenManager.accessToken.firstOrNull() }
        val newRequest = if (!token.isNullOrEmpty()) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        return chain.proceed(newRequest)
    }
}
