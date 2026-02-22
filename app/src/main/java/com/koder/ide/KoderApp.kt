package com.koder.ide

import android.app.Application
import com.koder.ide.core.editor.LanguageManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KoderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LanguageManager.initialize(this)
    }
}
