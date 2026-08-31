package com.happyhouse.challa.presentation.reaction

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.presentation.R

@Composable
internal fun ReactionEmojiSticker(
    emoji: ReactionEmoji,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Image(
        modifier = modifier,
        painter = painterResource(id = emoji.stickerDrawableRes),
        contentDescription = contentDescription,
    )
}

/** 흰 테두리가 이미지에 포함된 사진 반응 스티커 */
private val ReactionEmoji.stickerDrawableRes: Int
    @DrawableRes
    get() =
        when (this) {
            ReactionEmoji.FIRE -> R.drawable.img_reaction_sticker_fire
            ReactionEmoji.EYES -> R.drawable.img_reaction_sticker_eyes
            ReactionEmoji.MEDAL -> R.drawable.img_reaction_sticker_medal
            ReactionEmoji.QUESTION -> R.drawable.img_reaction_sticker_question
            ReactionEmoji.THINKING -> R.drawable.img_reaction_sticker_thinking
            ReactionEmoji.HEART -> R.drawable.img_reaction_sticker_heart
            ReactionEmoji.THUMBS_UP -> R.drawable.img_reaction_sticker_thumbs_up
            ReactionEmoji.SPARKLES -> R.drawable.img_reaction_sticker_sparkles
            ReactionEmoji.POOP -> R.drawable.img_reaction_sticker_poop
            ReactionEmoji.SKULL -> R.drawable.img_reaction_sticker_skull
        }

internal val ReactionEmoji.labelRes: Int
    @StringRes
    get() =
        when (this) {
            ReactionEmoji.FIRE -> R.string.photo_detail_reaction_fire
            ReactionEmoji.EYES -> R.string.photo_detail_reaction_eyes
            ReactionEmoji.MEDAL -> R.string.photo_detail_reaction_medal
            ReactionEmoji.QUESTION -> R.string.photo_detail_reaction_question
            ReactionEmoji.THINKING -> R.string.photo_detail_reaction_thinking
            ReactionEmoji.HEART -> R.string.photo_detail_reaction_heart
            ReactionEmoji.THUMBS_UP -> R.string.photo_detail_reaction_thumbs_up
            ReactionEmoji.SPARKLES -> R.string.photo_detail_reaction_sparkles
            ReactionEmoji.POOP -> R.string.photo_detail_reaction_poop
            ReactionEmoji.SKULL -> R.string.photo_detail_reaction_skull
        }
