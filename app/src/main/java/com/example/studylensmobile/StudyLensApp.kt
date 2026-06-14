package com.example.studylensmobile

import android.app.Application
import com.example.studylensmobile.core.network.AppContainer

class StudyLensApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
