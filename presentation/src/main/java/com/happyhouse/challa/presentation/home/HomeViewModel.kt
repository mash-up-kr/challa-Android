package com.happyhouse.challa.presentation.home

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor() :
    BaseViewModel<HomeState, HomeIntent, HomeSideEffect>(
            initialState = HomeState(isLoading = true),
        ) {
        init {
            loadHome()
        }

        override fun onIntent(intent: HomeIntent) = Unit

        private fun loadHome() {
            viewModelScope.launch {
                updateState { copy(isLoading = true) }
                delay(1000L) // TODO JH: API 호출
                updateState {
                    copy(
                        isLoading = false,
                        // TODO JH: API 연동 시 실제 유저 정보로 대체
                        nickname = "나는야멋쟁이토마토",
                        profileImageUrl = null,
                        // 촬영중/촬영완료한 방이 없는 상태(케이스 1)
                        rooms = persistentListOf(),
                    )
                }
            }
        }
    }
