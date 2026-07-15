package com.happyhouse.challa.presentation.home.shareinvite

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface ShareInviteSideEffect : UiSideEffect {
    data class KakaoShareRequested(
        val inviteLink: String,
        val roomName: String,
    ) : ShareInviteSideEffect
}
