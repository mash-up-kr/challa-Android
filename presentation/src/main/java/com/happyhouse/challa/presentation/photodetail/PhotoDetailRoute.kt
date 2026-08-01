package com.happyhouse.challa.presentation.photodetail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaToastVisuals
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailIntent
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailSideEffect
import com.happyhouse.challa.presentation.photodetail.permission.rememberPhotoSavePermissionGate
import kotlinx.coroutines.launch

// 상단바 아래에 토스트가 뜨도록 주는 여백
private val ToastTopOffset = 8.dp

@Composable
fun PhotoDetailRoute(
    roomId: Long,
    photoId: Long,
    onBackClick: () -> Unit,
    viewModel: PhotoDetailViewModel =
        hiltViewModel<PhotoDetailViewModel, PhotoDetailViewModel.Factory>(
            creationCallback = { factory ->
                factory.create(roomId = roomId, initialPhotoId = photoId)
            },
        ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val saveSuccessMessage = stringResource(R.string.photo_detail_save_success)
    val saveFailureMessage = stringResource(R.string.photo_detail_save_failure)
    val loadFailureMessage = stringResource(R.string.photo_detail_load_failure)
    val reactionFailureMessage = stringResource(R.string.photo_detail_reaction_failure)
    val destructiveIconTint = ChallaTheme.colors.statusDestructive

    val requestSave =
        rememberPhotoSavePermissionGate(
            onDenied = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        ChallaToastVisuals(
                            message = saveFailureMessage,
                            icon = ChallaIcons.Error,
                            iconTint = destructiveIconTint,
                            topOffset = ToastTopOffset,
                        ),
                    )
                }
            },
            onGranted = { photo -> viewModel.onIntent(PhotoDetailIntent.PhotoSave(photo)) },
        )

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            val visuals =
                when (effect) {
                    PhotoDetailSideEffect.PhotosLoadFailed ->
                        ChallaToastVisuals(
                            message = loadFailureMessage,
                            icon = ChallaIcons.Error,
                            iconTint = destructiveIconTint,
                            topOffset = ToastTopOffset,
                        )

                    PhotoDetailSideEffect.SaveSucceeded ->
                        ChallaToastVisuals(
                            message = saveSuccessMessage,
                            icon = ChallaIcons.Check,
                            topOffset = ToastTopOffset,
                        )

                    PhotoDetailSideEffect.SaveFailed ->
                        ChallaToastVisuals(
                            message = saveFailureMessage,
                            icon = ChallaIcons.Error,
                            iconTint = destructiveIconTint,
                            topOffset = ToastTopOffset,
                        )

                    PhotoDetailSideEffect.ReactionSendFailed ->
                        ChallaToastVisuals(
                            message = reactionFailureMessage,
                            icon = ChallaIcons.Error,
                            iconTint = destructiveIconTint,
                            topOffset = ToastTopOffset,
                        )
                }
            launch { snackbarHostState.showSnackbar(visuals) }
        }
    }

    PhotoDetailScreen(
        modifier = Modifier.fillMaxSize(),
        state = state,
        snackbarHostState = snackbarHostState,
        onRetryClick = { viewModel.onIntent(PhotoDetailIntent.PhotosLoad) },
        onSaveClick = requestSave,
        onEmojiClick = { photo, emoji -> viewModel.onIntent(PhotoDetailIntent.ReactionClick(photo, emoji)) },
        onMessageChange = { message -> viewModel.onIntent(PhotoDetailIntent.MessageChange(message)) },
        onSendClick = { photo -> viewModel.onIntent(PhotoDetailIntent.MessageSend(photo)) },
        onBackClick = onBackClick,
    )
}
