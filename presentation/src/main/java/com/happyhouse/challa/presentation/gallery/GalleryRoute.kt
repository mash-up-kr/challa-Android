package com.happyhouse.challa.presentation.gallery

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
import com.happyhouse.challa.presentation.gallery.contract.GallerySideEffect
import kotlinx.coroutines.launch

// 상단바 아래에 토스트가 뜨도록 주는 여백
private val ToastTopOffset = 8.dp

@Composable
fun GalleryRoute(
    roomId: Long,
    onBackClick: () -> Unit,
    onPhotoClick: (Long) -> Unit,
    onShootClick: () -> Unit,
    viewModel: GalleryViewModel =
        hiltViewModel<GalleryViewModel, GalleryViewModel.Factory>(
            creationCallback = { factory ->
                factory.create(roomId)
            },
        ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val printWaitingMessage = stringResource(R.string.gallery_print_waiting_message)
    val loadMoreFailureMessage = stringResource(R.string.gallery_load_more_failure)
    val membersFailureMessage = stringResource(R.string.gallery_members_load_failure)
    val inviteCodeCopySuccessMessage = stringResource(R.string.gallery_invite_code_copy_success)
    val inviteCodeCopyFailureMessage = stringResource(R.string.gallery_invite_code_copy_failure)
    val destructiveIconTint = ChallaTheme.colors.statusDestructive

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is GallerySideEffect.NavigateToPhotoDetail -> onPhotoClick(effect.photoId)
                GallerySideEffect.NavigateToCamera -> onShootClick()
                GallerySideEffect.PrintNotCompleted -> {
                    // showSnackbar는 스낵바가 사라질 때까지 suspend 하므로,
                    // 그대로 두면 후속 SideEffect 수집이 막힌다.
                    launch {
                        snackbarHostState.showSnackbar(
                            ChallaToastVisuals(
                                message = printWaitingMessage,
                                topOffset = ToastTopOffset,
                            ),
                        )
                    }
                }

                GallerySideEffect.PhotosLoadMoreFailed -> {
                    launch {
                        snackbarHostState.showSnackbar(
                            ChallaToastVisuals(
                                message = loadMoreFailureMessage,
                                icon = ChallaIcons.Error,
                                iconTint = destructiveIconTint,
                                topOffset = ToastTopOffset,
                            ),
                        )
                    }
                }

                GallerySideEffect.MembersLoadFailed -> {
                    launch {
                        snackbarHostState.showSnackbar(
                            ChallaToastVisuals(
                                message = membersFailureMessage,
                                icon = ChallaIcons.Error,
                                iconTint = destructiveIconTint,
                                topOffset = ToastTopOffset,
                            ),
                        )
                    }
                }

                GallerySideEffect.InviteCodeCopied -> {
                    launch {
                        snackbarHostState.showSnackbar(
                            ChallaToastVisuals(
                                message = inviteCodeCopySuccessMessage,
                                topOffset = ToastTopOffset,
                            ),
                        )
                    }
                }

                GallerySideEffect.InviteCodeCopyFailed -> {
                    launch {
                        snackbarHostState.showSnackbar(
                            ChallaToastVisuals(
                                message = inviteCodeCopyFailureMessage,
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

    GalleryScreen(
        modifier = Modifier.fillMaxSize(),
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick,
    )
}
