package com.example.studylensmobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val code: String,
    val title: String,
    val description: String,
    val itemSummary: String
)
