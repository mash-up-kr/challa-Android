package com.happyhouse.challa.presentation.roomsetting

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
import com.happyhouse.challa.presentation.roomsetting.contract.RoomSettingSideEffect
import kotlinx.coroutines.launch

// 상단바 아래에 토스트가 뜨도록 주는 여백
private val ToastTopOffset = 8.dp

@Composable
fun RoomSettingRoute(
    roomId: Long,
    roomName: String,
    onBackClick: () -> Unit,
    onCoverImageClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoomSettingViewModel =
        hiltViewModel<RoomSettingViewModel, RoomSettingViewModel.Factory>(
            creationCallback = { factory ->
                factory.create(roomId, roomName)
            },
        ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val updateFailureMessage = stringResource(R.string.room_setting_room_name_update_failure)
    val destructiveIconTint = ChallaTheme.colors.statusDestructive

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                RoomSettingSideEffect.RoomNameUpdateFailed -> {
                    // showSnackbar는 스낵바가 사라질 때까지 suspend 하므로,
                    // 그대로 두면 후속 SideEffect 수집이 막힌다.
                    launch {
                        snackbarHostState.showSnackbar(
                            ChallaToastVisuals(
                                message = updateFailureMessage,
                                icon = ChallaIcons.Error,
                                iconTint = destructiveIconTint,
                                topOffset = ToastTopOffset,
                            ),
                        )
                    }
                }
            }
        }
    }

    RoomSettingScreen(
        modifier = modifier.fillMaxSize(),
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick,
        onCoverImageClick = onCoverImageClick,
    )
}
