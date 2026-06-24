package com.example.studylensmobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PaginatedBoardScansDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<BoardScanDto>
)

data class BoardScanDto(
    val id: Int,
    val subject: Int?,
    @SerializedName("subject_title") val subjectTitle: String?,
    val module: Int?,
    @SerializedName("module_title") val moduleTitle: String?,
    val image: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("raw_ocr_text") val rawOcrText: String,
    @SerializedName("cleaned_text") val cleanedText: String,
    val summary: String,
    @SerializedName("review_status") val reviewStatus: String,
    val tags: List<Int>,
    @SerializedName("tag_details") val tagDetails: List<BoardScanTagDto>,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class BoardScanWriteRequest(
    val subject: Int? = null,
    val module: Int? = null,
    @SerializedName("raw_ocr_text") val rawOcrText: String? = null,
    @SerializedName("cleaned_text") val cleanedText: String? = null,
    val summary: String? = null,
    @SerializedName("review_status") val reviewStatus: String? = null,
    val tags: List<Int>? = null
)

data class BoardScanTagDto(
    val id: Int,
    val name: String,
    val color: String?
)

data class BoardScanUpdateRequest(
    @SerializedName("cleaned_text") val cleanedText: String? = null,
    @SerializedName("review_status") val reviewStatus: String? = null
)
