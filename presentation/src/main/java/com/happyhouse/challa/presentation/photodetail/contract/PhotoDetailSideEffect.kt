package com.happyhouse.challa.presentation.photodetail.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface PhotoDetailSideEffect : UiSideEffect {
    /** 다음 사진 페이지를 받지 못했을 때. 열려 있는 사진은 그대로 두고 알리기만 한다. */
    data object PhotosLoadMoreFailed : PhotoDetailSideEffect

    data object SaveSucceeded : PhotoDetailSideEffect

    data object SaveFailed : PhotoDetailSideEffect

    data object ReactionSendFailed : PhotoDetailSideEffect

    data object ReactionCancelFailed : PhotoDetailSideEffect

    /** 반응 목록을 받지 못했을 때. 남기거나 취소한 것 자체는 서버에 반영돼 있을 수 있다. */
    data object ReactionsLoadFailed : PhotoDetailSideEffect

    data object MessageSendFailed : PhotoDetailSideEffect
}
