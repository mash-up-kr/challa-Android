package com.happyhouse.challa.presentation.room.main

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.White
import com.happyhouse.challa.presentation.room.main.component.BottomActions
import com.happyhouse.challa.presentation.room.main.component.MemberCard
import com.happyhouse.challa.presentation.room.main.component.PhotoProgress
import com.happyhouse.challa.presentation.room.main.component.RoomTopBar
import com.happyhouse.challa.presentation.room.main.component.StatusCard
import com.happyhouse.challa.presentation.room.main.contract.RoomMainUiIntent
import com.happyhouse.challa.presentation.room.main.contract.RoomMainUiSideEffect
import com.happyhouse.challa.presentation.room.main.contract.RoomMainUiState
import com.happyhouse.challa.presentation.room.main.model.RoomMainStatus

@Composable
fun RoomMainRoute(
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    viewModel: RoomMainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                RoomMainUiSideEffect.ShareRequested -> onShareClick()
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { innerPadding ->
        RoomMainScreen(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onBackClick = onBackClick,
            onShareClick = { viewModel.onIntent(RoomMainUiIntent.ShareClick) },
            onMainActionClick = {
                when (uiState.status) {
                    RoomMainStatus.Shooting -> onCameraClick()
                    RoomMainStatus.Waiting -> Unit
                    RoomMainStatus.Published -> onGalleryClick()
                }
            },
        )
    }
}

@Composable
fun RoomMainScreen(
    modifier: Modifier = Modifier,
    uiState: RoomMainUiState = RoomMainUiState(),
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onMainActionClick: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(White)
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
            onShareClick = onShareClick,
            onMainActionClick = onMainActionClick,
        )
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomMainScreenPreview() {
    RoomMainScreen()
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomMainScreenWaitingPreview() {
    RoomMainScreen(
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
        uiState =
            RoomMainUiState(
                photoCount = 24,
                isPublished = true,
            ),
    )
}
