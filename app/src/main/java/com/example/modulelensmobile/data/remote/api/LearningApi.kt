package com.example.modulelensmobile.data.remote.api

import com.example.modulelensmobile.data.remote.dto.DashboardDto
import com.example.modulelensmobile.data.remote.dto.PaginatedSubjectsDto
import com.example.modulelensmobile.data.remote.dto.SubjectOverviewDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LearningApi {
    @GET("api/learning/dashboard/")
    suspend fun getDashboard(): Response<DashboardDto>

    @GET("api/learning/subjects/")
    suspend fun getSubjects(
        @Query("search") search: String? = null,
        @Query("ordering") ordering: String = "title"
    ): Response<PaginatedSubjectsDto>

    @GET("api/learning/subjects/{subjectId}/overview/")
    suspend fun getSubjectOverview(
        @Path("subjectId") subjectId: String
    ): Response<SubjectOverviewDto>
}

