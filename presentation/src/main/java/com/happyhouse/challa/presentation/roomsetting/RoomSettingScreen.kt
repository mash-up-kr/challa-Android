package com.happyhouse.challa.presentation.roomsetting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaNavigationIconButton
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigation
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigationVariant
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.layout.ChallaScaffold
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.roomsetting.component.EditRoomNameBottomSheet
import com.happyhouse.challa.presentation.roomsetting.component.RoomSettingCard
import com.happyhouse.challa.presentation.roomsetting.component.RoomSettingListItem

@Composable
fun RoomSettingScreen(
    roomName: String,
    onBackClick: () -> Unit,
    onCoverImageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO: 방 이름 수정 API가 없어 변경한 이름을 이 화면에서만 반영한다. API 연동 시 제거 예정.
    var currentRoomName by rememberSaveable(roomName) { mutableStateOf(roomName) }
    var isEditRoomNameSheetVisible by rememberSaveable { mutableStateOf(false) }

    ChallaScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ChallaTopNavigation(
                title = stringResource(R.string.room_setting_title),
                variant = ChallaTopNavigationVariant.SUB,
                leadingIcon = {
                    ChallaNavigationIconButton(
                        icon = ChallaIcons.Left,
                        onClick = onBackClick,
                        contentDescription = stringResource(R.string.room_setting_back_description),
                    )
                },
            )
        },
    ) { contentPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RoomSettingCard {
                RoomSettingListItem(
                    text = stringResource(R.string.room_setting_room_name),
                    leadingIcon = ChallaIcons.Edit,
                    trailingText = currentRoomName,
                    onClick = { isEditRoomNameSheetVisible = true },
                )
                RoomSettingListItem(
                    text = stringResource(R.string.room_setting_cover_image),
                    leadingIcon = ChallaIcons.Image,
                    onClick = onCoverImageClick,
                )
            }
        }
    }

    if (isEditRoomNameSheetVisible) {
        EditRoomNameBottomSheet(
            roomName = currentRoomName,
            onDismiss = { isEditRoomNameSheetVisible = false },
            onConfirm = { newRoomName ->
                currentRoomName = newRoomName
                isEditRoomNameSheetVisible = false
            },
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun RoomSettingScreenPreview() {
    RoomSettingScreen(
        roomName = "친구들과 강릉 여행",
        onBackClick = {},
        onCoverImageClick = {},
    )
}
