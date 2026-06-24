package com.example.studylensmobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "modules")
data class ModuleEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val title: String,
    val contentPreview: String,
    val subjectTitle: String,
    val description: String,
    val contentType: String,
    val markdownContent: String,
    val extractedText: String,
    val moduleFileUrl: String?,
    val isFavorite: Boolean,
    val updatedAt: String
)
