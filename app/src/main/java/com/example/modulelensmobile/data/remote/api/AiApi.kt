package com.example.modulelensmobile.data.remote.api

import com.example.modulelensmobile.data.remote.dto.SummaryDto
import com.example.modulelensmobile.data.remote.dto.SummaryRequest
import com.example.modulelensmobile.data.remote.dto.FlashcardDto
import com.example.modulelensmobile.data.remote.dto.GenerateFlashcardsRequest
import com.example.modulelensmobile.data.remote.dto.GenerateQuizRequest
import com.example.modulelensmobile.data.remote.dto.QuizDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AiApi {
    @POST("api/ai/summarize/")
    suspend fun summarize(@Body request: SummaryRequest): Response<SummaryDto>

    @POST("api/ai/generate-flashcards/")
    suspend fun generateFlashcards(@Body request: GenerateFlashcardsRequest): Response<List<FlashcardDto>>

    @POST("api/ai/generate-quiz/")
    suspend fun generateQuiz(@Body request: GenerateQuizRequest): Response<QuizDto>
}
