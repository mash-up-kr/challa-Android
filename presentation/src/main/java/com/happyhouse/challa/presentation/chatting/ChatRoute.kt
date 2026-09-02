package com.happyhouse.challa.presentation.chatting

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.chatting.contract.ChatSideEffect
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaToastVisuals
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

@Composable
fun ChatRoute(
    roomId: Long,
    roomName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel =
        hiltViewModel<ChatViewModel, ChatViewModel.Factory>(
            creationCallback = { factory -> factory.create(roomId, roomName) },
        ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val messageSendFailureMessage = stringResource(R.string.chat_message_send_failure)
    val destructiveTint = ChallaTheme.colors.statusDestructive

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                ChatSideEffect.MessageSendFailed ->
                    snackbarHostState.showSnackbar(
                        ChallaToastVisuals(
                            message = messageSendFailureMessage,
                            icon = ChallaIcons.Error,
                            iconTint = destructiveTint,
                        ),
                    )
            }
        }
    }

    LifecycleStartEffect(viewModel) {
        viewModel.startChatSession()

        onStopOrDispose {
            viewModel.pauseChatSession()
        }
    }

    ChatScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}
