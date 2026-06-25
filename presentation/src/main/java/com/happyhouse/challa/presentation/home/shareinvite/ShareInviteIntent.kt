package com.happyhouse.challa.presentation.home.shareinvite

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface ShareInviteIntent : UiIntent {
    data class KakaoShareClick(
        val link: String,
    ) : ShareInviteIntent
}
