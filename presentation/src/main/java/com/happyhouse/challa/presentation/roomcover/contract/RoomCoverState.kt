package com.happyhouse.challa.presentation.roomcover.contract

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.happyhouse.challa.domain.model.RoomCoverColor
import com.happyhouse.challa.domain.model.RoomCoverStickerOption
import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.roomcover.model.RoomCoverUiModel
import com.happyhouse.challa.presentation.roomcover.model.toColorOrNull
import kotlinx.collections.immutable.ImmutableList

/**
 * @param roomName 미리보기 카드에 그릴 방 이름.
 */
@Immutable
data class RoomCoverState(
    val roomName: String = "",
    val content: Content = Content.Loading,
) : UiState {
    @Immutable
    sealed interface Content {
        /** 방 정보·참여자·커버 옵션을 받아오는 중 */
        data object Loading : Content

        /** 셋 중 하나라도 받아오지 못했다. 다시 시도할 수 있게 안내한다. */
        data object Error : Content

        /**
         * @param selectedColorId 고른 색. 스티커가 없어도 팔레트 선택은 남는다.
         * @param backgroundImageUrl 배경 이미지. 업로드가 끝나기 전에는 고른 사진의 로컬 URI다.
         */
        @Immutable
        data class Ready(
            val memberCount: Int,
            val colors: ImmutableList<RoomCoverColorUiModel>,
            val stickers: ImmutableList<RoomCoverStickerUiModel>,
            val selectedColorId: Long?,
            val selectedStickerId: Long?,
            val backgroundImageUrl: String?,
        ) : Content {
            val selectedColor: Color?
                get() = colors.find { it.id == selectedColorId }?.color

            /** 미리보기 카드에 그릴 커버 */
            val cover: RoomCoverUiModel
                get() {
                    val sticker = stickers.find { it.id == selectedStickerId }
                    val color = selectedColor
                    return RoomCoverUiModel(
                        imageUrl = backgroundImageUrl,
                        sticker =
                            if (sticker != null && color != null) {
                                RoomCoverUiModel.Sticker(imageUrl = sticker.imageUrl, color = color)
                            } else {
                                null
                            },
                    )
                }
        }
    }
}

@Immutable
data class RoomCoverColorUiModel(
    val id: Long,
    val color: Color,
)

@Immutable
data class RoomCoverStickerUiModel(
    val id: Long,
    val imageUrl: String,
)

/** 색을 해석하지 못한 항목은 팔레트에서 뺀다. 그릴 수 없는 색을 고르게 둘 수는 없다. */
fun RoomCoverColor.toUiModelOrNull(): RoomCoverColorUiModel? = hex.toColorOrNull()?.let { RoomCoverColorUiModel(id = id, color = it) }

fun RoomCoverStickerOption.toUiModel(): RoomCoverStickerUiModel = RoomCoverStickerUiModel(id = id, imageUrl = imageUrl)
