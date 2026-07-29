package com.happyhouse.challa.data

import android.content.Context
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
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
        private lateinit var networkFlipperPlugin: NetworkFlipperPlugin

        fun initializeFlipper() {
            SoLoader.init(context, false)

            if (FlipperUtils.shouldEnableFlipper(context)) {
                networkFlipperPlugin = NetworkFlipperPlugin()
                AndroidFlipperClient.getInstance(context).apply {
                    addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))
                    addPlugin(SharedPreferencesFlipperPlugin(context))
                    addPlugin(networkFlipperPlugin)
                    start()
                }
            }
        }

        fun getFlipperInterceptor(): Interceptor? =
            if (::networkFlipperPlugin.isInitialized) {
                FlipperOkhttpInterceptor(networkFlipperPlugin, true)
            } else {
                null
            }
    }
