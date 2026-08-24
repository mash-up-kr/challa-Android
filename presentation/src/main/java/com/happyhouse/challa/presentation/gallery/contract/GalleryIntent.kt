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

    /** 초대 메뉴를 열고 닫는 토글 */
    data object ProfileBarClick : GalleryIntent

    /** 메뉴 바깥을 눌렀을 때 */
    data object InviteMenuDismiss : GalleryIntent

    data object PrintCountdownClick : GalleryIntent

    data object ShootClick : GalleryIntent

    /**
     * 인화 연출을 끝까지 봤을 때. 사진이 마지막까지 다 나타난 시점이다.
     *
     * 연출 진행은 화면이 들고 있고, 서버에 확인을 기록해야 하는 이 시점만 ViewModel로 올린다.
     */
    data object PrintAnimationComplete : GalleryIntent
}
