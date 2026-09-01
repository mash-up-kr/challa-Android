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
import com.happyhouse.challa.presentation.home.model.RoomUiModel
import com.happyhouse.challa.presentation.home.model.toUiModel
import com.happyhouse.challa.presentation.home.model.withCover
import com.happyhouse.challa.presentation.home.model.withName
import com.happyhouse.challa.presentation.home.model.withPrintChecked
import com.happyhouse.challa.presentation.roomcover.model.toCoverUiModel
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
    ) : BaseViewModel<HomeState, HomeIntent, HomeSideEffect>(initialState = HomeState()) {
        /**
         * 인화 확인이 기록된 방. 목록 조회가 그 기록보다 먼저 끝나면 확인 전으로 내려오므로,
         * 여기 담아두고 새로 받은 목록에도 다시 씌운다.
         */
        private val printCheckedRoomIds = mutableSetOf<Long>()

        init {
            observeProfile()
            prefetchProfile()
            loadHome()
            observeRoomEvents()
        }

        override fun onIntent(intent: HomeIntent) = Unit

        /** 다른 화면에 다녀왔을 때. 낡은 목록을 들고 있으면 인화 연출이 다시 재생된다. */
        fun onResume() {
            loadHome(showLoading = false)
        }

        /**
         * 방 설정에서 이름을 바꾸면 목록의 해당 방 이름만 갈아끼운다.
         *
         * 홈으로 돌아올 때 목록을 다시 받지 않으므로, 이 구독이 없으면 이전 이름이 그대로 남는다.
         * 목록에 없는 방(예: 다른 화면에서 바뀐 방)이면 아무것도 바뀌지 않는다.
         */
        private fun observeRoomEvents() {
            viewModelScope.launch {
                roomRepository.roomEventFlow.collect { event ->
                    when (event) {
                        is RoomEvent.TitleUpdate ->
                            updateRoom(event.roomId) { room -> room.withName(event.title) }

                        is RoomEvent.CoverUpdate ->
                            updateRoom(event.roomId) { room -> room.withCover(event.cover.toCoverUiModel()) }

                        is RoomEvent.PhotoPrintCompletionCheck -> {
                            printCheckedRoomIds += event.roomId
                            updateRoom(event.roomId) { room -> room.withPrintChecked() }
                        }
                    }
                }
            }
        }

        /** 목록에 없는 방(예: 다른 화면에서 바뀐 방)이면 아무것도 바뀌지 않는다. */
        private fun updateRoom(
            roomId: Long,
            transform: (RoomUiModel) -> RoomUiModel,
        ) {
            updateState {
                copy(
                    rooms =
                        rooms
                            .map { room -> if (room.id == roomId) transform(room) else room }
                            .toImmutableList(),
                )
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

        /** @param showLoading 최초 진입에서만 true. 재조회에서 로딩을 거치면 화면이 깜빡인다. */
        private fun loadHome(showLoading: Boolean = true) {
            viewModelScope.launch {
                if (showLoading) {
                    updateState { copy(roomLoadState = HomeRoomLoadState.LOADING) }
                }
                roomRepository
                    .getRoomList(ALL_ROOM_STATUSES)
                    .onSuccess { rooms ->
                        val roomUiModels =
                            rooms
                                .mapNotNull { it.toUiModel() }
                                .map { room ->
                                    if (room.id in printCheckedRoomIds) room.withPrintChecked() else room
                                }.toImmutableList()
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
