package com.happyhouse.challa.presentation.gallery.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface GalleryIntent : UiIntent {
    // 최초 진입 및 에러 후 재시도에 모두 사용한다.
    data object PhotosLoad : GalleryIntent

    /** 그리드 끝이 가까워져 다음 사진 페이지가 필요할 때 */
    data object PhotosLoadMore : GalleryIntent

    data class PhotoClick(
        val photoId: Long,
    ) : GalleryIntent

    /** 프로필 바를 눌러 초대 메뉴를 열고 닫는다. */
    data object ProfileBarClick : GalleryIntent

    /** 메뉴 바깥을 눌러 초대 메뉴를 닫는다. */
    data object InviteMenuDismiss : GalleryIntent

    /** 초대 코드를 눌러 클립보드에 복사한다. */
    data object InviteCodeClick : GalleryIntent

    data object PrintCountdownClick : GalleryIntent

    data object ShootClick : GalleryIntent
}
