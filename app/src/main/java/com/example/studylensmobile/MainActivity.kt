package com.example.studylensmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.studylensmobile.ui.navigation.AppNavGraph
import com.example.studylensmobile.ui.theme.StudyLensTheme
import com.example.studylensmobile.core.datastore.ThemeOption

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as StudyLensApp
        setContent {
            val themeOption by app.container.themePreferences.themeOption.collectAsState(initial = ThemeOption.SYSTEM)
            val isDarkTheme = when (themeOption) {
                ThemeOption.SYSTEM -> isSystemInDarkTheme()
                ThemeOption.DARK -> true
                ThemeOption.LIGHT -> false
            }

            StudyLensTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController, app = app)
                }
            }
        }
    }
}
