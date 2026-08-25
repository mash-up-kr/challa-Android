package com.happyhouse.challa.presentation.roomsetting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.happyhouse.challa.presentation.roomsetting.contract.RoomSettingIntent
import com.happyhouse.challa.presentation.roomsetting.contract.RoomSettingState

@Composable
fun RoomSettingScreen(
    state: RoomSettingState,
    snackbarHostState: SnackbarHostState,
    onIntent: (RoomSettingIntent) -> Unit,
    onBackClick: () -> Unit,
    onCoverImageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChallaScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHostState = snackbarHostState,
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
                    trailingText = state.roomName,
                    onClick = { onIntent(RoomSettingIntent.RoomNameClick) },
                )
                RoomSettingListItem(
                    text = stringResource(R.string.room_setting_cover_image),
                    leadingIcon = ChallaIcons.Image,
                    onClick = onCoverImageClick,
                )
            }
        }
    }

    if (state.isEditRoomNameSheetVisible) {
        EditRoomNameBottomSheet(
            roomName = state.roomName,
            isSubmitting = state.isSubmitting,
            onDismiss = { onIntent(RoomSettingIntent.EditRoomNameSheetDismiss) },
            onConfirm = { newRoomName -> onIntent(RoomSettingIntent.RoomNameSubmit(newRoomName)) },
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun RoomSettingScreenPreview() {
    RoomSettingScreen(
        state = RoomSettingState(roomName = "친구들과 강릉 여행"),
        snackbarHostState = remember { SnackbarHostState() },
        onIntent = {},
        onBackClick = {},
        onCoverImageClick = {},
    )
}
