package com.happyhouse.challa.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.model.PrimaryTheme
import com.happyhouse.challa.domain.repository.AuthRepository
import com.happyhouse.challa.domain.repository.ThemeRepository
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
        authRepository: AuthRepository,
        themeRepository: ThemeRepository,
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
         * 앱 시작 시 저장된 토큰 유무로 초기 화면을 정한다.
         * 토큰이 있으면 [ChallaRoute.Home], 없으면 [ChallaRoute.Login] 으로 시작한다.
         * 토큰을 아직 확인하지 못한 로딩 상태는 null 로 표현한다.
         */
        val startRoute: StateFlow<ChallaRoute?> =
            flow {
                emit(if (authRepository.isLoggedIn()) ChallaRoute.Home else ChallaRoute.Login)
            }.catch {
                emit(ChallaRoute.Login)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null,
            )
    }
