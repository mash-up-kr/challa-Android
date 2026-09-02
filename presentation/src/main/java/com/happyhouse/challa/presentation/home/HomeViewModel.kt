package com.happyhouse.challa.presentation.home

import androidx.lifecycle.viewModelScope
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
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
        /**
         * 인화 확인이 기록된 방. 목록 조회가 그 기록보다 먼저 끝나면 확인 전으로 내려오므로,
         * 여기 담아두고 새로 받은 목록에도 다시 씌운다.
         */
        private val printCheckedRoomIds = mutableSetOf<Long>()

        private var loadJob: Job? = null

        init {
            observeProfile()
            prefetchProfile()
        }

        // 최초 진입 로드도 ScreenResume이 맡는다. 그래야 홈이 다시 올라올 때마다 같은 경로로 목록을 갱신할 수 있다.
        override fun onIntent(intent: HomeIntent) {
            when (intent) {
                HomeIntent.ScreenResume -> loadHome()
                HomeIntent.PrintCountdownFinish -> loadHome()
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
         * 다만 첫 조회를 마친 뒤로는 이미 홈이 그려져 있으므로, 화면을 덮지 않고 진행 표시만 얹는 REFRESHING으로 알린다.
         *
         * 갱신 요청이 겹치면(예: 두 방의 카운트다운이 같이 끝남) 앞선 조회는 버리고 마지막 요청만 남긴다.
         */
        private fun loadHome() {
            loadJob?.cancel()
            loadJob =
                viewModelScope.launch {
                    // 첫 조회가 끝나기 전에는 LOADING 그대로 둔다. 그 뒤로는 어떤 상태에서 시작하든 갱신이다.
                    updateState {
                        if (roomLoadState == HomeRoomLoadState.LOADING) {
                            this
                        } else {
                            copy(roomLoadState = HomeRoomLoadState.REFRESHING)
                        }
                    }
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
            /**
             * 홈 화면은 촬영 중/인화 대기/인화 완료 방을 모두 노출한다.
             * UNKNOWN은 요청 파라미터에서 repoImpl이 빼주지만, 앱이 모르는 상태가 응답으로 내려오면
             * 다시 UNKNOWN으로 매핑되므로 [toUiModel]이 null을 돌려 목록에서 제외한다.
             */
            private val ALL_ROOM_STATUSES = RoomStatus.entries.toList()
        }
    }
