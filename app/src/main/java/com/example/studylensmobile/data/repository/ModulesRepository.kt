package com.example.studylensmobile.data.repository

import com.example.studylensmobile.core.format.toDisplayLabel
import com.example.studylensmobile.core.format.toPreview
import com.example.studylensmobile.core.format.toReadableDate
import com.example.studylensmobile.data.remote.networkFailure
import com.example.studylensmobile.data.remote.toResult
import com.example.studylensmobile.data.remote.api.LearningApi
import com.example.studylensmobile.data.remote.dto.ChapterDto
import com.example.studylensmobile.data.remote.dto.ModuleDto
import com.example.studylensmobile.domain.model.LearningChapter
import com.example.studylensmobile.domain.model.LearningModule

class ModulesRepository(
    private val learningApi: LearningApi
) {
    suspend fun getModuleReader(moduleId: String): Result<LearningModule> {
        return try {
            val module = learningApi.getModule(moduleId)
                .toResult("Module") { it }
                .getOrElse { return Result.failure(it) }
            val chapters = learningApi.getChapters(moduleId = moduleId)
                .toResult("Chapters") { it.results }
                .getOrElse { return Result.failure(it) }

            Result.success(module.toDomain(chapters))
        } catch (e: Exception) {
            networkFailure(e)
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
