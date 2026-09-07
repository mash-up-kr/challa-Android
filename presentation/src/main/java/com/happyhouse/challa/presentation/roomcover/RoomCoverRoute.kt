package com.happyhouse.challa.presentation.roomcover

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaToastVisuals
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.roomcover.contract.RoomCoverIntent
import com.happyhouse.challa.presentation.roomcover.contract.RoomCoverSideEffect
import kotlinx.coroutines.launch

// 상단바 아래에 토스트가 뜨도록 주는 여백
private val ToastTopOffset = 8.dp

@Composable
fun RoomCoverRoute(
    roomId: Long,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoomCoverViewModel =
        hiltViewModel<RoomCoverViewModel, RoomCoverViewModel.Factory>(
            creationCallback = { factory ->
                factory.create(roomId)
            },
        ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coverUpdateFailureMessage = stringResource(R.string.room_cover_update_failure)
    val imageUploadFailureMessage = stringResource(R.string.room_cover_image_upload_failure)
    val destructiveIconTint = ChallaTheme.colors.statusDestructive

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            uri?.let { viewModel.onIntent(RoomCoverIntent.BackgroundImageSelect(it.toString())) }
        }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            val message =
                when (effect) {
                    RoomCoverSideEffect.CoverUpdateFailed -> coverUpdateFailureMessage
                    RoomCoverSideEffect.BackgroundImageUploadFailed -> imageUploadFailureMessage
                }

            // showSnackbar는 스낵바가 사라질 때까지 suspend 하므로,
            // 그대로 두면 후속 SideEffect 수집이 막힌다.
            launch {
                snackbarHostState.showSnackbar(
                    ChallaToastVisuals(
                        message = message,
                        icon = ChallaIcons.Error,
                        iconTint = destructiveIconTint,
                        topOffset = ToastTopOffset,
                    ),
                )
            }
        }
    }

    RoomCoverScreen(
        modifier = modifier.fillMaxSize(),
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick,
        onSelectImageClick = {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
    )
}
