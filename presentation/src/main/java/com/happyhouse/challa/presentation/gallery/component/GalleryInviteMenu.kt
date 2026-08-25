package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaProfileImage
import com.happyhouse.challa.presentation.designsystem.foundation.icon.ChallaIconSize
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import com.happyhouse.challa.presentation.gallery.contract.GalleryMemberUiModel
import com.happyhouse.challa.presentation.gallery.previewGalleryMembers
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private val MenuWidth = 200.dp

private val MenuPadding = 20.dp

private val MemberListMaxHeight = 450.dp

/**
 * 초대 코드와 방 참여자 목록을 담은 메뉴
 *
 * @param members 비어 있으면 초대 코드만 그린다.
 */
@Composable
fun GalleryInviteMenu(
    invitationCode: String,
    members: ImmutableList<GalleryMemberUiModel>,
    onInviteCodeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .width(MenuWidth)
                .clip(RoundedCornerShape(16.dp))
                .background(ChallaTheme.colors.staticBlack)
                .padding(vertical = MenuPadding),
    ) {
        InviteCode(
            modifier = Modifier.padding(horizontal = MenuPadding),
            invitationCode = invitationCode,
            onClick = onInviteCodeClick,
        )

        if (members.isNotEmpty()) {
            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        vertical = 12.dp,
                        horizontal = MenuPadding,
                    ),
                color = ChallaTheme.colors.lineNormal,
            )

            MemberList(
                modifier = Modifier.padding(horizontal = MenuPadding),
                members = members,
            )
        }
    }
}

@Composable
private fun InviteCode(
    invitationCode: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier.noRippleClickOnce(
                role = Role.Button,
                onClickLabel = stringResource(R.string.gallery_invite_code_copy_label),
                onClick = onClick,
            ).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.gallery_invite_code_label),
            color = ChallaTheme.colors.labelNeutral,
            style = ChallaTheme.typography.descriptionLarge.medium,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = invitationCode,
                modifier = Modifier.weight(weight = 1f, fill = false),
                color = ChallaTheme.colors.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = ChallaTheme.typography.headingMedium.bold,
            )

            Icon(
                modifier = Modifier.size(ChallaIconSize.V20.dp),
                painter = painterResource(ChallaIcons.Copy),
                contentDescription = null,
                tint = ChallaTheme.colors.labelDisable,
            )
        }
    }
}

@Composable
private fun MemberList(
    members: ImmutableList<GalleryMemberUiModel>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.heightIn(max = MemberListMaxHeight),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(items = members, key = GalleryMemberUiModel::id) { member ->
            Member(member = member)
        }
    }
}

@Composable
private fun Member(
    member: GalleryMemberUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChallaProfileImage(
            modifier = Modifier.size(20.dp),
            profileImageUrl = member.profileImageUrl,
        )

        Text(
            text = member.nickname,
            color = ChallaTheme.colors.labelSubtle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = ChallaTheme.typography.bodyXSmall.medium,
        )
    }
}

@ComposePreview(showBackground = true, name = "InviteMenu - 참여자 3명")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryInviteMenuPreview() {
    GalleryInviteMenu(
        invitationCode = "1928121",
        members = previewGalleryMembers(count = 3),
        onInviteCodeClick = {},
    )
}

@ComposePreview(showBackground = true, heightDp = 640, name = "InviteMenu - 참여자 많음(스크롤)")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryInviteMenuManyMembersPreview() {
    GalleryInviteMenu(
        invitationCode = "1928121",
        members = previewGalleryMembers(count = 20),
        onInviteCodeClick = {},
    )
}

@ComposePreview(showBackground = true, name = "InviteMenu - 참여자 없음")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryInviteMenuEmptyMembersPreview() {
    GalleryInviteMenu(
        invitationCode = "1928121",
        members = persistentListOf(),
        onInviteCodeClick = {},
    )
}
