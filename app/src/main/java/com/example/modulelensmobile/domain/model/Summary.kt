package com.example.modulelensmobile.domain.model

data class Summary(
    val id: String,
    val title: String,
    val content: String,
    val keyTakeaways: List<String>
)
