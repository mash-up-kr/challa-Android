package com.happyhouse.challa

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.happyhouse.challa.ui.ChallaApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.light(
                    scrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT,
                ),
            navigationBarStyle =
                SystemBarStyle.light(
                    scrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT,
                ),
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            ChallaApp()
        }
    }
}
