package com.example.modulelensmobile.data.repository

import com.example.modulelensmobile.data.remote.api.LearningApi
import com.example.modulelensmobile.data.remote.dto.ChapterDto
import com.example.modulelensmobile.data.remote.dto.ModuleDto
import com.example.modulelensmobile.domain.model.LearningChapter
import com.example.modulelensmobile.domain.model.LearningModule

class ModulesRepository(
    private val learningApi: LearningApi
) {
    suspend fun getModuleReader(moduleId: String): Result<LearningModule> {
        return try {
            val moduleResponse = learningApi.getModule(moduleId)
            if (!moduleResponse.isSuccessful) {
                return Result.failure(Exception("Module failed (${moduleResponse.code()}). Please try again."))
            }

            val module = moduleResponse.body()
                ?: return Result.failure(Exception("Module failed: empty server response."))

            val chaptersResponse = learningApi.getChapters(moduleId = moduleId)
            if (!chaptersResponse.isSuccessful) {
                return Result.failure(Exception("Chapters failed (${chaptersResponse.code()}). Please try again."))
            }

            val chapters = chaptersResponse.body()?.results.orEmpty()
            Result.success(module.toDomain(chapters))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}

private fun ModuleDto.toDomain(chapterDtos: List<ChapterDto>): LearningModule {
    val readableContent = listOf(markdownContent, extractedText, description)
        .firstOrNull { it.isNotBlank() }
        .orEmpty()

    return LearningModule(
        id = id.toString(),
        subjectId = subject.toString(),
        title = title,
        contentPreview = readableContent.toPreview(),
        progressPercentage = 0,
        subjectTitle = subjectTitle,
        description = description,
        contentType = contentType.toDisplayLabel(),
        markdownContent = markdownContent,
        extractedText = extractedText,
        moduleFileUrl = moduleFileUrl,
        isFavorite = isFavorite,
        updatedAt = updatedAt.toReadableDate(),
        chapters = chapterDtos.map { it.toDomain() }
    )
}

private fun ChapterDto.toDomain(): LearningChapter {
    return LearningChapter(
        id = id.toString(),
        moduleId = module.toString(),
        title = title,
        order = order,
        markdownContent = markdownContent,
        extractedText = extractedText,
        updatedAt = updatedAt.toReadableDate()
    )
}

private fun String.toPreview(): String {
    return replace("\n", " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(140)
}

private fun String.toDisplayLabel(): String {
    return split("_", "-", " ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
}

private fun String.toReadableDate(): String {
    return takeIf { it.length >= 10 }?.substring(0, 10) ?: this
}
