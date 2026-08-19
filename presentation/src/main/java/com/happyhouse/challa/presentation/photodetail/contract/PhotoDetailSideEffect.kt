package com.happyhouse.challa.presentation.photodetail.contract

import com.happyhouse.challa.presentation.base.UiSideEffect

sealed interface PhotoDetailSideEffect : UiSideEffect {
    data object PhotosLoadFailed : PhotoDetailSideEffect

    /** 다음 사진 페이지를 받지 못했을 때. 열려 있는 사진은 그대로 두고 알리기만 한다. */
    data object PhotosLoadMoreFailed : PhotoDetailSideEffect

    data object SaveSucceeded : PhotoDetailSideEffect

    data object SaveFailed : PhotoDetailSideEffect

    data object ReactionSendFailed : PhotoDetailSideEffect

    /** 한 사진에 남길 수 있는 반응 수를 넘겨 눌렀을 때 */
    data object ReactionLimitExceeded : PhotoDetailSideEffect
}
