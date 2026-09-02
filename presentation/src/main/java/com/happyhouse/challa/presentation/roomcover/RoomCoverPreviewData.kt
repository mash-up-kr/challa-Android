package com.happyhouse.challa.presentation.roomcover

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.roomcover.contract.RoomCoverColorUiModel
import com.happyhouse.challa.presentation.roomcover.contract.RoomCoverStickerUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

/**
 * @Preview 전용 mock 색상 목록.
 *
 * 실제 팔레트는 서버(`GET /rooms/cover-options`)가 내려주고 앱은 hex를 받아 쓴다.
 * 프리뷰에서는 값을 지어내지 않도록 디자인 시스템 팔레트를 빌려 쓴다.
 */
@Composable
internal fun previewCoverColors(): ImmutableList<RoomCoverColorUiModel> =
    with(ChallaTheme.colors) {
        listOf(
            primaryYellow,
            primaryPink,
            primaryOrange,
            primarySky,
            primaryBlue,
            primaryPurple,
        )
    }.mapIndexed { index, color ->
        RoomCoverColorUiModel(
            id = index + 1L,
            hex = "#%06X".format(color.toArgb() and 0xFFFFFF),
            color = color,
        )
    }.toPersistentList()

/** @Preview 전용 mock 스티커 목록. 프리뷰는 원격 이미지를 그리지 못해 모양만 확인한다. */
internal fun previewCoverStickers(count: Int = 4): ImmutableList<RoomCoverStickerUiModel> =
    (1..count)
        .map { id ->
            RoomCoverStickerUiModel(id = id.toLong(), imageUrl = "$PREVIEW_STICKER_BASE_URL/$id.svg")
        }.toPersistentList()

// 프리뷰는 원격 이미지를 그리지 못한다. 실제로 연결되지 않는 예약 도메인(RFC 2606)을 쓴다.
private const val PREVIEW_STICKER_BASE_URL = "https://challa.example.com/cover-stickers"

internal const val PREVIEW_COVER_IMAGE_URL = "https://challa.example.com/cover.jpg"
internal const val PREVIEW_STICKER_IMAGE_URL = "$PREVIEW_STICKER_BASE_URL/1.svg"
