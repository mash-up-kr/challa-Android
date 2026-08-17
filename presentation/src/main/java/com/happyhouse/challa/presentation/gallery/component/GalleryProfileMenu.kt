package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaProfileBar
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.gallery.contract.GalleryMemberUiModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryState.InviteMenu
import com.happyhouse.challa.presentation.gallery.previewGalleryMembers
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

/**
 * 참여자 프로필 바와, 바를 눌러 여는 초대 메뉴
 *
 * 첫 진입에는 [InviteMenu.Opened.showsTooltip]이 켜진 채로 열려 초대 코드 사용을 안내한다.
 */
@Composable
fun GalleryProfileMenu(
    members: ImmutableList<GalleryMemberUiModel>,
    invitationCode: String,
    inviteMenu: InviteMenu,
    onProfileBarClick: () -> Unit,
    onInviteCodeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 인화 카운트다운으로 1초마다 재구성되므로 참여자가 그대로면 목록을 다시 만들지 않는다.
        val profileImageUrls =
            remember(members) {
                members.map(GalleryMemberUiModel::profileImageUrl).toPersistentList()
            }

        ChallaProfileBar(
            profileImageUrls = profileImageUrls,
            contentDescription = stringResource(R.string.gallery_member_count_description, members.size),
            isExpanded = inviteMenu is InviteMenu.Opened,
            onClickLabel =
                stringResource(
                    if (inviteMenu is InviteMenu.Opened) {
                        R.string.gallery_invite_menu_close_label
                    } else {
                        R.string.gallery_invite_menu_open_label
                    },
                ),
            onClick = onProfileBarClick,
        )

        if (inviteMenu !is InviteMenu.Opened) return@Column

        GalleryInviteMenu(
            modifier =
                Modifier
                    .padding(top = 8.dp)
                    // 메뉴 안쪽 빈 곳을 눌러도 바깥 닫기 레이어로 새지 않도록 여기서 터치를 소비한다.
                    .pointerInput(Unit) { detectTapGestures {} },
            invitationCode = invitationCode,
            members = members,
            onInviteCodeClick = { onInviteCodeClick(invitationCode) },
        )

        if (inviteMenu.showsTooltip) {
            GalleryInviteTooltip(modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@ComposePreview(showBackground = true, name = "ProfileMenu - 닫힘")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryProfileMenuClosedPreview() {
    GalleryProfileMenuPreviewTemplate(inviteMenu = InviteMenu.Closed)
}

@ComposePreview(showBackground = true, name = "ProfileMenu - 열림")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryProfileMenuOpenedPreview() {
    GalleryProfileMenuPreviewTemplate(inviteMenu = InviteMenu.Opened(showsTooltip = false))
}

@ComposePreview(showBackground = true, name = "ProfileMenu - 열림(첫 진입 툴팁)")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryProfileMenuWithTooltipPreview() {
    GalleryProfileMenuPreviewTemplate(inviteMenu = InviteMenu.Opened(showsTooltip = true))
}

@Composable
private fun GalleryProfileMenuPreviewTemplate(inviteMenu: InviteMenu) {
    GalleryProfileMenu(
        members = previewGalleryMembers(),
        invitationCode = "1928121",
        inviteMenu = inviteMenu,
        onProfileBarClick = {},
        onInviteCodeClick = {},
    )
}
