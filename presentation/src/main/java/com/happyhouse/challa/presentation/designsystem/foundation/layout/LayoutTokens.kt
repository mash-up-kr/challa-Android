package com.happyhouse.challa.presentation.designsystem.foundation.layout

import androidx.compose.ui.unit.dp

object LayoutTokens {
    /**
     * 가운데 정렬된 컨텐츠 영역이 하단에 비워 두는 높이. 버튼 높이 54dp + 버튼 아래 간격 8dp.
     *
     * 시안에서 컨텐츠 영역은 버튼이 몇 줄이든 한 줄만큼만 비우고, 나머지 줄은 그 영역 위로 겹친다.
     * 프로필 설정 화면과 홈 화면이 같은 값을 써야 두 화면을 넘어갈 때 프로필 이미지가 제자리에 머무른다.
     */
    val ContentBottomReserve = 62.dp

    /** 프로필 설정 화면과 홈 화면이 함께 쓰는 프로필 이미지 지름 */
    val ProfileImageSize = 80.dp
}
