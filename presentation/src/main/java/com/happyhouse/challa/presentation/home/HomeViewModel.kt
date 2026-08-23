package com.happyhouse.challa.presentation.home

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.repository.UserRepository
import com.happyhouse.challa.domain.result.onFailure
import com.happyhouse.challa.domain.result.onSuccess
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.home.contract.HomeIntent
import com.happyhouse.challa.presentation.home.contract.HomeSideEffect
import com.happyhouse.challa.presentation.home.contract.HomeState
import com.happyhouse.challa.presentation.home.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val roomRepository: RoomRepository,
        private val userRepository: UserRepository,
    ) : BaseViewModel<HomeState, HomeIntent, HomeSideEffect>(
            initialState = HomeState(isLoading = true),
        ) {
        init {
            loadProfile()
            loadHome()
        }

        override fun onIntent(intent: HomeIntent) = Unit

        private fun loadProfile() {
            viewModelScope.launch {
                userRepository.getMyProfile().onSuccess { profile ->
                    updateState {
                        copy(
                            nickname = profile.nickname,
                            profileImageUrl = profile.profileImageUrl,
                        )
                    }
                }
            }
        }

        private fun loadHome() {
            viewModelScope.launch {
                updateState { copy(isLoading = true) }
                roomRepository
                    .getRoomList(ALL_ROOM_STATUSES)
                    .onSuccess { rooms ->
                        updateState {
                            copy(
                                isLoading = false,
                                rooms = rooms.mapNotNull { it.toUiModel() }.toImmutableList(),
                            )
                        }
                    }.onFailure {
                        updateState { copy(isLoading = false) }
                        sendEffect(HomeSideEffect.RoomsLoadFailed)
                    }
            }
        }

        companion object {
            /** 홈 화면은 촬영 중/인화 대기/인화 완료 방을 모두 노출한다. UNKNOWN 타입은 repoImpl에서 필터링된다. */
            private val ALL_ROOM_STATUSES = RoomStatus.entries.toList()
        }
    }
