package com.happyhouse.challa.presentation.designsystem.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.transformations
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.dashedRoundedBorder
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import com.happyhouse.challa.presentation.util.BlurTransformation
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private const val CARD_ASPECT_RATIO = 3f / 4f

private val CardCornerRadius = 10.dp
private val CardShape = RoundedCornerShape(CardCornerRadius)
private val CardBorderWidth = 1.dp
private val CardDashLength = 4.dp

@Immutable
sealed interface ChallaCardType {
    /** 촬영 전 */
    data object NotCaptured : ChallaCardType

    /**
     * 인화 대기. 서버가 내려준 원본을 앱에서 블러 처리해 그린다.
     *
     * @param imageUrl 아직 이미지를 받지 못했으면 null. 그려줄 이미지는 없지만
     *  실선 테두리로 [NotCaptured] 와 구분된다.
     */
    data class PrintWaiting(
        val imageUrl: String?,
    ) : ChallaCardType

    /** 인화 완료 */
    data class Printed(
        val imageUrl: String,
    ) : ChallaCardType
}

enum class ChallaCardBorder {
    DEFAULT,
    DASHED,
    PRIMARY,
}

/**
 * 필름 그리드의 카드 1칸. 인화 상태에 따라 빈 칸 / 흐린 사진 / 공개된 사진을 그린다.
 *
 * @param contentDescription 번호와 이미지가 따로 읽히지 않도록 카드 전체를 대신 읽어줄 문구.
 *  카드는 항상 순서 번호를 그리므로 읽어줄 문구가 없는 경우가 없어 필수로 받는다.
 * @param onClick 넘기지 않으면 클릭 영역을 두지 않는다.
 * @param border DEFAULT는 [type]에 따른 테두리를 사용하고, 나머지 값은 이를 덮어쓴다.
 * 테두리 변경 시 사진은 유지하고 테두리만 1초 동안 교차 페이드한다.
 * @param onImageLoadFinished 이미지 로딩이 성공하거나 실패했을 때 호출한다.
 * 이미지가 없는 카드에는 호출하지 않으며, 이미지 요청이 다시 실행되면 재호출될 수 있다.
 */
@Composable
fun ChallaCardItem(
    order: Int,
    type: ChallaCardType,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClickLabel: String? = null,
    onClick: (() -> Unit)? = null,
    border: ChallaCardBorder = ChallaCardBorder.DEFAULT,
    onImageLoadFinished: () -> Unit = {},
) {
    Box(
        modifier =
            modifier
                // 번호가 따로 읽히지 않도록 카드 전체를 한 덩어리로 읽힌다.
                .semantics(mergeDescendants = true) {
                    this.contentDescription = contentDescription
                }
                .aspectRatio(CARD_ASPECT_RATIO)
                .clip(CardShape)
                .then(
                    if (onClick == null) {
                        Modifier
                    } else {
                        Modifier.noRippleClickOnce(
                            role = Role.Image,
                            onClickLabel = onClickLabel,
                            onClick = onClick,
                        )
                    },
                ),
    ) {
        when (type) {
            ChallaCardType.NotCaptured -> Unit

            is ChallaCardType.PrintWaiting -> {
                type.imageUrl?.let { imageUrl ->
                    CardImage(
                        imageUrl = imageUrl,
                        blurred = true,
                        onLoadFinished = onImageLoadFinished,
                    )
                }
            }

            is ChallaCardType.Printed ->
                CardImage(
                    imageUrl = type.imageUrl,
                    blurred = false,
                    onLoadFinished = onImageLoadFinished,
                )
        }

        Text(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, bottom = 10.dp),
            text = order.toString(),
            color =
                when (type) {
                    ChallaCardType.NotCaptured -> ChallaTheme.colors.labelDisable

                    is ChallaCardType.PrintWaiting,
                    is ChallaCardType.Printed,
                    -> ChallaTheme.colors.labelSubtle
                },
            style = ChallaTheme.typography.bodyLarge.bold,
        )

        CardBorder(
            border =
                if (border == ChallaCardBorder.DEFAULT && type == ChallaCardType.NotCaptured) {
                    ChallaCardBorder.DASHED
                } else {
                    border
                },
            modifier = Modifier.matchParentSize(),
        )
    }
}

@Composable
private fun CardBorder(
    border: ChallaCardBorder,
    modifier: Modifier = Modifier,
) {
    Crossfade(
        targetState = border,
        modifier = modifier,
        animationSpec = tween(durationMillis = 1000),
        label = "CardBorder",
    ) { displayedBorder ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .then(
                        when (displayedBorder) {
                            ChallaCardBorder.DASHED ->
                                Modifier.dashedRoundedBorder(
                                    color = ChallaTheme.colors.lineNormal,
                                    cornerRadius = CardCornerRadius,
                                    strokeWidth = CardBorderWidth,
                                    dashLength = CardDashLength,
                                    gapLength = CardDashLength,
                                )

                            ChallaCardBorder.PRIMARY ->
                                Modifier.border(2.dp, ChallaTheme.colors.primary, CardShape)

                            ChallaCardBorder.DEFAULT ->
                                Modifier.border(
                                    CardBorderWidth,
                                    ChallaTheme.colors.lineNeutral,
                                    CardShape,
                                )
                        },
                    ),
        )
    }
}

@Composable
private fun CardImage(
    imageUrl: String,
    blurred: Boolean,
    onLoadFinished: () -> Unit,
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
        onSuccess = { onLoadFinished() },
        onError = { onLoadFinished() },
    )
}

@ComposePreview(name = "CardItem - 촬영 전")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaCardItemNotCapturedPreview() {
    ChallaCardItem(
        modifier = Modifier.width(82.dp),
        order = 1,
        type = ChallaCardType.NotCaptured,
        contentDescription = "1번째 자리, 아직 촬영 전",
    )
}

@ComposePreview(name = "CardItem - 인화 대기")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaCardItemPrintWaitingPreview() {
    ChallaCardItem(
        modifier = Modifier.width(82.dp),
        order = 2,
        type = ChallaCardType.PrintWaiting(imageUrl = ""),
        contentDescription = "2번째 사진, 인화 전",
    )
}

@ComposePreview(name = "CardItem - 인화 대기(이미지 미수신)")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaCardItemPrintWaitingNoImagePreview() {
    ChallaCardItem(
        modifier = Modifier.width(82.dp),
        order = 2,
        type = ChallaCardType.PrintWaiting(imageUrl = null),
        contentDescription = "2번째 사진, 인화 전",
    )
}

@ComposePreview(name = "CardItem - 인화 완료")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaCardItemPrintedPreview() {
    ChallaCardItem(
        modifier = Modifier.width(82.dp),
        order = 3,
        type = ChallaCardType.Printed(imageUrl = ""),
        contentDescription = "3번째 사진",
        onClick = {},
    )
}
