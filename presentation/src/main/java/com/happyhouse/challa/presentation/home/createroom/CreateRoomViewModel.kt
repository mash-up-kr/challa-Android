package com.happyhouse.challa.presentation.home.createroom

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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
        override fun onIntent(intent: CreateRoomIntent) {
            when (intent) {
                is CreateRoomIntent.NameChanged -> onNameChanged(intent.name)
                CreateRoomIntent.CreateClick -> createRoom()
                CreateRoomIntent.CloseClick -> cancelRoomCreation()
            }
        }

        private fun onNameChanged(name: String) {
            val truncated = name.take(ROOM_NAME_MAX_LENGTH)
            updateState { copy(name = truncated) }
        }

        private fun cancelRoomCreation() {
            viewModelScope.launch {
                sendEffect(CreateRoomSideEffect.RoomCreationCancelled)
            }
        }

        private fun createRoom() {
            if (!currentState.canSubmit) return
            viewModelScope.launch {
                updateState { copy(isSubmitting = true) }
                delay(1000L) // TODO JH: API 호출
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
