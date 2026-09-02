package com.happyhouse.challa.presentation.roomcover.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.happyhouse.challa.presentation.roomcover.model.RoomCoverUiModel

private val CardWidth = 200.dp
private val CardHeight = 266.dp
private val ActionBarHeight = 36.dp

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
    canRemoveImage: Boolean,
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
                    .clip(RoundedCornerShape(12.dp)),
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
                                colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent),
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
        }

        Row(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .height(ActionBarHeight)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ChallaTheme.colors.backgroundLevel3.copy(alpha = 0.9f)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoverImageAction(
                icon = ChallaIcons.Camera,
                contentDescription = stringResource(id = R.string.room_cover_select_image_description),
                enabled = true,
                onClick = onSelectImageClick,
            )
            VerticalDivider(
                modifier = Modifier.height(16.dp),
                color = ChallaTheme.colors.lineNormal,
            )
            CoverImageAction(
                icon = ChallaIcons.Close,
                contentDescription = stringResource(id = R.string.room_cover_remove_image_description),
                enabled = canRemoveImage,
                onClick = onRemoveImageClick,
            )
        }
    }
}

@Composable
private fun CoverImageAction(
    icon: Int,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(width = 44.dp, height = ActionBarHeight)
                .noRippleClickOnce(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = if (enabled) ChallaTheme.colors.labelNormal else ChallaTheme.colors.labelDisable,
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
        canRemoveImage = false,
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
                imageUrl = "https://challa.example/cover.jpg",
                sticker =
                    RoomCoverUiModel.Sticker(
                        imageUrl = "https://challa.example/sticker.png",
                        color = Color(0xFFD5F700),
                    ),
            ),
        canRemoveImage = true,
        onSelectImageClick = {},
        onRemoveImageClick = {},
    )
}
