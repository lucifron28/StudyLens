package com.example.modulelensmobile.core.network

import android.content.Context
import com.example.modulelensmobile.core.datastore.TokenManager
import retrofit2.Retrofit

class AppContainer(private val context: Context) {
    val tokenManager: TokenManager by lazy {
        TokenManager(context)
    }

    val retrofit: Retrofit by lazy {
        RetrofitClient.createRetrofit(tokenManager)
    }
}
