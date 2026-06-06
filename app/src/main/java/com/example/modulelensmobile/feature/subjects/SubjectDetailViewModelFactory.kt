package com.example.modulelensmobile.feature.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.modulelensmobile.data.repository.SubjectsRepository

class SubjectDetailViewModelFactory(
    private val subjectId: String,
    private val subjectsRepository: SubjectsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectDetailViewModel(subjectId, subjectsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
