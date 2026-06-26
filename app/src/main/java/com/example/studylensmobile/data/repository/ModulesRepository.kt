package com.example.studylensmobile.data.repository

import com.example.studylensmobile.core.format.toDisplayLabel
import com.example.studylensmobile.core.format.toPreview
import com.example.studylensmobile.core.format.toReadableDate
import com.example.studylensmobile.data.local.dao.ModuleDao
import com.example.studylensmobile.data.local.entity.toDomain
import com.example.studylensmobile.data.local.entity.toEntity
import com.example.studylensmobile.data.remote.networkFailure
import com.example.studylensmobile.data.remote.toResult
import com.example.studylensmobile.data.remote.api.LearningApi
import com.example.studylensmobile.data.remote.dto.ModuleDto
import com.example.studylensmobile.domain.model.LearningModule

import com.example.studylensmobile.data.repository.AiCacheInvalidator
import com.example.studylensmobile.data.remote.dto.ModuleWriteRequest

class ModulesRepository(
    private val learningApi: LearningApi,
    private val moduleDao: ModuleDao,
    private val aiCacheInvalidator: AiCacheInvalidator
) {
    suspend fun updateModule(
        moduleId: String,
        title: String,
        description: String,
        contentType: String,
        markdownContent: String? = null
    ): Result<LearningModule> {
        return try {
            val response = learningApi.updateModule(
                moduleId = moduleId,
                request = ModuleWriteRequest(
                    title = title.trim(),
                    description = description.trim(),
                    contentType = contentType.toApiContentType(),
                    markdownContent = markdownContent?.trim()?.takeIf { it.isNotBlank() }
                )
            )
            if (!response.isSuccessful) {
                return Result.failure(Exception("Failed to update module: ${response.message()}"))
            }
            val dto = response.body() ?: return Result.failure(Exception("Empty response body"))
            val domain = dto.toDomain()
            moduleDao.upsert(domain.toEntity())
            aiCacheInvalidator.invalidateSource("module", moduleId)
            Result.success(domain)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getModuleReader(moduleId: String): Result<LearningModule> {
        return try {
            val module = learningApi.getModule(moduleId)
                .toResult("Module") { it }
                .getOrElse { return getCachedModule(moduleId) ?: Result.failure(it) }

            val domain = module.toDomain()
            moduleDao.upsert(domain.toEntity())
            Result.success(domain)
        } catch (e: Exception) {
            getCachedModule(moduleId) ?: networkFailure(e)
        }
    }

    private suspend fun getCachedModule(moduleId: String): Result<LearningModule>? {
        val cached = moduleDao.getById(moduleId)
        return if (cached != null) Result.success(cached.toDomain()) else null
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

private fun String.toApiContentType(): String {
    return trim()
        .lowercase()
        .replace(" ", "_")
        .ifBlank { "markdown" }
}
