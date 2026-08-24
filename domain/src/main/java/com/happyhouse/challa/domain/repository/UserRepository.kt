package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.model.UserProfile
import com.happyhouse.challa.domain.result.ChallaResult
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    /** 현재 앱 프로세스에서 가장 최근에 조회하거나 수정한 프로필. */
    val profile: StateFlow<UserProfile?>

    /** 캐시된 프로필이 없으면 서버에서 조회해 [profile]을 채우는 best-effort 작업. */
    suspend fun prefetchMyProfile()

    suspend fun getMyProfile(): ChallaResult<UserProfile>

    suspend fun withdraw(): ChallaResult<Unit>

    suspend fun updateProfile(
        nickname: String,
        profileImageUrl: String?,
    ): ChallaResult<UserProfile>
}
