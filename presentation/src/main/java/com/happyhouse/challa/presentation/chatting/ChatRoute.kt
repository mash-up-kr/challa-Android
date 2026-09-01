package com.happyhouse.challa.presentation.chatting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.happyhouse.challa.presentation.chatting.contract.ChatIntent
import com.happyhouse.challa.presentation.chatting.contract.ChatState

@Composable
fun ChatRoute(
    roomName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var message by rememberSaveable { mutableStateOf("") }

    ChatScreen(
        state =
            ChatState(
                roomName = roomName,
                message = message,
            ),
        onIntent = { intent ->
            when (intent) {
                is ChatIntent.MessageChange -> message = intent.message
            }
        },
        onBackClick = onBackClick,
        modifier = modifier,
    )
}
