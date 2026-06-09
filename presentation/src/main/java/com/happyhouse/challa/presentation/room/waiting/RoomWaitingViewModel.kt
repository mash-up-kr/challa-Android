package com.happyhouse.challa.presentation.room.waiting

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.room.waiting.contract.RoomWaitingUiIntent
import com.happyhouse.challa.presentation.room.waiting.contract.RoomWaitingUiSideEffect
import com.happyhouse.challa.presentation.room.waiting.contract.RoomWaitingUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
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
                    copy(roomId = intent.roomId)
                }
                startCountdown(intent.opensAtMillis)
            }

            RoomWaitingUiIntent.ClickBack -> {
                viewModelScope.launch {
                    sendEffect(RoomWaitingUiSideEffect.NavigateBack)
                }
            }

            RoomWaitingUiIntent.ClickShare -> {
                viewModelScope.launch {
                    sendEffect(RoomWaitingUiSideEffect.ShowShareSheet)
                }
            }

            RoomWaitingUiIntent.ClickOpen -> {
                if (currentState.isReadyToOpen) {
                    viewModelScope.launch {
                        sendEffect(RoomWaitingUiSideEffect.NavigateToRoom)
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
                        )
                    }

                    if (remainingSeconds <= 0L) break

                    delay(COUNTDOWN_INTERVAL_MILLIS)
                }
            }
    }

    private fun Long.resolveOpensAtMillis(): Long =
        takeIf { it > 0L }
            ?: (System.currentTimeMillis() + DEFAULT_WAITING_DURATION_MILLIS)

    private fun Long.toRemainingSeconds(): Long = (coerceAtLeast(0L) + 999L) / 1_000L

    companion object {
        private const val DEFAULT_WAITING_DURATION_MILLIS = 10_800_000L
        private const val COUNTDOWN_INTERVAL_MILLIS = 1_000L
    }
}
