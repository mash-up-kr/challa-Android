package com.happyhouse.challa.presentation.chatting.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaInputBox
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper

@Composable
fun ChatInputArea(
    message: String,
    showsFirstMessageTooltip: Boolean,
    onMessageChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showsFirstMessageTooltip) {
            ChatFirstMessageTooltip()
            Spacer(modifier = Modifier.size(6.dp))
        }

        ChallaInputBox(
            value = message,
            onValueChange = onMessageChange,
            placeholder = stringResource(R.string.chat_message_placeholder),
        )
    }
}

@Preview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChatInputAreaPreview() {
    ChatInputArea(
        message = "",
        showsFirstMessageTooltip = true,
        onMessageChange = {},
    )
}
