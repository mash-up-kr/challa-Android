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

        fun observeRoom(roomId: Long) {
            observeRooms(observedRoomIds + roomId)
        }

        fun observeRooms(roomIds: Set<Long>) {
            val distinctRoomIds = roomIds.toSet()
            if (observedRoomIds == distinctRoomIds && observeJob?.isActive == true) return

            observeJob?.cancel()
            observedRoomIds = distinctRoomIds
            observeJob =
                distinctRoomIds
                    .takeIf { it.isNotEmpty() }
                    ?.let { subscribedRoomIds ->
                        viewModelScope.launch {
                            roomRepository.observeMemberJoined(subscribedRoomIds).collect(_events::send)
                        }
                    }
        }

        fun stopObserving() {
            observedRoomIds = emptySet()
            observeJob?.cancel()
            observeJob = null
        }
    }
