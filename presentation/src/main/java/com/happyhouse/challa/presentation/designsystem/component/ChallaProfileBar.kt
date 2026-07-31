package com.happyhouse.challa.presentation.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

/** 이 수를 넘으면 나머지는 `+n` 뱃지로 묶는다. */
private const val MAX_VISIBLE_MEMBER_COUNT = 9

private val MemberAvatarSize = 30.dp
private val MemberAvatarOverlap = 5.dp

/**
 * 겹쳐 놓인 참여자 프로필 바
 *
 * @param contentDescription 아바타가 하나씩 읽히면 소음이 되므로 바 전체를 대신 읽어줄 문구.
 *  아바타가 하나도 없으면 아예 그리지 않으므로 읽어줄 문구가 없는 경우가 없어 필수로 받는다.
 */
@Composable
fun ChallaProfileBar(
    profileImageUrls: ImmutableList<String>,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    if (profileImageUrls.isEmpty()) return

    val visibleUrls = profileImageUrls.take(MAX_VISIBLE_MEMBER_COUNT)
    val overflowCount = profileImageUrls.size - visibleUrls.size

    val barColor =
        if (overflowCount > 0) {
            ChallaTheme.colors.staticBlack
        } else {
            ChallaTheme.colors.staticWhite
        }

    Row(
        modifier =
            modifier
                // 아바타를 하나씩 읽어주면 소음이 되므로 바 전체를 한 덩어리로 읽힌다.
                .semantics(mergeDescendants = true) {
                    this.contentDescription = contentDescription
                }.clip(CircleShape)
                .background(barColor)
                .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(-MemberAvatarOverlap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Row는 나중에 놓인 자식이 위에 그려지므로, 왼쪽이 오른쪽을 덮는 디자인에 맞춰 z축을 뒤집는다.
        visibleUrls.forEachIndexed { index, profileImageUrl ->
            MemberAvatar(
                modifier = Modifier.zIndex((visibleUrls.size - index).toFloat()),
                profileImageUrl = profileImageUrl,
                borderColor = barColor,
            )
        }

        if (overflowCount > 0) {
            // 맨 오른쪽이므로 아바타 전부보다 아래에 깔린다.
            MemberOverflowBadge(
                modifier = Modifier.zIndex(0f),
                count = overflowCount,
                borderColor = barColor,
            )
        }
    }
}

@Composable
private fun MemberAvatar(
    profileImageUrl: String,
    borderColor: Color,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        modifier = modifier.memberCircle(borderColor),
        model =
            ImageRequest
                .Builder(LocalContext.current)
                .data(profileImageUrl)
                .crossfade(true)
                .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        placeholder = painterResource(ChallaIcons.Profile),
        error = painterResource(ChallaIcons.Profile),
    )
}

@Composable
private fun MemberOverflowBadge(
    count: Int,
    borderColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .memberCircle(borderColor)
                .background(ChallaTheme.colors.backgroundLevel1),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.challa_profile_bar_overflow, count),
            color = ChallaTheme.colors.labelNeutral,
            style = ChallaTheme.typography.descriptionSmall.bold,
        )
    }
}

/**
 * 겹쳐 놓인 프로필끼리 구분되도록 바 색과 같은 테두리를 두른 원형 영역
 */
private fun Modifier.memberCircle(borderColor: Color): Modifier =
    this
        .size(MemberAvatarSize)
        .border(
            width = 1.5.dp,
            color = borderColor,
            shape = CircleShape,
        ).clip(CircleShape)

@ComposePreview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaProfileBarPreview() {
    ChallaProfileBar(
        profileImageUrls = previewProfileImageUrls(count = 6),
        contentDescription = "참여자 6명",
    )
}

@ComposePreview(showBackground = true, name = "ProfileBar - 9명 초과")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaProfileBarOverflowPreview() {
    ChallaProfileBar(
        profileImageUrls = previewProfileImageUrls(count = 12),
        contentDescription = "참여자 12명",
    )
}

private fun previewProfileImageUrls(count: Int): ImmutableList<String> = List(count) { "" }.toPersistentList()
