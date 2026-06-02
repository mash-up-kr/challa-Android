package com.happyhouse.challa.logging

import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import timber.log.Timber

object ChallaLogger {
    fun init() {
        Logger.clearLogAdapters()
        Timber.uprootAll()

        val formatStrategy =
            PrettyFormatStrategy
                .newBuilder()
                .showThreadInfo(false)
                .methodCount(0)
                .methodOffset(5)
                .tag(LOGGER_TAG)
                .build()

        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))
        Timber.plant(
            object : Timber.DebugTree() {
                override fun log(
                    priority: Int,
                    tag: String?,
                    message: String,
                    t: Throwable?,
                ) {
                    Logger.log(priority, tag, message, t)
                }
            },
        )
    }

    private const val LOGGER_TAG = "Challa"
}
