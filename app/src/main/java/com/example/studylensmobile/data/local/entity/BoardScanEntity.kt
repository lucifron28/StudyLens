package com.example.studylensmobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * BoardScan is stored as a flat entity. Tags are stored as a comma-separated string
 * (e.g. "tag1Id:tag1Name:tag1Color,tag2Id:tag2Name:tag2Color") to avoid a join table
 * for this simple offline-read use case.
 */
@Entity(tableName = "board_scans")
data class BoardScanEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subjectCode: String,
    val dateLabel: String,
    val reviewStatus: String,
    val previewText: String,
    val subjectId: String?,
    val subjectTitle: String,
    val moduleId: String?,
    val moduleTitle: String,
    val imageUrl: String?,
    val rawOcrText: String,
    val cleanedText: String,
    val summary: String,
    val tagsCsv: String,       // serialized tags
    val createdAt: String,
    val updatedAt: String
)
