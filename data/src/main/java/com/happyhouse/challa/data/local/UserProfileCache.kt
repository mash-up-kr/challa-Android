package com.happyhouse.challa.data.local

import com.happyhouse.challa.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 현재 인증 세션에서 마지막으로 조회하거나 수정한 사용자 프로필을 메모리에 보관한다.
 *
 * 앱 프로세스가 종료되거나 로그인·로그아웃·회원 탈퇴·인증 만료로 세션이 변경되면 캐시를 비운다.
 */
@Singleton
class UserProfileCache
    @Inject
    constructor() {
        private val _profile = MutableStateFlow<UserProfile?>(null)
        val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

        fun update(profile: UserProfile) {
            _profile.value = profile
        }

        fun clear() {
            _profile.value = null
        }
    }
