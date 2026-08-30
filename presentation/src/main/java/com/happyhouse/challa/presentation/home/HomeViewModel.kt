package com.happyhouse.challa.presentation.home

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.event.RoomEvent
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.repository.UserRepository
import com.happyhouse.challa.domain.result.onFailure
import com.happyhouse.challa.domain.result.onSuccess
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.home.contract.HomeIntent
import com.happyhouse.challa.presentation.home.contract.HomeRoomLoadState
import com.happyhouse.challa.presentation.home.contract.HomeSideEffect
import com.happyhouse.challa.presentation.home.contract.HomeState
import com.happyhouse.challa.presentation.home.model.toUiModel
import com.happyhouse.challa.presentation.home.model.withName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val roomRepository: RoomRepository,
        private val userRepository: UserRepository,
    ) : BaseViewModel<HomeState, HomeIntent, HomeSideEffect>(initialState = HomeState()) {
        private var loadJob: Job? = null

        init {
            observeProfile()
            prefetchProfile()
            observeRoomEvents()
        }

        // 최초 진입 로드도 ScreenResume이 맡는다. 그래야 홈이 다시 올라올 때마다 같은 경로로 목록을 갱신할 수 있다.
        override fun onIntent(intent: HomeIntent) {
            when (intent) {
                HomeIntent.ScreenResume -> loadHome()
            }
        }

        /**
         * 방 설정에서 이름을 바꾸면 목록의 해당 방 이름만 갈아끼운다.
         *
         * 홈으로 돌아올 때 목록을 다시 받지 않으므로, 이 구독이 없으면 이전 이름이 그대로 남는다.
         * 목록에 없는 방(예: 다른 화면에서 바뀐 방)이면 아무것도 바뀌지 않는다.
         */
        private fun observeRoomEvents() {
            viewModelScope.launch {
                roomRepository.roomEventFlow.filterIsInstance<RoomEvent.TitleUpdate>()
                    .collect { event ->
                        updateState {
                            copy(
                                rooms =
                                    rooms
                                        .map { room ->
                                            if (room.id == event.roomId) room.withName(event.title) else room
                                        }.toImmutableList(),
                            )
                        }
                    }
            }
        }

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

        /**
         * 방 목록을 받아온다.
         *
         * 최초 진입이든 갱신이든 조회하는 동안 로딩을 노출한다. 목록이 소리 없이 바뀌면 사용자가 무엇이 왜 달라졌는지 알 수 없다.
         *
         * 갱신 요청이 겹치면(예: 두 방의 카운트다운이 같이 끝남) 앞선 조회는 버리고 마지막 요청만 남긴다.
         */
        private fun loadHome() {
            loadJob?.cancel()
            loadJob =
                viewModelScope.launch {
                    updateState { copy(roomLoadState = HomeRoomLoadState.LOADING) }
                    roomRepository
                        .getRoomList(ALL_ROOM_STATUSES)
                        .onSuccess { rooms ->
                            val roomUiModels = rooms.mapNotNull { it.toUiModel() }.toImmutableList()
                            updateState {
                                copy(
                                    roomLoadState = HomeRoomLoadState.LOADED,
                                    rooms = roomUiModels,
                                )
                            }
                        }.onFailure {
                            updateState { copy(roomLoadState = HomeRoomLoadState.FAILED) }
                            sendEffect(HomeSideEffect.RoomsLoadFailed)
                        }
                }
        }

        companion object {
            /** 홈 화면은 촬영 중/인화 대기/인화 완료 방을 모두 노출한다. UNKNOWN 타입은 repoImpl에서 필터링된다. */
            private val ALL_ROOM_STATUSES = RoomStatus.entries.toList()
        }
    }
