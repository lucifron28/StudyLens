package com.example.modulelensmobile.domain.model

data class BoardScan(
    val id: String,
    val title: String,
    val subjectCode: String,
    val dateLabel: String,
    val reviewStatus: String,
    val previewText: String,
    val subjectId: String? = null,
    val subjectTitle: String = "",
    val moduleId: String? = null,
    val moduleTitle: String = "",
    val chapterId: String? = null,
    val chapterTitle: String = "",
    val imageUrl: String? = null,
    val rawOcrText: String = "",
    val cleanedText: String = "",
    val summary: String = "",
    val tags: List<BoardScanTag> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String = ""
)

data class BoardScanTag(
    val id: String,
    val name: String,
    val color: String = ""
)
