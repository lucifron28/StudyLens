package com.example.modulelensmobile.feature.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.modulelensmobile.data.repository.ModulesRepository

class ModuleReaderViewModelFactory(
    private val moduleId: String,
    private val modulesRepository: ModulesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModuleReaderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ModuleReaderViewModel(moduleId, modulesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
