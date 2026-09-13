package com.happyhouse.challa.presentation.logging

/**
 * 앱의 오류 진단 정보를 수집 서비스에 전달하는 추상화입니다.
 *
 * 구현체가 사용하는 외부 SDK를 presentation 모듈에 노출하지 않습니다. 사용자 입력, 인증 토큰, URL 등
 * 개인정보나 민감한 정보는 전달하지 않아야 합니다.
 */
interface CrashReporter {
    /**
     * 이후 오류 보고서에 연결할 내부 사용자 식별자를 설정합니다.
     *
     * 직접 식별할 수 없는 서버 내부 ID를 사용하며, `null`을 전달하면 기존 식별자를 제거합니다.
     */
    fun setUserId(userId: String?)

    /** 이후 오류 보고서에 첨부할 문자열 상태를 설정하거나 동일한 [key]의 값을 갱신합니다. */
    fun setCustomKey(
        key: String,
        value: String,
    )

    /** 이후 오류 보고서에 첨부할 Boolean 상태를 설정하거나 동일한 [key]의 값을 갱신합니다. */
    fun setCustomKey(
        key: String,
        value: Boolean,
    )

    /** 이후 오류 보고서에 첨부할 정수 상태를 설정하거나 동일한 [key]의 값을 갱신합니다. */
    fun setCustomKey(
        key: String,
        value: Int,
    )

    /** 크래시 직전의 중요한 작업 경계를 오류 보고서 로그에 남깁니다. */
    fun log(message: String)

    /**
     * 앱이 복구했지만 개발자가 확인해야 하는 예상 밖 예외를 non-fatal 오류로 기록합니다.
     *
     * 네트워크 단절이나 잘못된 사용자 입력처럼 정상적으로 처리되는 실패에는 사용하지 않습니다.
     */
    fun recordException(throwable: Throwable)
}
