package com.happyhouse.challa.presentation.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

enum class ChallaInputBoxState {
    PLACEHOLDER,
    FOCUSED,
    TYPED,
}

@Composable
fun ChallaInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    state: ChallaInputBoxState? = null,
    placeholder: String = "",
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val shape = RoundedCornerShape(12.dp)
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val inputBoxState = resolveChallaInputBoxState(state, value, isFocused)
    val inputBoxKeyboardActions =
        KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                keyboardActions.onDone?.invoke(this)
            },
        )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
            modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(shape)
                .background(ChallaTheme.colors.backgroundLevel2)
                .border((1.5).dp, inputBoxState.borderColor, shape)
                .padding(horizontal = 16.dp),
        enabled = enabled,
        singleLine = true,
        textStyle =
            ChallaTheme.typography.bodyMedium.copy(
                color = ChallaTheme.colors.labelNormal,
                textAlign = TextAlign.Center,
            ),
        cursorBrush = SolidColor(ChallaTheme.colors.primaryYellow),
        keyboardOptions = keyboardOptions,
        keyboardActions = inputBoxKeyboardActions,
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        color = ChallaTheme.colors.labelAlternative,
                        textAlign = TextAlign.Center,
                        style = ChallaTheme.typography.bodyMedium,
                    )
                }
                innerTextField()
            }
        },
    )
}

private fun resolveChallaInputBoxState(
    state: ChallaInputBoxState?,
    value: String,
    isFocused: Boolean,
): ChallaInputBoxState =
    state
        ?: when {
            isFocused -> ChallaInputBoxState.FOCUSED
            value.isNotEmpty() -> ChallaInputBoxState.TYPED
            else -> ChallaInputBoxState.PLACEHOLDER
        }

private val ChallaInputBoxState.borderColor: Color
    @Composable
    get() =
        if (this == ChallaInputBoxState.FOCUSED) {
            ChallaTheme.colors.primaryYellow
        } else {
            Color.Transparent
        }

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaInputBoxStatePreview() {
    ChallaTheme {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            ChallaInputBox(
                value = "",
                onValueChange = {},
                placeholder = "내용을 입력해 주세요.",
                state = ChallaInputBoxState.PLACEHOLDER,
            )
            ChallaInputBox(
                value = "",
                onValueChange = {},
                placeholder = "내용을 입력해 주세요.",
                state = ChallaInputBoxState.FOCUSED,
            )
            ChallaInputBox(
                value = "텍스트",
                onValueChange = {},
                state = ChallaInputBoxState.TYPED,
            )
        }
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
                state = ChallaInputBoxState.PLACEHOLDER,
                placeholder = "내용을 입력해 주세요.",
            )
        }
    }
}
