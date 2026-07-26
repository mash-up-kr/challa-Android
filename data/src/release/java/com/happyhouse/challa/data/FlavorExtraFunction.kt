package com.happyhouse.challa.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlavorExtraFunction
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun initializeFlipper() {
            // do nothing
        }

        fun getFlipperInterceptor(): Interceptor? = null
    }
