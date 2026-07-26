package com.happyhouse.challa.presentation.login

import android.app.Activity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 카카오 로그인 SDK 호출을 담당한다.
 *
 * 카카오톡 앱 로그인은 내부적으로 startActivity 로 카카오톡을 실행하므로 반드시 Activity Context 가
 * 필요하다. 그래서 이 호출은 Activity 가 살아 있는 UI 레이어에서 수행하고, 발급받은 액세스 토큰만
 * ViewModel 로 넘긴다. (data 레이어는 Activity 를 알 필요가 없다.)
 */
object KakaoLoginClient {
    /**
     * 카카오 로그인을 수행하고 ID 토큰(OIDC)을 반환한다.
     *
     * 서버 로그인 API 는 액세스 토큰이 아닌 ID 토큰을 요구한다. ID 토큰은 카카오 개발자 콘솔에서
     * OpenID Connect 가 활성화돼 있어야 발급되므로, 없으면 로그인을 실패로 처리한다.
     */
    suspend fun login(activity: Activity): String =
        loginWithKakaoSdk(activity).idToken
            ?: throw IllegalStateException("카카오 ID 토큰이 비어 있습니다. (OpenID Connect 설정 확인)")

    /**
     * 카카오 SDK 로그인을 코루틴으로 감싼다.
     *
     * 카카오톡 앱이 설치돼 있으면 앱 로그인을, 아니면(또는 앱 로그인이 사용자 취소 외의 이유로 실패하면)
     * 카카오 계정 웹 로그인으로 폴백한다.
     */
    private suspend fun loginWithKakaoSdk(activity: Activity): OAuthToken =
        suspendCancellableCoroutine { continuation ->
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
                UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                    // 사용자가 카카오톡 로그인을 직접 취소한 경우엔 폴백하지 않는다.
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        continuation.resumeWithException(error)
                    } else if (error != null) {
                        // 그 외 실패(카카오톡 미로그인 등)는 계정 로그인으로 폴백한다.
                        UserApiClient.instance.loginWithKakaoAccount(activity, callback = continuation.resumeCallback())
                    } else {
                        continuation.resumeCallback()(token, null)
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(activity, callback = continuation.resumeCallback())
            }
        }

    private fun Continuation<OAuthToken>.resumeCallback(): (OAuthToken?, Throwable?) -> Unit =
        { token, error ->
            when {
                error != null -> resumeWithException(error)
                token != null -> resume(token)
                else -> resumeWithException(IllegalStateException("카카오 로그인 토큰이 비어 있습니다."))
            }
        }
}
