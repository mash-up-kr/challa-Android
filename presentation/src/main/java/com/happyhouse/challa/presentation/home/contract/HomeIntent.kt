package com.happyhouse.challa.presentation.home.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface HomeIntent : UiIntent {
    /**
     * 다른 화면에 다녀와 방 목록을 다시 받아야 할 때.
     *
     * 갤러리에서 인화 연출을 보고 오면 서버의 확인 시각이 채워지는데,
     * 갱신하지 않으면 홈이 낡은 값을 들고 있어 같은 방에서 연출이 다시 재생된다.
     */
    data object RoomsRefresh : HomeIntent
}
