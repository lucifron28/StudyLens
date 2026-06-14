package com.example.studylensmobile.data.remote.api

import com.example.studylensmobile.data.remote.dto.BoardScanDto
import com.example.studylensmobile.data.remote.dto.BoardScanUpdateRequest
import com.example.studylensmobile.data.remote.dto.DashboardDto
import com.example.studylensmobile.data.remote.dto.ModuleDto
import com.example.studylensmobile.data.remote.dto.PaginatedBoardScansDto
import com.example.studylensmobile.data.remote.dto.PaginatedChaptersDto
import com.example.studylensmobile.data.remote.dto.PaginatedSubjectsDto
import com.example.studylensmobile.data.remote.dto.SubjectOverviewDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
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

    @GET("api/learning/board-scans/")
    suspend fun getBoardScans(
        @Query("search") search: String? = null,
        @Query("review_status") reviewStatus: String? = null,
        @Query("ordering") ordering: String = "-created_at"
    ): Response<PaginatedBoardScansDto>

    @GET("api/learning/board-scans/{scanId}/")
    suspend fun getBoardScan(
        @Path("scanId") scanId: String
    ): Response<BoardScanDto>

    @PATCH("api/learning/board-scans/{scanId}/")
    suspend fun updateBoardScan(
        @Path("scanId") scanId: String,
        @Body request: BoardScanUpdateRequest
    ): Response<BoardScanDto>

    @GET("api/learning/modules/{moduleId}/")
    suspend fun getModule(
        @Path("moduleId") moduleId: String
    ): Response<ModuleDto>

    @GET("api/learning/chapters/")
    suspend fun getChapters(
        @Query("module") moduleId: String,
        @Query("ordering") ordering: String = "order"
    ): Response<PaginatedChaptersDto>
}