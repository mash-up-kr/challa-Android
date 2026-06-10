package com.happyhouse.challa.presentation.room.waiting

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.room.waiting.contract.RoomWaitingUiIntent
import com.happyhouse.challa.presentation.room.waiting.contract.RoomWaitingUiSideEffect
import com.happyhouse.challa.presentation.room.waiting.contract.RoomWaitingUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RoomWaitingViewModel :
    BaseViewModel<RoomWaitingUiState, RoomWaitingUiIntent, RoomWaitingUiSideEffect>(
        RoomWaitingUiState(),
    ) {
    private var countdownJob: Job? = null

    override fun onIntent(intent: RoomWaitingUiIntent) {
        when (intent) {
            is RoomWaitingUiIntent.Initialize -> {
                updateState {
                    copy(
                        roomId = intent.roomId,
                        isInitialized = false,
                    )
                }
                startCountdown(intent.opensAtMillis)
            }

            RoomWaitingUiIntent.ClickShare -> {
                val roomId = currentState.roomId

                viewModelScope.launch {
                    sendEffect(
                        RoomWaitingUiSideEffect.ShareLink(
                            link = "https://challa.app/rooms/$roomId",
                            title = "찰나 방에 초대합니다",
                            description = "링크를 통해 방에 입장해 주세요.",
                        ),
                    )
                }
            }

            RoomWaitingUiIntent.ClickOpen -> {
                if (currentState.isReadyToOpen) {
                    viewModelScope.launch {
                        sendEffect(RoomWaitingUiSideEffect.NavigateToRoom(currentState.roomId))
                    }
                }
            }
        }
    }

    private fun startCountdown(opensAtMillis: Long) {
        countdownJob?.cancel()

        val targetMillis = opensAtMillis.resolveOpensAtMillis()

        countdownJob =
            viewModelScope.launch {
                while (isActive) {
                    val remainingSeconds =
                        (targetMillis - System.currentTimeMillis()).toRemainingSeconds()

                    updateState {
                        copy(
                            opensAtMillis = targetMillis,
                            remainingSeconds = remainingSeconds,
                            isInitialized = true,
                        )
                    }

                    if (remainingSeconds <= 0L) break

                    delay(COUNTDOWN_INTERVAL_MILLIS)
                }
            }
    }

    // TODO: 서버/로컬 저장소에서 고정된 opensAtMillis를 받으면 fallback 제거
    private fun Long.resolveOpensAtMillis(): Long =
        takeIf { it > 0L }
            ?: (System.currentTimeMillis() + DEFAULT_WAITING_DURATION_MILLIS)

    private fun Long.toRemainingSeconds(): Long = (coerceAtLeast(0L) + 999L) / 1_000L

    companion object {
        private const val DEFAULT_WAITING_DURATION_MILLIS = 10_800_000L
        private const val COUNTDOWN_INTERVAL_MILLIS = 1_000L
    }
}
