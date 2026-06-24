package com.example.studylensmobile.core.network

import android.content.Context
import com.example.studylensmobile.core.datastore.TokenManager
import com.example.studylensmobile.data.remote.api.AiApi
import com.example.studylensmobile.data.remote.api.AuthApi
import com.example.studylensmobile.data.remote.api.LearningApi
import com.example.studylensmobile.data.repository.AiRepository
import com.example.studylensmobile.data.repository.AuthRepository
import com.example.studylensmobile.data.repository.BoardScansRepository
import com.example.studylensmobile.data.repository.DashboardRepository
import com.example.studylensmobile.data.repository.ModulesRepository
import com.example.studylensmobile.data.repository.SubjectsRepository
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

    val learningApi: LearningApi by lazy {
        retrofit.create(LearningApi::class.java)
    }

    val aiApi: AiApi by lazy {
        retrofit.create(AiApi::class.java)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authApi, tokenManager)
    }

    val dashboardRepository: DashboardRepository by lazy {
        DashboardRepository(learningApi)
    }

    val subjectsRepository: SubjectsRepository by lazy {
        SubjectsRepository(learningApi, context.contentResolver, aiRepository)
    }

    val boardScansRepository: BoardScansRepository by lazy {
        BoardScansRepository(learningApi, context.contentResolver, aiRepository)
    }

    val modulesRepository: ModulesRepository by lazy {
        ModulesRepository(learningApi)
    }

    val aiRepository: AiRepository by lazy {
        AiRepository(aiApi)
    }
}
