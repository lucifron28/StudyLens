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

class ModulesRepository(
    private val learningApi: LearningApi,
    private val moduleDao: ModuleDao
) {
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
