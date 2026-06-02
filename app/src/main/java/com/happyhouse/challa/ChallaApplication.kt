package com.happyhouse.challa

import android.app.Application
import com.happyhouse.challa.logging.ChallaLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ChallaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            ChallaLogger.init()
        }
    }
}
