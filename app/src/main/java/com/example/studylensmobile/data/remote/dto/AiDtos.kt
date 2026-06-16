package com.example.studylensmobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SummaryRequest(
    @SerializedName("module_id") val moduleId: Int? = null,
    @SerializedName("chapter_id") val chapterId: Int? = null,
    @SerializedName("board_scan_id") val boardScanId: Int? = null,
    val text: String? = null
)

data class GenerateFlashcardsRequest(
    @SerializedName("module_id") val moduleId: Int? = null,
    @SerializedName("chapter_id") val chapterId: Int? = null,
    @SerializedName("board_scan_id") val boardScanId: Int? = null,
    val text: String? = null,
    val count: Int = 5
)

data class GenerateQuizRequest(
    @SerializedName("module_id") val moduleId: Int? = null,
    @SerializedName("chapter_id") val chapterId: Int? = null,
    @SerializedName("board_scan_id") val boardScanId: Int? = null,
    val text: String? = null,
    val count: Int = 5,
    @SerializedName("question_type") val questionType: String? = null
)

data class StartTutorRequest(
    @SerializedName("module_id") val moduleId: Int? = null,
    @SerializedName("chapter_id") val chapterId: Int? = null,
    @SerializedName("board_scan_id") val boardScanId: Int? = null,
    val title: String? = null
)

data class TutorMessageRequest(
    @SerializedName("session_id") val sessionId: Int,
    val message: String
)

data class SummaryDto(
    val id: Int,
    val module: Int?,
    @SerializedName("module_title") val moduleTitle: String?,
    val chapter: Int?,
    @SerializedName("chapter_title") val chapterTitle: String?,
    @SerializedName("board_scan") val boardScan: Int?,
    @SerializedName("source_type") val sourceType: String,
    val content: String,
    @SerializedName("is_ai_generated") val isAiGenerated: Boolean,
    @SerializedName("created_at") val createdAt: String
)

data class FlashcardDto(
    val id: Int,
    val module: Int?,
    @SerializedName("module_title") val moduleTitle: String?,
    val chapter: Int?,
    @SerializedName("chapter_title") val chapterTitle: String?,
    @SerializedName("board_scan") val boardScan: Int?,
    val question: String,
    val answer: String,
    @SerializedName("source_type") val sourceType: String,
    @SerializedName("is_ai_generated") val isAiGenerated: Boolean,
    val difficulty: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class TutorSessionDto(
    val id: Int,
    val module: Int?,
    @SerializedName("module_title") val moduleTitle: String?,
    val chapter: Int?,
    @SerializedName("chapter_title") val chapterTitle: String?,
    @SerializedName("board_scan") val boardScan: Int?,
    val title: String,
    val status: String,
    @SerializedName("clear_answers_count") val clearAnswersCount: Int,
    @SerializedName("target_clear_answers") val targetClearAnswers: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class TutorMessageDto(
    val id: Int,
    val session: Int,
    @SerializedName("session_title") val sessionTitle: String?,
    val role: String,
    val content: String,
    @SerializedName("clarity_result") val clarityResult: String?,
    @SerializedName("created_at") val createdAt: String
)

data class TutorResponseDto(
    val session: TutorSessionDto,
    val message: TutorMessageDto
)

data class QuizDto(
    val id: Int,
    val module: Int?,
    @SerializedName("module_title") val moduleTitle: String?,
    val chapter: Int?,
    @SerializedName("chapter_title") val chapterTitle: String?,
    @SerializedName("board_scan") val boardScan: Int?,
    val title: String,
    val description: String,
    @SerializedName("source_type") val sourceType: String,
    @SerializedName("is_ai_generated") val isAiGenerated: Boolean,
    val questions: List<QuizQuestionDto>,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class QuizQuestionDto(
    val id: Int,
    val quiz: Int,
    val question: String,
    @SerializedName("question_type") val questionType: String,
    val choices: List<String>?,
    @SerializedName("correct_answer") val correctAnswer: String,
    val explanation: String,
    val order: Int
)
