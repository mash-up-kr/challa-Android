package com.happyhouse.challa.presentation.photodetail.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface PhotoDetailSideEffect : UiSideEffect {
    data object SaveSucceeded : PhotoDetailSideEffect

    data object SaveFailed : PhotoDetailSideEffect
}
