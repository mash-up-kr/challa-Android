package com.happyhouse.challa.presentation.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.room.component.BottomActions
import com.happyhouse.challa.presentation.room.component.MemberCard
import com.happyhouse.challa.presentation.room.component.PhotoProgress
import com.happyhouse.challa.presentation.room.component.RoomTopBar
import com.happyhouse.challa.presentation.room.component.RoomWhite
import com.happyhouse.challa.presentation.room.component.StatusCard
import com.happyhouse.challa.presentation.room.contract.RoomMainUiState

@Composable
fun RoomMainScreen(
    onBackClick: () -> Unit,
    uiState: RoomMainUiState = RoomMainUiState(),
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(RoomWhite)
                .statusBarsPadding(),
    ) {
        RoomTopBar(
            title = uiState.title,
            onBackClick = onBackClick,
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(all = 20.dp),
        ) {
            MemberCard(
                memberInitials = uiState.memberInitials,
                maxMemberCount = uiState.maxMemberCount,
            )
            Spacer(modifier = Modifier.height(60.dp))
            PhotoProgress(
                currentCount = uiState.photoCount,
                totalCount = uiState.totalPhotoCount,
            )
            Spacer(modifier = Modifier.height(60.dp))
            StatusCard(status = uiState.status)
        }

        BottomActions(
            status = uiState.status,
            modifier =
                Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
        )
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomMainScreenPreview() {
    RoomMainScreen(
        onBackClick = {},
    )
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomMainScreenWaitingPreview() {
    RoomMainScreen(
        onBackClick = {},
        uiState =
            RoomMainUiState(
                photoCount = 24,
            ),
    )
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomMainScreenPublishedPreview() {
    RoomMainScreen(
        onBackClick = {},
        uiState =
            RoomMainUiState(
                photoCount = 24,
                isPublished = true,
            ),
    )
}
