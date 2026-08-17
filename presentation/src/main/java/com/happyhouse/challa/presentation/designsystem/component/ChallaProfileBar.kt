package com.happyhouse.challa.presentation.designsystem.component

import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

/** 이 수를 넘으면 나머지는 `+n` 뱃지로 묶는다. */
private const val MAX_VISIBLE_MEMBER_COUNT = 9

/** 프로필 사진(원형)의 지름 */
private val MemberPhotoSize = 30.dp

/**
 * 사진 바깥에 바 색으로 두르는 링의 두께.
 * 겹쳐 놓인 아바타끼리 경계를 만들어주는 역할이라 사진 크기에 더해진다(30 + 3*2 = 36).
 */
private val MemberRingWidth = 3.dp
private val MemberAvatarSize = MemberPhotoSize + MemberRingWidth * 2

/** 겹쳐 놓인 아바타의 중심 간 거리 */
private val MemberAvatarPitch = 24.5.dp

/** 바 높이가 40이 되도록 하는 여백 (2 + 36 + 2) */
private val BarPadding = 2.dp

/**
 * 겹쳐 놓인 참여자 프로필 바
 *
 * @param contentDescription 아바타가 하나씩 읽히면 소음이 되므로 바 전체를 대신 읽어줄 문구.
 *  아바타가 하나도 없으면 아예 그리지 않으므로 읽어줄 문구가 없는 경우가 없어 필수로 받는다.
 * @param isExpanded 바에 딸린 메뉴가 열려 있는지. 열려 있으면 배경색이 반전된다.
 * @param onClickLabel 누르면 무엇이 일어나는지 읽어줄 문구. [contentDescription]은 바가 무엇인지만 읽어준다.
 * @param onClick null이면 누를 수 없는 바로 그린다.
 */
@Composable
fun ChallaProfileBar(
    profileImageUrls: ImmutableList<String?>,
    contentDescription: String,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    onClickLabel: String? = null,
    onClick: (() -> Unit)? = null,
) {
    if (profileImageUrls.isEmpty()) return

    val visibleUrls = profileImageUrls.take(MAX_VISIBLE_MEMBER_COUNT)
    val overflowCount = profileImageUrls.size - visibleUrls.size

    val barColor =
        if (isExpanded) {
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
                .then(
                    if (onClick == null) {
                        Modifier
                    } else {
                        Modifier.noRippleClickOnce(
                            role = Role.Button,
                            onClickLabel = onClickLabel,
                            onClick = onClick,
                        )
                    },
                ).padding(BarPadding),
        horizontalArrangement = Arrangement.spacedBy(MemberAvatarPitch - MemberAvatarSize),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Row는 나중에 놓인 자식이 위에 그려지므로, 왼쪽이 오른쪽을 덮는 디자인에 맞춰 z축을 뒤집는다.
        visibleUrls.forEachIndexed { index, profileImageUrl ->
            ChallaProfileImage(
                modifier =
                    Modifier
                        .zIndex((visibleUrls.size - index).toFloat())
                        .memberCircle(barColor),
                profileImageUrl = profileImageUrl,
            )
        }

        if (overflowCount > 0) {
            // 맨 오른쪽이므로 아바타 전부보다 아래에 깔린다.
            MemberOverflowBadge(
                modifier = Modifier.zIndex(0f),
                count = overflowCount,
                ringColor = barColor,
            )
        }
    }
}

@Composable
private fun MemberOverflowBadge(
    count: Int,
    ringColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .memberCircle(ringColor)
                .background(ChallaTheme.colors.backgroundLevel1),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.challa_profile_bar_overflow, count),
            color = ChallaTheme.colors.labelNeutral,
            style = ChallaTheme.typography.descriptionLarge.bold,
        )
    }
}

/**
 * 겹쳐 놓인 프로필끼리 구분되도록 바 색과 같은 링을 사진 바깥에 두른 원형 영역
 *
 * 링은 사진을 덮는 테두리가 아니라 사진 바깥에 더해지는 영역이므로,
 * 사진은 [MemberPhotoSize] 그대로 보이고 원 전체는 [MemberAvatarSize]가 된다.
 */
private fun Modifier.memberCircle(ringColor: Color): Modifier =
    this
        .size(MemberAvatarSize)
        .clip(CircleShape)
        .background(ringColor)
        .padding(MemberRingWidth)
        .clip(CircleShape)

@ComposePreview(showBackground = true, name = "ProfileBar - 1명")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaProfileBarSinglePreview() {
    ChallaProfileBar(
        profileImageUrls = previewProfileImageUrls(count = 1),
        contentDescription = "참여자 1명",
    )
}

@ComposePreview(showBackground = true, name = "ProfileBar - 2명")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaProfileBarPairPreview() {
    ChallaProfileBar(
        profileImageUrls = previewProfileImageUrls(count = 2),
        contentDescription = "참여자 2명",
    )
}

@ComposePreview(showBackground = true, name = "ProfileBar - 6명")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaProfileBarPreview() {
    ChallaProfileBar(
        profileImageUrls = previewProfileImageUrls(count = 6),
        contentDescription = "참여자 6명",
    )
}

@ComposePreview(showBackground = true, name = "ProfileBar - 9명 (초과 직전)")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaProfileBarFullPreview() {
    ChallaProfileBar(
        profileImageUrls = previewProfileImageUrls(count = MAX_VISIBLE_MEMBER_COUNT),
        contentDescription = "참여자 9명",
    )
}

@ComposePreview(showBackground = true, name = "ProfileBar - 9명 초과")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaProfileBarOverflowPreview() {
    ChallaProfileBar(
        profileImageUrls = previewProfileImageUrls(count = 13),
        contentDescription = "참여자 13명",
    )
}

@ComposePreview(showBackground = true, name = "ProfileBar - 메뉴 열림")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaProfileBarExpandedPreview() {
    ChallaProfileBar(
        profileImageUrls = previewProfileImageUrls(count = 6),
        contentDescription = "참여자 6명",
        isExpanded = true,
        onClick = {},
    )
}

@ComposePreview(showBackground = true, name = "ProfileBar - 메뉴 열림(9명 초과)")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaProfileBarExpandedOverflowPreview() {
    ChallaProfileBar(
        profileImageUrls = previewProfileImageUrls(count = 13),
        contentDescription = "참여자 13명",
        isExpanded = true,
        onClick = {},
    )
}

@ComposePreview(showBackground = true, name = "ProfileBar - 프로필 사진 없음")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaProfileBarNoPhotoPreview() {
    ChallaProfileBar(
        profileImageUrls = List<String?>(3) { null }.toPersistentList(),
        contentDescription = "참여자 3명",
    )
}

private fun previewProfileImageUrls(count: Int): ImmutableList<String?> = List<String?>(count) { "" }.toPersistentList()
