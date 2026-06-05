package com.example.modulelensmobile.data.remote.api

import com.example.modulelensmobile.data.remote.dto.DashboardDto
import retrofit2.Response
import retrofit2.http.GET

interface LearningApi {
    @GET("api/learning/dashboard/")
    suspend fun getDashboard(): Response<DashboardDto>
}

