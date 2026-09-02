package com.happyhouse.challa.presentation.roomcover.component

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.happyhouse.challa.presentation.roomcover.contract.RoomCoverStickerUiModel
import com.happyhouse.challa.presentation.roomcover.model.RoomCoverUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private val StickerWidth = 96.dp
private val StickerHeight = 128.dp
private val StickerShape = RoundedCornerShape(8.dp)

/**
 * 스티커를 고르는 가로 목록. 항목마다 고른 색을 입혀 보여준다.
 *
 * @param stickerColor 스티커에 입힐 색. 팔레트에서 고른 색을 그대로 받는다.
 */
@Composable
fun RoomCoverStickerPicker(
    stickers: ImmutableList<RoomCoverStickerUiModel>,
    selectedStickerId: Long?,
    stickerColor: Color?,
    onStickerClick: (RoomCoverStickerUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        stickers.forEach { sticker ->
            StickerItem(
                sticker = sticker,
                stickerColor = stickerColor,
                selected = sticker.id == selectedStickerId,
                onClick = { onStickerClick(sticker) },
            )
        }
    }
}

@Composable
private fun StickerItem(
    sticker: RoomCoverStickerUiModel,
    stickerColor: Color?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .width(StickerWidth)
                .height(StickerHeight)
                .clip(StickerShape)
                .semantics { this.selected = selected }
                .noRippleClickOnce(role = Role.RadioButton, onClick = onClick)
                .border(
                    width = 1.5.dp,
                    color = if (selected) ChallaTheme.colors.labelStrong else Color.Transparent,
                    shape = StickerShape,
                ),
    ) {
        RoomCoverBackground(
            cover =
                RoomCoverUiModel(
                    sticker =
                        stickerColor?.let {
                            RoomCoverUiModel.Sticker(imageUrl = sticker.imageUrl, color = it)
                        },
                ),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomCoverStickerPickerPreview() {
    RoomCoverStickerPicker(
        stickers = previewStickers(),
        selectedStickerId = 1L,
        stickerColor = Color(0xFFD5F700),
        onStickerClick = {},
    )
}

private fun previewStickers(): ImmutableList<RoomCoverStickerUiModel> =
    persistentListOf(
        RoomCoverStickerUiModel(id = 1L, imageUrl = "https://challa.example/sticker-1.png"),
        RoomCoverStickerUiModel(id = 2L, imageUrl = "https://challa.example/sticker-2.png"),
        RoomCoverStickerUiModel(id = 3L, imageUrl = "https://challa.example/sticker-3.png"),
    )
