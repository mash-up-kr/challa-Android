package com.happyhouse.challa.presentation.room.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.White
import com.happyhouse.challa.presentation.model.Room
import com.happyhouse.challa.presentation.model.RoomStatus
import com.happyhouse.challa.presentation.room.main.component.BottomActions
import com.happyhouse.challa.presentation.room.main.component.MemberCard
import com.happyhouse.challa.presentation.room.main.component.PhotoProgress
import com.happyhouse.challa.presentation.room.main.component.RoomMainTopBar
import com.happyhouse.challa.presentation.room.main.component.StatusCard
import com.happyhouse.challa.presentation.room.main.contract.RoomMainIntent
import com.happyhouse.challa.presentation.room.main.contract.RoomMainSideEffect
import com.happyhouse.challa.presentation.room.main.contract.RoomMainState
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Duration.Companion.hours

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
                RoomMainSideEffect.ShareRequested -> onShareClick()
            }
        }
    }

    Scaffold(
        containerColor = White,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { innerPadding ->
        RoomMainScreen(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onBackClick = onBackClick,
            onShareClick = { viewModel.onIntent(RoomMainIntent.ShareClick) },
            onMainActionClick = { status ->
                when (status) {
                    is RoomStatus.Shooting -> onCameraClick()
                    is RoomStatus.Waiting -> Unit
                    RoomStatus.Opened -> onGalleryClick()
                    is RoomStatus.Expiring -> onGalleryClick()
                }
            },
        )
    }
}

@Composable
fun RoomMainScreen(
    uiState: RoomMainState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onMainActionClick: (RoomStatus) -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(White),
    ) {
        RoomMainTopBar(
            title =
                when (uiState) {
                    is RoomMainState.Content -> uiState.room.name
                    RoomMainState.Loading,
                    RoomMainState.Error,
                    -> ""
                },
            onBackClick = onBackClick,
        )

        when (uiState) {
            RoomMainState.Loading -> {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is RoomMainState.Content -> {
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
                    StatusCard(room = uiState.room)
                }

                BottomActions(
                    status = uiState.room.status,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                    onShareClick = onShareClick,
                    onMainActionClick = { onMainActionClick(uiState.room.status) },
                )
            }

            RoomMainState.Error -> {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "방 정보를 불러오지 못했어요.")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomMainScreenPreview() {
    RoomMainScreen(uiState = previewRoomMainState())
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomMainScreenWaitingPreview() {
    RoomMainScreen(
        uiState =
            previewRoomMainState(
                status = RoomStatus.Waiting(dDay = 0, remaining = 3.hours),
            ),
    )
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomMainScreenPublishedPreview() {
    RoomMainScreen(
        uiState = previewRoomMainState(status = RoomStatus.Opened),
    )
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomMainScreenLoadingPreview() {
    RoomMainScreen(uiState = RoomMainState.Loading)
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomMainScreenErrorPreview() {
    RoomMainScreen(uiState = RoomMainState.Error)
}

private fun previewRoomMainState(status: RoomStatus = RoomStatus.Shooting(taken = 11)): RoomMainState =
    RoomMainState.Content(
        room =
            Room(
                id = "preview-room-id",
                name = "해피하우스 프작모",
                status = status,
            ),
        memberInitials = persistentListOf("박", "김", "이"),
        maxMemberCount = 12,
    )
