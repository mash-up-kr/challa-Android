package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.transformations
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.CHALLA_PREVIEW_BACKGROUND
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.util.BlurTransformation
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private const val CARD_ASPECT_RATIO = 3f / 4f

private val CardShape = RoundedCornerShape(10.dp)

// TODO: 디자인 토큰 추가되면 교체
private val CardEmptyColor = Color.White.copy(alpha = 0.05f)

/** 디자인 시스템 Card Item 타입과 1:1 대응한다. (`더보기`는 갤러리에서 쓰지 않아 제외) */
@Immutable
sealed interface GalleryCardType {
    /** 촬영 전 */
    data object NotCaptured : GalleryCardType

    /** 인화 대기 */
    data class PrintWaiting(
        val imageUrl: String,
    ) : GalleryCardType

    /** 인화 완료 */
    data class Printed(
        val imageUrl: String,
    ) : GalleryCardType
}

/**
 * @param onClick 인화 전 카드는 열 사진이 없어 null이다.
 */
@Composable
fun GalleryCardItem(
    order: Int,
    type: GalleryCardType,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val cardDescription =
        when (type) {
            GalleryCardType.NotCaptured,
            is GalleryCardType.PrintWaiting,
            -> stringResource(R.string.gallery_film_slot_description, order)

            is GalleryCardType.Printed -> stringResource(R.string.gallery_photo_content_description, order)
        }
    val openPhotoLabel = stringResource(R.string.gallery_open_photo)

    Box(
        modifier =
            modifier
                // 번호 텍스트만 따로 읽히지 않도록 카드 전체를 한 덩어리로 읽힌다.
                .semantics(mergeDescendants = true) { contentDescription = cardDescription }
                .aspectRatio(CARD_ASPECT_RATIO)
                .clip(CardShape)
                .background(CardEmptyColor)
                .then(
                    // 인화 완료 카드는 사진이 꽉 차서 테두리가 필요 없다.
                    if (type is GalleryCardType.Printed) {
                        Modifier
                    } else {
                        Modifier.border(
                            width = 1.dp,
                            color = ChallaTheme.colors.lineNeutral,
                            shape = CardShape,
                        )
                    },
                ).then(
                    if (onClick == null) {
                        Modifier
                    } else {
                        Modifier.clickable(
                            role = Role.Image,
                            onClickLabel = openPhotoLabel,
                            onClick = onClick,
                        )
                    },
                ),
    ) {
        when (type) {
            GalleryCardType.NotCaptured -> Unit

            is GalleryCardType.PrintWaiting -> {
                CardImage(
                    imageUrl = type.imageUrl,
                    // TODO: 서버가 블러 이미지를 내려주면 이중 블러가 되므로 제거할 것
                    blurred = true,
                )

                // 한 겹 더 덮어 번호만 또렷하게 보이게 한다.
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(CardEmptyColor),
                )
            }

            is GalleryCardType.Printed -> CardImage(imageUrl = type.imageUrl, blurred = false)
        }

        Text(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, bottom = 10.dp),
            text = order.toString(),
            color =
                if (type is GalleryCardType.Printed) {
                    ChallaTheme.colors.staticWhite
                } else {
                    ChallaTheme.colors.labelSubtle
                },
            style = ChallaTheme.typography.bodyLarge.bold,
        )
    }
}

@Composable
private fun CardImage(
    imageUrl: String,
    blurred: Boolean,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        modifier = modifier.fillMaxSize(),
        model =
            ImageRequest
                .Builder(LocalContext.current)
                .data(imageUrl)
                .apply { if (blurred) transformations(BlurTransformation()) }
                .crossfade(true)
                .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}

@ComposePreview(showBackground = true, backgroundColor = CHALLA_PREVIEW_BACKGROUND, name = "CardItem - 촬영 전")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryCardItemNotCapturedPreview() {
    GalleryCardItem(
        modifier = Modifier.width(82.dp),
        order = 1,
        type = GalleryCardType.NotCaptured,
    )
}

@ComposePreview(showBackground = true, backgroundColor = CHALLA_PREVIEW_BACKGROUND, name = "CardItem - 인화 대기")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryCardItemPrintWaitingPreview() {
    GalleryCardItem(
        modifier = Modifier.width(82.dp),
        order = 2,
        type = GalleryCardType.PrintWaiting(imageUrl = ""),
    )
}

@ComposePreview(showBackground = true, backgroundColor = CHALLA_PREVIEW_BACKGROUND, name = "CardItem - 인화 완료")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryCardItemPrintedPreview() {
    GalleryCardItem(
        modifier = Modifier.width(82.dp),
        order = 3,
        type = GalleryCardType.Printed(imageUrl = ""),
        onClick = {},
    )
}
