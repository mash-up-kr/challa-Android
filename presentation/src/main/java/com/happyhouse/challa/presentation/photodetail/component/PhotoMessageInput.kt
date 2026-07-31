package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaInputBox
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonSize
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonVariant
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaIconButton
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewLabel
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

/**
 * 사진에 메시지를 보내는 입력 바.
 *
 * 전송 버튼은 보낼 수 있을 때만 노출한다(ref. 인스타그램).
 * 전송 후 키보드를 유지해야 해서 IME 액션으로는 보내지 않고 전송 버튼으로만 보낸다.
 */
@Composable
fun PhotoMessageInput(
    message: String,
    isSendable: Boolean,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChallaInputBox(
        modifier = modifier,
        value = message,
        onValueChange = onMessageChange,
        placeholder = stringResource(R.string.photo_detail_message_placeholder),
        trailing =
            if (isSendable) {
                {
                    ChallaIconButton(
                        icon = ChallaIcons.Up,
                        onClick = onSendClick,
                        contentDescription = stringResource(R.string.photo_detail_message_send_description),
                        variant = ChallaButtonVariant.PRIMARY,
                        size = ChallaButtonSize.SMALL,
                    )
                }
            } else {
                null
            },
    )
}

@ComposePreview(showBackground = true, widthDp = 390)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoMessageInputPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ChallaPreviewLabel(text = "비어 있음 - 전송 버튼 없음")
        PhotoMessageInput(
            modifier = Modifier.fillMaxWidth(),
            message = "",
            isSendable = false,
            onMessageChange = {},
            onSendClick = {},
        )

        ChallaPreviewLabel(text = "입력됨 - 전송 버튼 노출")
        PhotoMessageInput(
            modifier = Modifier.fillMaxWidth(),
            message = "기엽다",
            isSendable = true,
            onMessageChange = {},
            onSendClick = {},
        )

        ChallaPreviewLabel(text = "전송 중 - 전송 버튼 없음")
        PhotoMessageInput(
            modifier = Modifier.fillMaxWidth(),
            message = "기엽다",
            isSendable = false,
            onMessageChange = {},
            onSendClick = {},
        )
    }
}

@ComposePreview(showBackground = true, widthDp = 390, name = "PhotoMessageInput - 입력 동작")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoMessageInputInteractivePreview() {
    var message by remember { mutableStateOf("") }

    PhotoMessageInput(
        modifier = Modifier.fillMaxWidth(),
        message = message,
        isSendable = message.isNotBlank(),
        onMessageChange = { message = it },
        onSendClick = { message = "" },
    )
}
