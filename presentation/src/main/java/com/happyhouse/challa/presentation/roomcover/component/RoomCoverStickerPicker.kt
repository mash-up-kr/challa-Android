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
import com.happyhouse.challa.presentation.roomcover.previewCoverStickers
import kotlinx.collections.immutable.ImmutableList

private val StickerWidth = 100.dp
private val StickerHeight = 134.dp
private val StickerShape = RoundedCornerShape(12.dp)

/**
 * 스티커를 고르는 가로 목록. 항목마다 고른 색을 입혀 보여준다.
 *
 * @param stickerColor 스티커에 입힐 색. 팔레트에서 고른 색을 그대로 받는다.
 */
@Composable
fun RoomCoverStickerPicker(
    stickers: ImmutableList<RoomCoverStickerUiModel>,
    selectedSticker: RoomCoverStickerUiModel?,
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
                selected = sticker == selectedSticker,
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
                    width = 2.dp,
                    color = if (selected) ChallaTheme.colors.staticWhite else ChallaTheme.colors.backgroundLevel4,
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
        stickers = previewCoverStickers(count = 3),
        selectedSticker = previewCoverStickers().first(),
        stickerColor = ChallaTheme.colors.primaryYellow,
        onStickerClick = {},
    )
}
