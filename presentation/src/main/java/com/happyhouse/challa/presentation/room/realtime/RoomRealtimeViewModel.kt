package com.happyhouse.challa.presentation.room.realtime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.model.RoomMemberJoinedEvent
import com.happyhouse.challa.domain.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomRealtimeViewModel
    @Inject
    constructor(
        private val roomRepository: RoomRepository,
    ) : ViewModel() {
        private val _events = Channel<RoomMemberJoinedEvent>(capacity = Channel.BUFFERED)
        val events: Flow<RoomMemberJoinedEvent> = _events.receiveAsFlow()

        private var observedRoomIds: Set<Long> = emptySet()
        private var observeJob: Job? = null
        private var isForeground = false

        fun startObserving() {
            if (isForeground) return

            isForeground = true
            restartObservation()
        }

        fun pauseObserving() {
            if (!isForeground) return

            isForeground = false
            cancelObservation()
        }

        fun observeRoom(roomId: Long) {
            observeRooms(observedRoomIds + roomId)
        }

        fun observeRooms(roomIds: Set<Long>) {
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
                    roomRepository.observeMemberJoined(subscribedRoomIds).collect(_events::send)
                }
        }

        private fun cancelObservation() {
            observeJob?.cancel()
            observeJob = null
        }

        fun stopObserving() {
            observedRoomIds = emptySet()
            cancelObservation()
        }
    }
