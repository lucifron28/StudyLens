package com.example.modulelensmobile.core.network

import android.content.Context
import com.example.modulelensmobile.core.datastore.TokenManager
import com.example.modulelensmobile.data.remote.api.AuthApi
import com.example.modulelensmobile.data.repository.AuthRepository
import retrofit2.Retrofit

class AppContainer(private val context: Context) {
    val tokenManager: TokenManager by lazy {
        TokenManager(context)
    }

    val retrofit: Retrofit by lazy {
        RetrofitClient.createRetrofit(tokenManager)
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authApi, tokenManager)
    }
}
