package com.happyhouse.challa.presentation.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonSize
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonVariant
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaIconButton
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

private val TrailingSpacing = 8.dp

/**
 * @param trailing 입력 영역 오른쪽에 붙는 슬롯. 전송 버튼처럼 입력과 함께 쓰는 액션에 사용한다.
 *   null이면 입력 영역이 가로를 모두 차지한다.
 */
@Composable
fun ChallaInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
    onDone: KeyboardActionScope.() -> Unit = {},
    trailing: (@Composable () -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val inputBoxKeyboardActions =
        KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onDone()
            },
        )

    ChallaInputBoxContent(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        isFocused = isFocused,
        modifier = modifier,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        keyboardActions = inputBoxKeyboardActions,
        interactionSource = interactionSource,
        trailing = trailing,
    )
}

@Composable
private fun ChallaInputBoxContent(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    trailing: (@Composable () -> Unit)? = null,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
            modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(12.dp))
                .background(ChallaTheme.colors.backgroundLevel2)
                .border(
                    width = (1.5).dp,
                    color = inputBoxBorderColor(isFocused),
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(16.dp),
        enabled = enabled,
        singleLine = true,
        textStyle =
            ChallaTheme.typography.bodyMedium.bold.copy(
                color = ChallaTheme.colors.labelNormal,
            ),
        cursorBrush = SolidColor(ChallaTheme.colors.primaryYellow),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TrailingSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = ChallaTheme.colors.labelAlternative,
                            style = ChallaTheme.typography.bodyMedium.bold,
                        )
                    }
                    innerTextField()
                }

                trailing?.invoke()
            }
        },
    )
}

@Composable
private fun inputBoxBorderColor(isFocused: Boolean): Color =
    if (isFocused) {
        ChallaTheme.colors.primaryYellow
    } else {
        Color.Transparent
    }

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaInputBoxPreview() {
    ChallaTheme {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            ChallaInputBoxPreviewItem(
                label = "Empty / Unfocused",
                value = "",
                placeholder = "내용을 입력해 주세요.",
                isFocused = false,
            )
            ChallaInputBoxPreviewItem(
                label = "Empty / Focused",
                value = "",
                placeholder = "내용을 입력해 주세요.",
                isFocused = true,
            )
            ChallaInputBoxPreviewItem(
                label = "Has value / Unfocused",
                value = "텍스트",
                isFocused = false,
            )
            ChallaInputBoxPreviewItem(
                label = "Has value / Focused",
                value = "텍스트",
                isFocused = true,
            )
            ChallaInputBoxPreviewItem(
                label = "Has value / Focused / Trailing",
                value = "텍스트",
                isFocused = true,
                trailing = {
                    ChallaIconButton(
                        icon = ChallaIcons.Up,
                        onClick = {},
                        variant = ChallaButtonVariant.PRIMARY,
                        size = ChallaButtonSize.SMALL,
                    )
                },
            )
        }
    }
}

@Composable
private fun ChallaInputBoxPreviewItem(
    label: String,
    value: String,
    isFocused: Boolean,
    placeholder: String = "",
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            color = ChallaTheme.colors.labelNormal,
            style = ChallaTheme.typography.bodyMedium.bold,
        )
        ChallaInputBoxContent(
            value = value,
            onValueChange = {},
            isFocused = isFocused,
            placeholder = placeholder,
            trailing = trailing,
        )
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaInputBoxInteractivePreview() {
    ChallaTheme {
        var text by remember { mutableStateOf("") }

        Column {
            ChallaInputBox(
                value = text,
                onValueChange = { text = it },
                placeholder = "내용을 입력해 주세요.",
            )
        }
    }
}
