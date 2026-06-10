package com.happyhouse.challa.presentation.room.waiting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.room.waiting.contract.RoomWaitingUiIntent
import com.happyhouse.challa.presentation.room.waiting.contract.RoomWaitingUiSideEffect
import com.happyhouse.challa.presentation.room.waiting.model.ShareLinkUiModel

@Composable
fun RoomWaitingRoute(
    roomId: Long,
    opensAtMillis: Long,
    onBackClick: () -> Unit,
    onShareClick: (ShareLinkUiModel) -> Unit,
    onOpenClick: (Long) -> Unit,
    viewModel: RoomWaitingViewModel = hiltViewModel(),
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
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is RoomWaitingUiSideEffect.ShareLink -> {
                    onShareClick(
                        ShareLinkUiModel(
                            link = effect.link,
                            title = effect.title,
                            description = effect.description,
                        ),
                    )
                }

                is RoomWaitingUiSideEffect.NavigateToRoom -> {
                    onOpenClick(effect.roomId)
                }
            }
        }
    }

    RoomWaitingScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onShareClick = {
            viewModel.onIntent(RoomWaitingUiIntent.ClickShare)
        },
        onOpenClick = {
            viewModel.onIntent(RoomWaitingUiIntent.ClickOpen)
        },
    )
}
