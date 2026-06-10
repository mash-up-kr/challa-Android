package com.happyhouse.challa.presentation.room.waiting.model

import androidx.compose.runtime.Immutable

@Immutable
data class ShareLinkUiModel(
    val link: String,
    val title: String,
    val description: String,
)
