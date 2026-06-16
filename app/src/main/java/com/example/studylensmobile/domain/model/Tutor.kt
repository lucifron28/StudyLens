package com.example.studylensmobile.domain.model

data class TutorSession(
    val id: String,
    val title: String,
    val status: String,
    val clearAnswersCount: Int,
    val targetClearAnswers: Int,
    val moduleTitle: String = "",
    val chapterTitle: String = "",
    val createdAt: String = ""
) {
    val progressLabel: String
        get() = "$clearAnswersCount / $targetClearAnswers clear"

    val isMastered: Boolean
        get() = status.equals("Mastered", ignoreCase = true)
}

data class TutorMessage(
    val id: String,
    val sessionId: String,
    val role: String,
    val content: String,
    val clarityResult: String = "",
    val createdAt: String = ""
) {
    val isFromStudent: Boolean
        get() = role.equals("User", ignoreCase = true)
}
