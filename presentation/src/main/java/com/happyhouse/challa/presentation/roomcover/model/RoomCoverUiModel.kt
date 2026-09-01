package com.happyhouse.challa.presentation.roomcover.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.happyhouse.challa.domain.model.RoomCover
import timber.log.Timber

/**
 * 화면에 그릴 방 커버.
 *
 * @param imageUrl 배경 이미지. null이면 검정으로 그린다.
 * @param sticker 배경 위에 올릴 스티커. 없거나 색을 해석하지 못했으면 null이다.
 */
@Immutable
data class RoomCoverUiModel(
    val imageUrl: String? = null,
    val sticker: Sticker? = null,
) {
    @Immutable
    data class Sticker(
        val imageUrl: String,
        val color: Color,
    )
}

fun RoomCover.toCoverUiModel(): RoomCoverUiModel =
    RoomCoverUiModel(
        imageUrl = imageUrl,
        sticker =
            sticker?.let { sticker ->
                sticker.color.hex.toColorOrNull()?.let { color ->
                    RoomCoverUiModel.Sticker(imageUrl = sticker.imageUrl, color = color)
                }
            },
    )

/** 서버가 준 `#RRGGBB` 문자열을 색으로 바꾼다. 형식이 깨졌으면 null. */
fun String.toColorOrNull(): Color? =
    runCatching { Color(toColorInt()) }
        .onFailure { Timber.w(it, "커버 색상을 해석하지 못했습니다. hex=$this") }
        .getOrNull()
