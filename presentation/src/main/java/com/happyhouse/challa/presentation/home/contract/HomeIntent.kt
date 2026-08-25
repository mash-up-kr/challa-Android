package com.happyhouse.challa.presentation.home.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface HomeIntent : UiIntent {
    /** 다른 화면에 다녀왔을 때. 낡은 목록을 들고 있으면 인화 연출이 다시 재생된다. */
    data object RoomsRefresh : HomeIntent
}
