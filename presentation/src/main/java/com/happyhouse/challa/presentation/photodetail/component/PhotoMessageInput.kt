package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonSize
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonVariant
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaIconButton
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewLabel
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

// TODO: 피그마 수치 확인 전까지 쓰는 임시 값. 디자인 확정되면 맞출 것. (이슈 #62)
private val InputShape = RoundedCornerShape(12.dp)
private val InputBorderWidth = 1.5.dp
private val InputHeight = 52.dp
private val InputTextStartPadding = 16.dp
private val InputTextVerticalPadding = 16.dp
private val SendButtonEndPadding = 10.dp

/**
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
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    PhotoMessageInputContent(
        modifier = modifier,
        message = message,
        isSendable = isSendable,
        isFocused = isFocused,
        onMessageChange = onMessageChange,
        onSendClick = onSendClick,
        interactionSource = interactionSource,
    )
}

@Composable
private fun PhotoMessageInputContent(
    message: String,
    isSendable: Boolean,
    isFocused: Boolean,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    BasicTextField(
        value = message,
        onValueChange = onMessageChange,
        modifier =
            modifier
                .fillMaxWidth()
                .height(InputHeight)
                .clip(InputShape)
                .background(ChallaTheme.colors.backgroundLevel2)
                .border(
                    width = InputBorderWidth,
                    color = if (isFocused) ChallaTheme.colors.primary else Color.Transparent,
                    shape = InputShape,
                ),
        singleLine = true,
        textStyle =
            ChallaTheme.typography.bodyMedium.medium.copy(
                color = ChallaTheme.colors.labelNormal,
            ),
        cursorBrush = SolidColor(ChallaTheme.colors.primary),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(end = SendButtonEndPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(
                                start = InputTextStartPadding,
                                top = InputTextVerticalPadding,
                                bottom = InputTextVerticalPadding,
                            ),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (message.isEmpty()) {
                        Text(
                            text = stringResource(R.string.photo_detail_message_placeholder),
                            color = ChallaTheme.colors.labelAlternative,
                            style = ChallaTheme.typography.bodyMedium.medium,
                        )
                    }
                    innerTextField()
                }

                if (isSendable) {
                    ChallaIconButton(
                        icon = ChallaIcons.Up,
                        onClick = onSendClick,
                        contentDescription = stringResource(R.string.photo_detail_message_send_description),
                        variant = ChallaButtonVariant.PRIMARY,
                        size = ChallaButtonSize.SMALL,
                    )
                }
            }
        },
    )
}

@ComposePreview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoMessageInputPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ChallaPreviewLabel(text = "비어 있음 / 포커스 없음")
        PhotoMessageInputContent(
            message = "",
            isSendable = false,
            isFocused = false,
            onMessageChange = {},
            onSendClick = {},
        )

        ChallaPreviewLabel(text = "비어 있음 / 포커스")
        PhotoMessageInputContent(
            message = "",
            isSendable = false,
            isFocused = true,
            onMessageChange = {},
            onSendClick = {},
        )

        ChallaPreviewLabel(text = "입력됨 / 포커스 - 전송 버튼 노출")
        PhotoMessageInputContent(
            message = "기엽다",
            isSendable = true,
            isFocused = true,
            onMessageChange = {},
            onSendClick = {},
        )

        ChallaPreviewLabel(text = "전송 중 - 전송 버튼 없음")
        PhotoMessageInputContent(
            message = "기엽다",
            isSendable = false,
            isFocused = true,
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
        message = message,
        isSendable = message.isNotBlank(),
        onMessageChange = { message = it },
        onSendClick = { message = "" },
    )
}
