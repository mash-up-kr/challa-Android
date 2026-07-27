package com.happyhouse.challa

import android.app.Application
import com.happyhouse.challa.data.FlavorExtraFunction
import com.happyhouse.challa.logging.ChallaLogger
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ChallaApplication : Application() {
    @Inject
    lateinit var flavorExtraFunction: FlavorExtraFunction

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            ChallaLogger.init()
            flavorExtraFunction.initializeFlipper()
        }
    }
}
