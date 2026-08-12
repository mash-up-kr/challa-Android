package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.result.ChallaResult
import kotlinx.coroutines.flow.Flow

/** 앱 알림 설정과 FCM registration token의 서버 동기화를 관리합니다. */
interface NotificationRepository {
    val isEnabled: Flow<ChallaResult<Boolean>>

    suspend fun setEnabled(enabled: Boolean): ChallaResult<Unit>

    /**
     * 새 token을 저장하고 로그인 상태이면 서버에 등록합니다.
     * 기존 token과 다르면 서버에서 기존 token을 먼저 삭제합니다.
     */
    suspend fun updatePushToken(token: String): ChallaResult<Unit>

    /** 로그인 후 로컬에 보관된 token을 현재 사용자에게 등록합니다. */
    suspend fun registerSavedPushToken(): ChallaResult<Unit>

    /** 로그아웃 전에 로컬에 보관된 token을 현재 사용자에게서 삭제합니다. */
    suspend fun deleteSavedPushToken(): ChallaResult<Unit>

    /**
     * 현재 사용자에게 테스트 푸시를 전송하고 성공한 token 수를 반환합니다.
     * [title]이나 [body]가 null이면 해당 필드를 생략해 서버 기본값을 사용합니다.
     */
    suspend fun sendTestPush(
        title: String? = null,
        body: String? = null,
    ): ChallaResult<Int>
}
