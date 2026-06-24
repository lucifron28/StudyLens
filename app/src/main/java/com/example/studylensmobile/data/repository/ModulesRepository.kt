package com.example.studylensmobile.data.repository

import com.example.studylensmobile.core.format.toDisplayLabel
import com.example.studylensmobile.core.format.toPreview
import com.example.studylensmobile.core.format.toReadableDate
import com.example.studylensmobile.data.remote.networkFailure
import com.example.studylensmobile.data.remote.toResult
import com.example.studylensmobile.data.remote.api.LearningApi
import com.example.studylensmobile.data.remote.dto.ModuleDto
import com.example.studylensmobile.domain.model.LearningModule

class ModulesRepository(
    private val learningApi: LearningApi
) {
    suspend fun getModuleReader(moduleId: String): Result<LearningModule> {
        return try {
            val module = learningApi.getModule(moduleId)
                .toResult("Module") { it }
                .getOrElse { return Result.failure(it) }

            Result.success(module.toDomain())
        } catch (e: Exception) {
            networkFailure(e)
        }
    }
}

private fun ModuleDto.toDomain(): LearningModule {
    val readableContent = listOf(markdownContent, extractedText, description)
        .firstOrNull { it.isNotBlank() }
        .orEmpty()

    return LearningModule(
        id = id.toString(),
        subjectId = subject.toString(),
        title = title,
        contentPreview = readableContent.toPreview(),
        subjectTitle = subjectTitle,
        description = description,
        contentType = contentType.toDisplayLabel(),
        markdownContent = markdownContent,
        extractedText = extractedText,
        moduleFileUrl = moduleFileUrl,
        isFavorite = isFavorite,
        updatedAt = updatedAt.toReadableDate()
    )
}
