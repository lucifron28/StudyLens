package com.example.modulelensmobile

import android.app.Application
import com.example.modulelensmobile.core.network.AppContainer

class ModuleLensApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
