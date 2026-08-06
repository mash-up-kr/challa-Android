package com.happyhouse.challa.presentation.home.enterroom

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.onFailure
import com.happyhouse.challa.domain.result.onSuccess
import com.happyhouse.challa.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

internal const val ENTER_ROOM_CODE_LENGTH = 6

@HiltViewModel
class EnterRoomViewModel
    @Inject
    constructor(
        private val roomRepository: RoomRepository,
    ) : BaseViewModel<EnterRoomState, EnterRoomIntent, EnterRoomSideEffect>(
            initialState = EnterRoomState(),
        ) {
        private var enterRoomJob: Job? = null

        override fun onIntent(intent: EnterRoomIntent) {
            when (intent) {
                is EnterRoomIntent.CodeChanged -> onCodeChanged(intent.code)
                EnterRoomIntent.EnterClick -> enterRoom()
                EnterRoomIntent.Reset -> reset()
            }
        }

        // 시트를 닫을 때 호출된다. 진행 중이던 입장 코루틴을 취소해, 수신자가 없는 사이 발행되어 채널에
        // 걸려 있던 RoomEntered 이펙트가 다음 오픈 때 뒤늦게 소비되어 오작동하는 것을 막는다.
        private fun reset() {
            enterRoomJob?.cancel()
            enterRoomJob = null
            updateState { EnterRoomState() }
        }

        // 숫자 6자리 입장 코드만 허용한다.
        private fun onCodeChanged(code: String) {
            val digitsOnly = code.filter(Char::isDigit).take(ENTER_ROOM_CODE_LENGTH)
            updateState { copy(code = digitsOnly) }
        }

        private fun enterRoom() {
            if (!currentState.canSubmit || currentState.isSubmitting) return
            val code = currentState.code
            enterRoomJob =
                viewModelScope.launch {
                    updateState { copy(isSubmitting = true) }
                    roomRepository
                        .enterRoom(code = code)
                        .onSuccess { entered ->
                            updateState { copy(isSubmitting = false) }
                            sendEffect(EnterRoomSideEffect.RoomEntered(roomId = entered.id))
                        }.onFailure { failure ->
                            updateState { copy(isSubmitting = false) }
                            sendEffect(EnterRoomSideEffect.RoomEnterFailed(failure.serverMessage()))
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
