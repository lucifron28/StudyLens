package com.example.studylensmobile.data.local.entity

import com.example.studylensmobile.domain.model.BoardScan
import com.example.studylensmobile.domain.model.BoardScanTag
import com.example.studylensmobile.domain.model.LearningModule
import com.example.studylensmobile.domain.model.Subject

// ── Subject ──────────────────────────────────────────────────────────────────
fun Subject.toEntity() = SubjectEntity(
    id = id,
    code = code,
    title = title,
    description = description,
    itemSummary = itemSummary
)

fun SubjectEntity.toDomain() = Subject(
    id = id,
    code = code,
    title = title,
    description = description,
    itemSummary = itemSummary
)

// ── LearningModule ────────────────────────────────────────────────────────────
fun LearningModule.toEntity() = ModuleEntity(
    id = id,
    subjectId = subjectId,
    title = title,
    contentPreview = contentPreview,
    subjectTitle = subjectTitle,
    description = description,
    contentType = contentType,
    markdownContent = markdownContent,
    extractedText = extractedText,
    moduleFileUrl = moduleFileUrl,
    isFavorite = isFavorite,
    updatedAt = updatedAt
)

fun ModuleEntity.toDomain() = LearningModule(
    id = id,
    subjectId = subjectId,
    title = title,
    contentPreview = contentPreview,
    subjectTitle = subjectTitle,
    description = description,
    contentType = contentType,
    markdownContent = markdownContent,
    extractedText = extractedText,
    moduleFileUrl = moduleFileUrl,
    isFavorite = isFavorite,
    updatedAt = updatedAt
)

// ── BoardScan ─────────────────────────────────────────────────────────────────
private const val TAG_SEPARATOR = ","
private const val TAG_FIELD_SEPARATOR = ":"

fun BoardScan.toEntity() = BoardScanEntity(
    id = id,
    title = title,
    subjectCode = subjectCode,
    dateLabel = dateLabel,
    reviewStatus = reviewStatus,
    previewText = previewText,
    subjectId = subjectId,
    subjectTitle = subjectTitle,
    moduleId = moduleId,
    moduleTitle = moduleTitle,
    imageUrl = imageUrl,
    rawOcrText = rawOcrText,
    cleanedText = cleanedText,
    summary = summary,
    tagsCsv = tags.joinToString(TAG_SEPARATOR) { "${it.id}$TAG_FIELD_SEPARATOR${it.name}$TAG_FIELD_SEPARATOR${it.color}" },
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun BoardScanEntity.toDomain() = BoardScan(
    id = id,
    title = title,
    subjectCode = subjectCode,
    dateLabel = dateLabel,
    reviewStatus = reviewStatus,
    previewText = previewText,
    subjectId = subjectId,
    subjectTitle = subjectTitle,
    moduleId = moduleId,
    moduleTitle = moduleTitle,
    imageUrl = imageUrl,
    rawOcrText = rawOcrText,
    cleanedText = cleanedText,
    summary = summary,
    tags = tagsCsv.split(TAG_SEPARATOR).mapNotNull { entry ->
        val parts = entry.split(TAG_FIELD_SEPARATOR)
        if (parts.size == 3) BoardScanTag(id = parts[0], name = parts[1], color = parts[2]) else null
    },
    createdAt = createdAt,
    updatedAt = updatedAt
)
