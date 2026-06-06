package com.example.modulelensmobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SummaryRequest(
    @SerializedName("module_id") val moduleId: Int? = null,
    @SerializedName("chapter_id") val chapterId: Int? = null,
    @SerializedName("board_scan_id") val boardScanId: Int? = null,
    val text: String? = null
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
