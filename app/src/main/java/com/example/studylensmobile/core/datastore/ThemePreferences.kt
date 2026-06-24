package com.example.studylensmobile.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

enum class ThemeOption {
    SYSTEM, LIGHT, DARK
}

class ThemePreferences(private val context: Context) {
    
    companion object {
        private val THEME_KEY = stringPreferencesKey("app_theme_option")
    }

    val themeOption: Flow<ThemeOption> = context.themeDataStore.data.map { prefs ->
        val name = prefs[THEME_KEY] ?: ThemeOption.SYSTEM.name
        try {
            ThemeOption.valueOf(name)
        } catch (e: Exception) {
            ThemeOption.SYSTEM
        }
    }

    suspend fun setThemeOption(option: ThemeOption) {
        context.themeDataStore.edit { prefs ->
            prefs[THEME_KEY] = option.name
        }
    }
}
