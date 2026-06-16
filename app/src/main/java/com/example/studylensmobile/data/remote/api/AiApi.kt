package com.example.studylensmobile.data.remote.api

import com.example.studylensmobile.data.remote.dto.FlashcardDto
import com.example.studylensmobile.data.remote.dto.GenerateFlashcardsRequest
import com.example.studylensmobile.data.remote.dto.GenerateQuizRequest
import com.example.studylensmobile.data.remote.dto.QuizDto
import com.example.studylensmobile.data.remote.dto.StartTutorRequest
import com.example.studylensmobile.data.remote.dto.SummaryDto
import com.example.studylensmobile.data.remote.dto.SummaryRequest
import com.example.studylensmobile.data.remote.dto.TutorMessageRequest
import com.example.studylensmobile.data.remote.dto.TutorResponseDto
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

    @POST("api/ai/start-tutor/")
    suspend fun startTutor(@Body request: StartTutorRequest): Response<TutorResponseDto>

    @POST("api/ai/tutor-message/")
    suspend fun sendTutorMessage(@Body request: TutorMessageRequest): Response<TutorResponseDto>
}
