package com.happyhouse.challa.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.model.PrimaryTheme
import com.happyhouse.challa.domain.repository.AuthRepository
import com.happyhouse.challa.domain.repository.ThemeRepository
import com.happyhouse.challa.domain.repository.UserRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.presentation.navigation.ChallaRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChallaAppViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        themeRepository: ThemeRepository,
        private val userRepository: UserRepository,
    ) : ViewModel() {
        val primaryTheme: StateFlow<PrimaryTheme> =
            themeRepository.primaryTheme
                .mapNotNull { result ->
                    when (result) {
                        is ChallaResult.Success -> result.data
                        is ChallaResult.Failure -> null
                    }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = PrimaryTheme.LEMONADE,
                )

        /**
         * 앱 시작 시 저장된 토큰과 프로필 설정 여부로 초기 화면을 정한다.
         * 토큰이 없으면 [ChallaRoute.Login], 토큰은 있지만 닉네임이 없으면 [ChallaRoute.SettingProfile],
         * 프로필까지 설정된 유저면 [ChallaRoute.Home] 으로 시작한다.
         * 초기 화면을 아직 확인하지 못한 로딩 상태는 null 로 표현한다.
         */
        val startRoute: StateFlow<ChallaRoute?> =
            flow {
                emit(resolveStartRoute())
            }.catch {
                emit(ChallaRoute.Login)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null,
            )

        private suspend fun resolveStartRoute(): ChallaRoute {
            if (!authRepository.isLoggedIn()) return ChallaRoute.Login
            return when (val result = userRepository.getMyProfile()) {
                is ChallaResult.Success ->
                    if (result.data.nickname.isNullOrBlank()) {
                        ChallaRoute.SettingProfile
                    } else {
                        ChallaRoute.Home()
                    }
                // 프로필 조회에 실패하면 로그인 화면으로 보낸다.
                is ChallaResult.Failure -> ChallaRoute.Login
            }
        }
    }
