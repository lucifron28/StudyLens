package com.example.modulelensmobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ModuleDto(
    val id: Int,
    val subject: Int,
    @SerializedName("subject_title") val subjectTitle: String,
    val title: String,
    val description: String,
    @SerializedName("content_type") val contentType: String,
    @SerializedName("markdown_content") val markdownContent: String,
    @SerializedName("extracted_text") val extractedText: String,
    @SerializedName("module_file") val moduleFile: String?,
    @SerializedName("module_file_url") val moduleFileUrl: String?,
    @SerializedName("original_filename") val originalFilename: String,
    @SerializedName("is_favorite") val isFavorite: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class PaginatedChaptersDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<ChapterDto>
)

data class ChapterDto(
    val id: Int,
    val module: Int,
    @SerializedName("module_title") val moduleTitle: String,
    val title: String,
    val order: Int,
    @SerializedName("markdown_content") val markdownContent: String,
    @SerializedName("extracted_text") val extractedText: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)
