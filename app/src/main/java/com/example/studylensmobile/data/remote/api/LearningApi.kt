package com.example.studylensmobile.data.remote.api

import com.google.gson.JsonObject
import com.example.studylensmobile.data.remote.dto.BoardScanDto
import com.example.studylensmobile.data.remote.dto.BoardScanUpdateRequest
import com.example.studylensmobile.data.remote.dto.BoardScanWriteRequest
import com.example.studylensmobile.data.remote.dto.DashboardDto
import com.example.studylensmobile.data.remote.dto.ModuleDto
import com.example.studylensmobile.data.remote.dto.ModuleWriteRequest
import com.example.studylensmobile.data.remote.dto.PaginatedBoardScansDto
import com.example.studylensmobile.data.remote.dto.PaginatedModulesDto
import com.example.studylensmobile.data.remote.dto.PaginatedSubjectsDto
import com.example.studylensmobile.data.remote.dto.SubjectDto
import com.example.studylensmobile.data.remote.dto.SubjectOverviewDto
import com.example.studylensmobile.data.remote.dto.SubjectWriteRequest
import com.example.studylensmobile.data.remote.dto.StudyTaskPreviewDto
import com.example.studylensmobile.data.remote.dto.StudyTaskWriteRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
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

    @POST("api/learning/subjects/")
    suspend fun createSubject(
        @Body request: SubjectWriteRequest
    ): Response<SubjectDto>

    @PATCH("api/learning/subjects/{subjectId}/")
    suspend fun updateSubject(
        @Path("subjectId") subjectId: String,
        @Body request: SubjectWriteRequest
    ): Response<SubjectDto>

    @DELETE("api/learning/subjects/{subjectId}/")
    suspend fun deleteSubject(
        @Path("subjectId") subjectId: String
    ): Response<Unit>

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

    @POST("api/learning/board-scans/")
    suspend fun createBoardScan(
        @Body request: BoardScanWriteRequest
    ): Response<BoardScanDto>

    @Multipart
    @POST("api/learning/board-scans/")
    suspend fun createBoardScanWithImage(
        @Part image: MultipartBody.Part,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>
    ): Response<BoardScanDto>

    @PATCH("api/learning/board-scans/{scanId}/")
    suspend fun updateBoardScanDetails(
        @Path("scanId") scanId: String,
        @Body request: JsonObject
    ): Response<BoardScanDto>

    @Multipart
    @PATCH("api/learning/board-scans/{scanId}/")
    suspend fun updateBoardScanWithImage(
        @Path("scanId") scanId: String,
        @Part image: MultipartBody.Part,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>
    ): Response<BoardScanDto>

    @DELETE("api/learning/board-scans/{scanId}/")
    suspend fun deleteBoardScan(
        @Path("scanId") scanId: String
    ): Response<Unit>

    @GET("api/learning/modules/")
    suspend fun getModules(
        @Query("subject") subjectId: String? = null,
        @Query("ordering") ordering: String = "title"
    ): Response<PaginatedModulesDto>

    @GET("api/learning/modules/{moduleId}/")
    suspend fun getModule(
        @Path("moduleId") moduleId: String
    ): Response<ModuleDto>

    @POST("api/learning/modules/")
    suspend fun createModule(
        @Body request: ModuleWriteRequest
    ): Response<ModuleDto>

    @Multipart
    @POST("api/learning/modules/")
    suspend fun createModuleWithFile(
        @Part file: MultipartBody.Part,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>
    ): Response<ModuleDto>

    @PATCH("api/learning/modules/{moduleId}/")
    suspend fun updateModule(
        @Path("moduleId") moduleId: String,
        @Body request: ModuleWriteRequest
    ): Response<ModuleDto>

    @Multipart
    @PATCH("api/learning/modules/{moduleId}/")
    suspend fun updateModuleWithFile(
        @Path("moduleId") moduleId: String,
        @Part file: MultipartBody.Part,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>
    ): Response<ModuleDto>

    @DELETE("api/learning/modules/{moduleId}/")
    suspend fun deleteModule(
        @Path("moduleId") moduleId: String
    ): Response<Unit>

    @POST("api/learning/tasks/")
    suspend fun createStudyTask(
        @Body request: StudyTaskWriteRequest
    ): Response<StudyTaskPreviewDto>

    @PATCH("api/learning/tasks/{taskId}/")
    suspend fun updateStudyTask(
        @Path("taskId") taskId: String,
        @Body request: StudyTaskWriteRequest
    ): Response<StudyTaskPreviewDto>

    @DELETE("api/learning/tasks/{taskId}/")
    suspend fun deleteStudyTask(
        @Path("taskId") taskId: String
    ): Response<Unit>

}
