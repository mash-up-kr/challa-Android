package com.happyhouse.challa

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.happyhouse.challa.deeplink.parseInvitationCode
import com.happyhouse.challa.presentation.invite.RoomInviteViewModel
import com.happyhouse.challa.ui.ChallaApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val roomInviteViewModel: RoomInviteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.dark(
                    scrim = Color.TRANSPARENT,
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

        handleDeepLink(intent)

        setContent {
            ChallaApp()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        // 나중에 다른 딥링크를 처리하게 될 일이 있으면 수정한다.
        val invitationCode = intent.parseInvitationCode() ?: return
        intent.data = null
        roomInviteViewModel.onInvitationReceived(invitationCode)
    }
}
