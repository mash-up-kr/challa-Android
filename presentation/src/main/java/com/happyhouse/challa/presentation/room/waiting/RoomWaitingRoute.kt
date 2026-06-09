package com.happyhouse.challa.presentation.room.waiting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.room.waiting.contract.RoomWaitingUiIntent
import com.happyhouse.challa.presentation.room.waiting.contract.RoomWaitingUiSideEffect

@Composable
fun RoomWaitingRoute(
    roomId: Long,
    opensAtMillis: Long,
    viewModel: RoomWaitingViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onOpenClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(roomId, opensAtMillis) {
        viewModel.onIntent(
            RoomWaitingUiIntent.Initialize(
                roomId = roomId,
                opensAtMillis = opensAtMillis,
            ),
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { sideEffect ->
            when (sideEffect) {
                RoomWaitingUiSideEffect.NavigateBack -> {
                    onBackClick()
                }

                RoomWaitingUiSideEffect.ShowShareSheet -> {
                    onShareClick()
                }

                RoomWaitingUiSideEffect.NavigateToRoom -> {
                    onOpenClick()
                }
            }
        }
    }

    RoomWaitingScreen(
        uiState = uiState,
        onBackClick = {
            viewModel.onIntent(RoomWaitingUiIntent.ClickBack)
        },
        onShareClick = {
            viewModel.onIntent(RoomWaitingUiIntent.ClickShare)
        },
        onOpenClick = {
            viewModel.onIntent(RoomWaitingUiIntent.ClickOpen)
        },
    )
}
