package com.example.modulelensmobile.domain.model

data class Summary(
    val id: String,
    val title: String,
    val content: String,
    val keyTakeaways: List<String>,
    val sourceType: String = "",
    val moduleId: String? = null,
    val moduleTitle: String = "",
    val chapterId: String? = null,
    val chapterTitle: String = "",
    val boardScanId: String? = null,
    val isAiGenerated: Boolean = true,
    val createdAt: String = ""
)
