package com.happyhouse.challa.presentation.chatting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun ChatRoute(
    roomName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var message by rememberSaveable { mutableStateOf("") }

    ChatScreen(
        roomName = roomName,
        message = message,
        onMessageChange = { message = it },
        onBackClick = onBackClick,
        modifier = modifier,
    )
}
