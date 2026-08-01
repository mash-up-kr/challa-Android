package com.happyhouse.challa.presentation.home.createroom

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

internal const val ROOM_NAME_MAX_LENGTH = 20

@HiltViewModel
class CreateRoomViewModel
    @Inject
    constructor() :
    BaseViewModel<CreateRoomState, CreateRoomIntent, CreateRoomSideEffect>(
            initialState = CreateRoomState(),
        ) {
        private var createRoomJob: Job? = null

        override fun onIntent(intent: CreateRoomIntent) {
            when (intent) {
                is CreateRoomIntent.NameChanged -> onNameChanged(intent.name)
                is CreateRoomIntent.ShotCountChanged -> updateState { copy(shotCount = intent.shotCount) }
                CreateRoomIntent.CreateClick -> createRoom()
                CreateRoomIntent.Reset -> reset()
            }
        }

        // 시트를 닫을 때 호출된다. 진행 중이던 생성 코루틴을 취소해, 수신자가 없는 사이 발행되어 채널에
        // 걸려 있던 RoomCreated 이펙트가 다음 오픈 때 뒤늦게 소비되어 오작동하는 것을 막는다.
        private fun reset() {
            createRoomJob?.cancel()
            createRoomJob = null
            updateState { CreateRoomState() }
        }

        private fun onNameChanged(name: String) {
            val truncated = name.take(ROOM_NAME_MAX_LENGTH)
            updateState { copy(name = truncated) }
        }

        private fun createRoom() {
            if (!currentState.canSubmit || currentState.isSubmitting) return
            createRoomJob =
                viewModelScope.launch {
                    updateState { copy(isSubmitting = true) }
                    // TODO JH: API 호출 (요청 시 currentState.shotCount.count 값을 함께 전송)
                    delay(1000L)
                    val created = mockCreateRoom(currentState.name.trim())
                    updateState { copy(isSubmitting = false) }
                    sendEffect(
                        CreateRoomSideEffect.RoomCreated(
                            roomId = created.id,
                            roomName = created.name,
                        ),
                    )
                }
        }

        private data class MockCreatedRoom(
            val id: String,
            val name: String,
        )

        private fun mockCreateRoom(name: String): MockCreatedRoom =
            MockCreatedRoom(
                id = "mock-${System.currentTimeMillis()}",
                name = name,
            )
    }
