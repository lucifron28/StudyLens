package com.example.modulelensmobile.data.remote.api

import com.example.modulelensmobile.data.remote.dto.SummaryDto
import com.example.modulelensmobile.data.remote.dto.SummaryRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AiApi {
    @POST("api/ai/summarize/")
    suspend fun summarize(@Body request: SummaryRequest): Response<SummaryDto>
}
