package com.happyhouse.challa.presentation.home.createroom

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.onFailure
import com.happyhouse.challa.domain.result.onSuccess
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.model.ROOM_NAME_MAX_LENGTH
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateRoomViewModel
    @Inject
    constructor(
        private val roomRepository: RoomRepository,
    ) : BaseViewModel<CreateRoomState, CreateRoomIntent, CreateRoomSideEffect>(
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
            val roomName = currentState.name.trim()
            createRoomJob =
                viewModelScope.launch {
                    updateState { copy(isSubmitting = true) }
                    roomRepository
                        .postRoom(
                            title = roomName,
                            totalPhotoCount = currentState.shotCount.count,
                        ).onSuccess { created ->
                            updateState { copy(isSubmitting = false) }
                            sendEffect(
                                CreateRoomSideEffect.RoomCreated(
                                    roomId = created.id,
                                    roomName = roomName,
                                ),
                            )
                        }.onFailure { failure ->
                            updateState { copy(isSubmitting = false) }
                            sendEffect(CreateRoomSideEffect.RoomCreateFailed(failure.serverMessage()))
                        }
                }
        }

        private fun ChallaResult.Failure.serverMessage(): String? =
            when (this) {
                is ChallaResult.Failure.Http -> message
                is ChallaResult.Failure.Unknown -> cause?.message
                is ChallaResult.Failure.Network -> null
            }
    }
