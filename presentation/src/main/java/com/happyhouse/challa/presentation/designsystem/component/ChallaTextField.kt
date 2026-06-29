package com.happyhouse.challa.presentation.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

@Composable
fun ChallaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    supportingText: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colorSpec = challaTextFieldColorSpec(enabled = enabled, isError = isError)
    val shape = RoundedCornerShape(12.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        label?.let {
            Text(
                text = it,
                modifier = Modifier.fillMaxWidth(),
                color = ChallaTheme.colors.labelNormal,
                textAlign = TextAlign.Center,
                style = ChallaTheme.typography.headingSmall,
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        BasicTextField(
            value = value,
            onValueChange = { newValue -> onValueChange(newValue) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(shape = shape)
                    .background(ChallaTheme.colors.backgroundLevel2)
                    .border(
                        width = 1.dp,
                        color = colorSpec.borderColor,
                        shape = shape,
                    ).padding(all = 16.dp),
            enabled = enabled,
            textStyle =
                ChallaTheme.typography.bodyMedium.copy(
                    color = ChallaTheme.colors.labelNormal,
                    textAlign = TextAlign.Center,
                ),
            cursorBrush = SolidColor(ChallaTheme.colors.primaryYellow),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            modifier = Modifier.fillMaxWidth(),
                            color = ChallaTheme.colors.labelAlternative,
                            textAlign = TextAlign.Center,
                            style = ChallaTheme.typography.bodyMedium,
                        )
                    }
                    innerTextField()
                }
            },
        )

        supportingText?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = it,
                modifier = Modifier.fillMaxWidth(),
                color = colorSpec.supportingTextColor,
                textAlign = TextAlign.Center,
                style = ChallaTheme.typography.bodySmall,
            )
        }
    }
}

private data class ChallaTextFieldColorSpec(
    val borderColor: Color,
    val textColor: Color,
    val supportingTextColor: Color,
)

@Composable
private fun challaTextFieldColorSpec(
    enabled: Boolean,
    isError: Boolean,
): ChallaTextFieldColorSpec =
    ChallaTextFieldColorSpec(
        borderColor =
            when {
                isError -> ChallaTheme.colors.primaryOrange
                else -> ChallaTheme.colors.lineNormal
            },
        textColor =
            if (enabled) {
                ChallaTheme.colors.labelNormal
            } else {
                ChallaTheme.colors.labelDisable
            },
        supportingTextColor =
            if (isError) {
                ChallaTheme.colors.primaryOrange
            } else {
                ChallaTheme.colors.labelAlternative
            },
    )

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaTextFieldPreview() {
    Column {
        ChallaTextField(
            value = "",
            onValueChange = {},
            label = "방 이름",
            placeholder = "방 이름을 입력해주세요",
            supportingText = "0/10",
        )
        Spacer(modifier = Modifier.height(20.dp))
        ChallaTextField(
            value = "해피하우스",
            onValueChange = {},
            label = "방 이름",
            supportingText = "5/10",
        )
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaTextFieldErrorPreview() {
    Column {
        ChallaTextField(
            value = "해피하우스",
            onValueChange = {},
            label = "방 이름",
            supportingText = "이미 사용 중인 이름이에요",
            isError = true,
        )
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaTextFieldInteractivePreview() {
    var text by remember { mutableStateOf("") }

    Column {
        ChallaTextField(
            value = text,
            onValueChange = { text = it },
            label = "방 이름",
            placeholder = "방 이름을 입력해주세요",
            supportingText = "${text.length}/20",
        )
    }
}
