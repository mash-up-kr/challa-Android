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
import kotlinx.coroutines.flow.filterNotNull
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
            observeProfile()
            prefetchProfile()
            loadHome()
        }

        override fun onIntent(intent: HomeIntent) = Unit

        private fun observeProfile() {
            viewModelScope.launch {
                userRepository.profile.filterNotNull().collect { profile ->
                    val nickname = profile.nickname ?: return@collect
                    updateState {
                        copy(
                            nickname = nickname,
                            profileImageUrl = profile.profileImageUrl,
                        )
                    }
                }
            }
        }

        private fun prefetchProfile() {
            viewModelScope.launch {
                userRepository.prefetchMyProfile()
            }
        }

        private fun loadHome() {
            viewModelScope.launch {
                updateState { copy(isLoading = true) }
                roomRepository
                    .getRoomList(ALL_ROOM_STATUSES)
                    .onSuccess { rooms ->
                        val roomUiModels = rooms.mapNotNull { it.toUiModel() }.toImmutableList()
                        updateState {
                            copy(
                                isLoading = false,
                                hasLoadedRooms = true,
                                rooms = roomUiModels,
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
