package com.happyhouse.challa.presentation.home.enterroom

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface EnterRoomIntent : UiIntent {
    data class CodeChanged(
        val code: String,
    ) : EnterRoomIntent

    data object EnterClick : EnterRoomIntent

    /**
     * 폼 상태를 초기화하고 진행 중이던 방 입장 코루틴을 취소한다.
     * 바텀시트를 열 때(이전 입력 제거)와 닫을 때(대기 중인 이펙트 정리) 모두 사용한다.
     */
    data object Reset : EnterRoomIntent
}
