package com.happyhouse.challa.presentation.gallery.component

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
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.CHALLA_PREVIEW_BACKGROUND
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.gallery.contract.GalleryMemberUiModel
import com.happyhouse.challa.presentation.gallery.previewGalleryMembers
import kotlinx.collections.immutable.ImmutableList
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private const val MAX_VISIBLE_MEMBER_COUNT = 9

private val MemberAvatarSize = 30.dp
private val MemberAvatarOverlap = 5.dp

/**
 * 방 참여자 프로필 바
 */
@Composable
fun GalleryProfileBar(
    members: ImmutableList<GalleryMemberUiModel>,
    modifier: Modifier = Modifier,
) {
    if (members.isEmpty()) return

    val visibleMembers = members.take(MAX_VISIBLE_MEMBER_COUNT)
    val overflowCount = members.size - visibleMembers.size
    val membersDescription = stringResource(R.string.gallery_member_count_description, members.size)

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
                .semantics(mergeDescendants = true) { contentDescription = membersDescription }
                .clip(CircleShape)
                .background(barColor)
                .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(-MemberAvatarOverlap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        visibleMembers.forEach { member ->
            MemberAvatar(member = member, borderColor = barColor)
        }

        if (overflowCount > 0) {
            MemberOverflowBadge(count = overflowCount, borderColor = barColor)
        }
    }
}

@Composable
private fun MemberAvatar(
    member: GalleryMemberUiModel,
    borderColor: Color,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        modifier = modifier.memberCircle(borderColor),
        model =
            ImageRequest
                .Builder(LocalContext.current)
                .data(member.profileImageUrl)
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
                .background(ChallaTheme.colors.backgroundLevel2),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.gallery_member_overflow, count),
            color = ChallaTheme.colors.labelNormal,
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

@ComposePreview(showBackground = true, backgroundColor = CHALLA_PREVIEW_BACKGROUND)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryProfileBarPreview() {
    GalleryProfileBar(members = previewGalleryMembers())
}

@ComposePreview(showBackground = true, backgroundColor = CHALLA_PREVIEW_BACKGROUND, name = "ProfileBar - 9명 초과")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryProfileBarOverflowPreview() {
    GalleryProfileBar(members = previewGalleryMembers(count = 12))
}
