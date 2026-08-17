package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.result.ChallaResult
import kotlinx.coroutines.flow.Flow

/** 앱 알림 설정과 FCM registration token의 서버 동기화를 관리합니다. */
interface NotificationRepository {
    val isEnabled: Flow<ChallaResult<Boolean>>

    /** 알림 사용 여부를 저장하고 현재 기기의 token 등록 상태를 서버와 동기화합니다. */
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
}
