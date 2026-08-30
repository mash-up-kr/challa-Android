package com.happyhouse.challa.presentation.gallery

import android.content.ClipData
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.domain.model.RoomMemberJoinedEvent
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaToastVisuals
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.gallery.contract.GalleryIntent
import com.happyhouse.challa.presentation.gallery.contract.GallerySideEffect
import com.happyhouse.challa.presentation.navigation.PhotoDetailArgs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber

// 상단바 아래에 토스트가 뜨도록 주는 여백
private val ToastTopOffset = 8.dp

/** 붙여넣을 때 시스템이 함께 보여줄 수 있는 이름 */
private const val INVITE_CODE_CLIP_LABEL = "challa invite code"

@Composable
fun GalleryRoute(
    roomId: Long,
    memberJoinedEvents: Flow<RoomMemberJoinedEvent>,
    onBackClick: () -> Unit,
    onPhotoClick: (args: PhotoDetailArgs) -> Unit,
    onShootClick: () -> Unit,
    onChatClick: (roomName: String) -> Unit,
    onSettingClick: (roomName: String) -> Unit,
    viewModel: GalleryViewModel =
        hiltViewModel<GalleryViewModel, GalleryViewModel.Factory>(
            creationCallback = { factory ->
                factory.create(roomId)
            },
        ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val shouldRefreshAfterCamera = rememberSaveable { mutableStateOf(false) }
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val printWaitingMessage = stringResource(R.string.gallery_print_waiting_message)
    val loadMoreFailureMessage = stringResource(R.string.gallery_load_more_failure)
    val membersFailureMessage = stringResource(R.string.gallery_members_load_failure)
    val inviteCodeCopySuccessMessage = stringResource(R.string.gallery_invite_code_copy_success)
    val inviteCodeCopyFailureMessage = stringResource(R.string.gallery_invite_code_copy_failure)
    val destructiveIconTint = ChallaTheme.colors.statusDestructive

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (shouldRefreshAfterCamera.value) {
            shouldRefreshAfterCamera.value = false
            viewModel.onIntent(GalleryIntent.PhotosLoad)
        }
    }

    LaunchedEffect(viewModel, roomId, memberJoinedEvents) {
        memberJoinedEvents.collect { event ->
            if (event.roomId == roomId) {
                viewModel.onIntent(GalleryIntent.MembersRefresh)
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is GallerySideEffect.NavigateToPhotoDetail -> onPhotoClick(effect.args)
                is GallerySideEffect.NavigateToChat -> onChatClick(effect.roomName)
                GallerySideEffect.NavigateToCamera -> {
                    shouldRefreshAfterCamera.value = true
                    onShootClick()
                }
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
            }
        }
    }

    GalleryScreen(
        modifier = Modifier.fillMaxSize(),
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick,
        onSettingClick = { onSettingClick(state.roomName) },
        onInviteCodeClick = { invitationCode ->
            coroutineScope.launch {
                if (clipboard.copyInviteCode(invitationCode)) {
                    // Android 13부터는 복사하면 시스템이 안내를 띄워 우리 토스트와 겹친다.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return@launch

                    snackbarHostState.showSnackbar(
                        ChallaToastVisuals(
                            message = inviteCodeCopySuccessMessage,
                            topOffset = ToastTopOffset,
                        ),
                    )
                } else {
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
        },
    )
}

/**
 * 초대 코드를 클립보드에 복사한다.
 *
 * @return 복사에 성공하면 true
 */
private suspend fun Clipboard.copyInviteCode(invitationCode: String): Boolean {
    // 방 정보를 받아야 메뉴가 열리므로, 코드가 비었다면 응답이 스펙과 다른 것이다.
    if (invitationCode.isBlank()) {
        Timber.w("초대 코드가 비어 있어 복사하지 않습니다.")
        return false
    }

    return runCatching {
        setClipEntry(ClipEntry(ClipData.newPlainText(INVITE_CODE_CLIP_LABEL, invitationCode)))
    }.onFailure { throwable ->
        if (throwable is CancellationException) throw throwable
        Timber.e(throwable, "초대 코드를 복사하지 못했습니다.")
    }.isSuccess
}
