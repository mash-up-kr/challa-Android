package com.happyhouse.challa.presentation.room.realtime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.model.RoomMemberJoinedEvent
import com.happyhouse.challa.domain.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 로그인 사용자가 속한 방의 실시간 참여 이벤트 구독을 앱 화면 범위에서 관리한다.
 *
 * [replaceObservedRooms]는 홈 API가 반환한 전체 방 목록으로 구독 대상을 교체하고,
 * [addObservedRoom]은 생성하거나 입장한 방 하나를 기존 대상에 추가한다.
 * [startObserving]과 [pauseObserving]은 `ChallaNavHost`의 foreground 생명주기와 연결된다.
 * 일시정지할 때 방 ID는 보존하므로 앱이 foreground로 돌아오면 같은 대상으로 다시 연결한다.
 * 로그아웃처럼 사용자 세션이 끝날 때는 [stopObserving]으로 방 ID까지 제거한다.
 */
@HiltViewModel
class RoomRealtimeViewModel
    @Inject
    constructor(
        private val roomRepository: RoomRepository,
    ) : ViewModel() {
        private val _events = MutableSharedFlow<RoomMemberJoinedEvent>()

        /** 전역 토스트와 현재 방 화면이 함께 수신할 방 참여 이벤트 stream. */
        val events: SharedFlow<RoomMemberJoinedEvent> = _events.asSharedFlow()

        private var observedRoomIds: Set<Long> = emptySet()
        private var observeJob: Job? = null
        private var isForeground = false

        /** 보존된 방 ID로 구독을 시작하거나 재개한다. 이미 foreground이면 아무 작업도 하지 않는다. */
        fun startObserving() {
            if (isForeground) return

            isForeground = true
            restartObservation()
        }

        /** 현재 연결만 취소하고 구독할 방 ID는 보존한다. */
        fun pauseObserving() {
            if (!isForeground) return

            isForeground = false
            cancelObservation()
        }

        /** [roomId]를 현재 구독 대상에 추가하고 연결을 갱신한다. */
        fun addObservedRoom(roomId: Long) {
            replaceObservedRooms(observedRoomIds + roomId)
        }

        /**
         * 현재 구독 대상을 [roomIds] 전체로 교체하고 연결을 갱신한다.
         *
         * 기존 대상에 방 하나만 추가하려면 [addObservedRoom]을 사용한다. 같은 목록을 이미 활성 구독 중이면
         * 불필요한 재연결을 하지 않으며, foreground가 아닐 때는 목록만 보존한다.
         */
        fun replaceObservedRooms(roomIds: Set<Long>) {
            val distinctRoomIds = roomIds.toSet()
            if (observedRoomIds == distinctRoomIds && (!isForeground || observeJob?.isActive == true)) return

            observedRoomIds = distinctRoomIds
            restartObservation()
        }

        private fun restartObservation() {
            cancelObservation()
            if (!isForeground || observedRoomIds.isEmpty()) return

            val subscribedRoomIds = observedRoomIds
            observeJob =
                viewModelScope.launch {
                    roomRepository.observeMemberJoined(subscribedRoomIds).collect(_events::emit)
                }
        }

        private fun cancelObservation() {
            observeJob?.cancel()
            observeJob = null
        }

        /** 사용자 세션 종료 시 구독 대상과 현재 연결을 모두 제거한다. */
        fun stopObserving() {
            observedRoomIds = emptySet()
            cancelObservation()
        }
    }
