package com.example.studylensmobile.data.repository

fun interface AiCacheInvalidator {
    fun invalidateSource(sourceType: String, sourceId: String)
}
