package com.happyhouse.challa.presentation.roomcover.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import com.happyhouse.challa.presentation.roomcover.contract.RoomCoverColorUiModel
import com.happyhouse.challa.presentation.roomcover.previewCoverColors
import kotlinx.collections.immutable.ImmutableList

private val SwatchSize = 36.dp

/** 스티커에 입힐 색을 고르는 가로 팔레트. */
@Composable
fun RoomCoverColorPicker(
    colors: ImmutableList<RoomCoverColorUiModel>,
    selectedColorId: Long?,
    onColorClick: (RoomCoverColorUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        colors.forEach { color ->
            ColorSwatch(
                color = color.color,
                selected = color.id == selectedColorId,
                onClick = { onColorClick(color) },
            )
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(SwatchSize)
                .semantics { this.selected = selected }
                .noRippleClickOnce(role = Role.RadioButton, onClick = onClick)
                .background(color = color, shape = CircleShape)
                // 고른 색은 테두리로 표시한다. 색이 밝아도 보이도록 안쪽에 그린다.
                .border(
                    width = 2.dp,
                    color = if (selected) ChallaTheme.colors.labelStrong else Color.Transparent,
                    shape = CircleShape,
                ),
    )
}

@Preview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomCoverColorPickerPreview() {
    RoomCoverColorPicker(
        colors = previewCoverColors(),
        selectedColorId = 1L,
        onColorClick = {},
    )
}
