package com.happyhouse.challa.presentation.home.shareinvite

import com.happyhouse.challa.presentation.base.UiState

data class ShareInviteState(
    val roomName: String = "",
    val inviteLink: String = "",
    val isLoading: Boolean = true,
) : UiState
