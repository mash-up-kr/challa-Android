package com.happyhouse.challa.presentation.gallery.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface GalleryIntent : UiIntent {
    // 최초 진입 및 에러 후 재시도에 모두 사용한다.
    data object PhotosLoad : GalleryIntent

    /** 그리드 끝이 가까워져 다음 사진 페이지가 필요할 때 */
    data object PhotosLoadMore : GalleryIntent

    /** 실시간 참여 이벤트를 받은 뒤 프로필 바의 참여자 목록을 다시 조회할 때 */
    data object MembersRefresh : GalleryIntent

    data class PhotoClick(
        val photoId: Long,
    ) : GalleryIntent

    /** 초대 메뉴를 열고 닫는 토글 */
    data object ProfileBarClick : GalleryIntent

    /** 메뉴 바깥을 눌렀을 때 */
    data object InviteMenuDismiss : GalleryIntent

    data object PrintCountdownClick : GalleryIntent

    data object ShootClick : GalleryIntent
}
