package com.happyhouse.challa.presentation.home.contract

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface HomeIntent : UiIntent {
    /** 홈이 화면에 올라올 때. 최초 진입과 다른 화면에서 돌아온 경우에 모두 사용한다. */
    data object ScreenResume : HomeIntent

    /** 인화 대기 카드의 카운트다운이 0에 닿았을 때 */
    data object PrintCountdownFinish : HomeIntent
}
