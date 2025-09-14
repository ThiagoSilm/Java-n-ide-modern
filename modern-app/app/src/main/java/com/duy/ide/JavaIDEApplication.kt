package com.duy.ide

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class JavaIDEApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicialização dos componentes principais
    }
}