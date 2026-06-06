package com.example.modulelensmobile.feature.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.modulelensmobile.data.repository.SubjectsRepository

class SubjectsViewModelFactory(
    private val subjectsRepository: SubjectsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectsViewModel(subjectsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
