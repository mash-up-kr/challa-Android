package com.happyhouse.challa.presentation.home.createroom

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface CreateRoomIntent : UiIntent {
    data class NameChanged(
        val name: String,
    ) : CreateRoomIntent

    data class ShotCountChanged(
        val shotCount: ShotCount,
    ) : CreateRoomIntent

    data object CreateClick : CreateRoomIntent

    /**
     * 폼 상태를 초기화하고 진행 중이던 방 생성 코루틴을 취소한다.
     * 바텀시트를 열 때(이전 입력 제거)와 닫을 때(대기 중인 이펙트 정리) 모두 사용한다.
     */
    data object Reset : CreateRoomIntent
}
