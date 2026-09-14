package com.happyhouse.challa.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.happyhouse.challa.presentation.logging.CrashReporter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCrashReporter
    @Inject
    constructor() : CrashReporter {
        private val crashlytics = FirebaseCrashlytics.getInstance()

        override fun setUserId(userId: String?) {
            crashlytics.setUserId(userId.orEmpty())
        }

        override fun setCustomKey(
            key: String,
            value: String,
        ) {
            crashlytics.setCustomKey(key, value)
        }

        override fun setCustomKey(
            key: String,
            value: Boolean,
        ) {
            crashlytics.setCustomKey(key, value)
        }

        override fun setCustomKey(
            key: String,
            value: Int,
        ) {
            crashlytics.setCustomKey(key, value)
        }

        override fun log(message: String) {
            crashlytics.log(message)
        }

        override fun recordException(throwable: Throwable) {
            crashlytics.recordException(throwable)
        }
    }
