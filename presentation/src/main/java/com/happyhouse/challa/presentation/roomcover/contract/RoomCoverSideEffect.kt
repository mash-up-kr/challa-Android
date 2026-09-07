package com.happyhouse.challa.presentation.roomcover.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface RoomCoverSideEffect : UiSideEffect {
    /** 커버를 저장하지 못했을 때. 화면은 직전에 저장된 커버로 되돌아간다. */
    data object CoverUpdateFailed : RoomCoverSideEffect

    /** 배경 이미지를 올리지 못했을 때 */
    data object BackgroundImageUploadFailed : RoomCoverSideEffect
}
