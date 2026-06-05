package com.example.modulelensmobile.domain.model

data class Subject(
    val id: String,
    val code: String,
    val title: String,
    val description: String,
    val itemSummary: String,
    val progressPercentage: Int
)
