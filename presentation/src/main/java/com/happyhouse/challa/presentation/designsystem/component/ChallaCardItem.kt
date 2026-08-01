package com.happyhouse.challa.presentation.designsystem.component

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

/** 디자인 시스템 Card Item 타입. (`더보기`는 아직 쓰는 화면이 없어 제외) */
@Immutable
sealed interface ChallaCardType {
    /** 촬영 전 */
    data object NotCaptured : ChallaCardType

    /** 인화 대기 */
    data class PrintWaiting(
        val imageUrl: String,
    ) : ChallaCardType

    /** 인화 완료 */
    data class Printed(
        val imageUrl: String,
    ) : ChallaCardType
}

/**
 * 필름 그리드의 카드 1칸. 인화 상태에 따라 빈 칸 / 흐린 사진 / 공개된 사진을 그린다.
 *
 * @param contentDescription 번호와 이미지가 따로 읽히지 않도록 카드 전체를 대신 읽어줄 문구.
 *  카드는 항상 순서 번호를 그리므로 읽어줄 문구가 없는 경우가 없어 필수로 받는다.
 * @param onClick 넘기지 않으면 클릭 영역을 두지 않는다.
 */
@Composable
fun ChallaCardItem(
    order: Int,
    type: ChallaCardType,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClickLabel: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier =
            modifier
                // 번호가 따로 읽히지 않도록 카드 전체를 한 덩어리로 읽힌다.
                .semantics(mergeDescendants = true) {
                    this.contentDescription = contentDescription
                }.aspectRatio(CARD_ASPECT_RATIO)
                .clip(CardShape)
                .then(
                    when (type) {
                        ChallaCardType.NotCaptured ->
                            Modifier.dashedRoundedBorder(
                                color = ChallaTheme.colors.lineNormal,
                                cornerRadius = CardCornerRadius,
                                // 옆칸 카드의 실선과 굵기를 맞춘다.
                                strokeWidth = CardBorderWidth,
                                dashLength = CardDashLength,
                                gapLength = CardDashLength,
                            )

                        is ChallaCardType.PrintWaiting,
                        is ChallaCardType.Printed,
                        ->
                            Modifier.border(
                                width = CardBorderWidth,
                                color = ChallaTheme.colors.lineNeutral,
                                shape = CardShape,
                            )
                    },
                ).then(
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
                CardImage(
                    imageUrl = type.imageUrl,
                    // TODO: 서버가 블러 이미지를 내려주면 이중 블러가 되므로 제거할 것
                    blurred = true,
                )
            }

            is ChallaCardType.Printed -> CardImage(imageUrl = type.imageUrl, blurred = false)
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

@ComposePreview(showBackground = true, name = "CardItem - 촬영 전")
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

@ComposePreview(showBackground = true, name = "CardItem - 인화 대기")
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

@ComposePreview(showBackground = true, name = "CardItem - 인화 완료")
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
