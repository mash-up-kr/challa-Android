package com.happyhouse.challa.presentation.roomcover.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import com.happyhouse.challa.presentation.roomcover.PREVIEW_COVER_IMAGE_URL
import com.happyhouse.challa.presentation.roomcover.PREVIEW_STICKER_IMAGE_URL
import com.happyhouse.challa.presentation.roomcover.model.RoomCoverUiModel

private val CardWidth = 200.dp
private val CardHeight = 266.dp
private val CardShape = RoundedCornerShape(12.dp)
private val CardBorderWidth = 2.dp
private val ActionBarHeight = 40.dp
private val ActionBarHorizontalPadding = 10.dp
private val ActionBarGap = 6.dp
private val ActionIconSize = 24.dp
private val ActionDividerWidth = 2.dp
private val ActionDividerHeight = 16.dp

/** 흰색 오버레이가 사라지는 지점. 시안에서 카드 높이의 78%로 측정했다. */
private const val WHITE_OVERLAY_END = 0.78f

/**
 * 커버가 홈 카드에 어떻게 보일지 미리 보여주는 카드.
 *
 * 배경 사진을 고르고 지우는 버튼이 카드 아래쪽에 걸쳐 있다.
 */
@Composable
fun RoomCoverPreviewCard(
    roomName: String,
    memberCount: Int,
    cover: RoomCoverUiModel,
    onSelectImageClick: () -> Unit,
    onRemoveImageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.height(CardHeight + ActionBarHeight / 2),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier =
                Modifier
                    .width(CardWidth)
                    .height(CardHeight)
                    .clip(CardShape),
        ) {
            RoomCoverBackground(
                cover = cover,
                modifier = Modifier.fillMaxSize(),
            )
            // 커버가 밝아도 방 이름이 읽히도록 위쪽을 어둡게 덮는다.
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.Black.copy(alpha = 0.8f),
                                        Color.Black.copy(alpha = 0.2f),
                                    ),
                            ),
                        ),
            )
            // 어둡게 덮은 위를 다시 살짝 띄운다. 검정 위에 얹어야 시안과 같은 밝기가 나온다.
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops =
                                    arrayOf(
                                        0f to Color.White.copy(alpha = 0.2f),
                                        WHITE_OVERLAY_END to Color.Transparent,
                                    ),
                            ),
                        ),
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = roomName,
                    color = ChallaTheme.colors.labelNormal,
                    style = ChallaTheme.typography.bodyMedium.bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = ChallaIcons.Person),
                        contentDescription = stringResource(id = R.string.room_cover_member_count_description),
                        modifier = Modifier.size(14.dp),
                        tint = ChallaTheme.colors.labelSubtle,
                    )
                    Text(
                        text = memberCount.toString(),
                        color = ChallaTheme.colors.labelSubtle,
                        style = ChallaTheme.typography.descriptionLarge.bold,
                    )
                }
            }

            // 테두리를 modifier로 주면 배경·내용에 가려지므로 맨 위에 얹는다.
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .border(
                            width = CardBorderWidth,
                            color = ChallaTheme.colors.lineNormal,
                            shape = CardShape,
                        ),
            )
        }

        Row(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .height(ActionBarHeight)
                    .clip(CircleShape)
                    .background(ChallaTheme.colors.backgroundLevel2)
                    .border(
                        width = CardBorderWidth,
                        color = ChallaTheme.colors.lineNormal,
                        shape = CircleShape,
                    )
                    .padding(horizontal = ActionBarHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(ActionBarGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoverImageAction(
                icon = ChallaIcons.Camera,
                contentDescription = stringResource(id = R.string.room_cover_select_image_description),
                onClick = onSelectImageClick,
            )
            VerticalDivider(
                modifier = Modifier.height(ActionDividerHeight),
                thickness = ActionDividerWidth,
                color = ChallaTheme.colors.lineNormal,
            )
            CoverImageAction(
                icon = ChallaIcons.Close,
                contentDescription = stringResource(id = R.string.room_cover_remove_image_description),
                onClick = onRemoveImageClick,
            )
        }
    }
}

@Composable
private fun CoverImageAction(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                // 아이콘은 24dp지만 누르는 영역은 알약 높이만큼 잡는다.
                .width(ActionIconSize)
                .fillMaxHeight()
                .noRippleClickOnce(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(ActionIconSize),
            tint = ChallaTheme.colors.labelAlternative,
        )
    }
}

@Preview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomCoverPreviewCardEmptyPreview() {
    RoomCoverPreviewCard(
        roomName = "친구들과 유럽 여행",
        memberCount = 12,
        cover = RoomCoverUiModel(),
        onSelectImageClick = {},
        onRemoveImageClick = {},
    )
}

@Preview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomCoverPreviewCardWithImagePreview() {
    RoomCoverPreviewCard(
        roomName = "친구들과 유럽 여행",
        memberCount = 12,
        cover =
            RoomCoverUiModel(
                imageUrl = PREVIEW_COVER_IMAGE_URL,
                sticker =
                    RoomCoverUiModel.Sticker(
                        imageUrl = PREVIEW_STICKER_IMAGE_URL,
                        color = ChallaTheme.colors.primaryYellow,
                    ),
            ),
        onSelectImageClick = {},
        onRemoveImageClick = {},
    )
}
