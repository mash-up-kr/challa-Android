package com.happyhouse.challa.presentation.login

import com.happyhouse.challa.presentation.base.UiIntent

sealed interface LoginIntent : UiIntent {
    /**
     * 카카오 로그인 버튼 클릭.
     *
     * [acquireKakaoToken] 은 Activity 가 필요한 카카오 SDK 호출을 UI 레이어에서 주입한 것으로,
     * ViewModel 은 Activity 를 직접 참조하지 않고 토큰만 받아 처리한다.
     */
    class LoginClick(val acquireKakaoToken: suspend () -> String) : LoginIntent
}
