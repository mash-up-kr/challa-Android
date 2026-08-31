package com.happyhouse.challa.presentation.home.enterroom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaBottomSheet
import com.happyhouse.challa.presentation.designsystem.component.ChallaInputBox
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaTextButton
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarHost
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaToastVisuals
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import kotlinx.coroutines.launch

/**
 * 홈에서 방 입장하기를 선택하면 뜨는 방 입장 바텀시트.
 *
 * @param onDismiss 시트를 닫아 사라지게 할 때 호출.
 * @param onRoomEntered 방 입장이 완료됐을 때 참여한 roomId와 함께 호출. 방 상세 화면으로의 이동은 호출부에서 처리한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterRoomBottomSheet(
    onDismiss: () -> Unit,
    onRoomEntered: (roomId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EnterRoomViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val destructiveTint = ChallaTheme.colors.statusDestructive
    val roomEnterFailedMessage = stringResource(R.string.enter_room_failed)

    // 스크림/뒤로가기 외의 경로(닫기 아이콘·방 입장 완료)로 닫을 때 내려가는 애니메이션을 태운 뒤 실제 콜백을 실행한다.
    fun hideThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) action()
        }
    }

    fun showToast(message: String) {
        snackbarHostState.currentSnackbarData?.dismiss()
        scope.launch {
            snackbarHostState.showSnackbar(
                ChallaToastVisuals(
                    message = message,
                    icon = ChallaIcons.Error,
                    iconTint = destructiveTint,
                ),
            )
        }
    }

    // 시트가 열릴 때마다 이전에 입력한 값이 남지 않도록 초기화한다. (ViewModel이 홈 화면 스코프로 유지되기 때문)
    LaunchedEffect(Unit) {
        viewModel.onIntent(EnterRoomIntent.Reset)
    }

    // 시트가 사라질 때(스크림/뒤로가기 포함) 진행 중이던 방 입장 코루틴을 취소한다.
    // 취소하지 않으면 수신자가 없는 사이 발행된 RoomEntered 이펙트가 채널에 걸려 있다가
    // 시트를 다시 열 때 뒤늦게 소비되어 의도치 않게 방 상세로 이동할 수 있다.
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.onIntent(EnterRoomIntent.Reset)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is EnterRoomSideEffect.RoomEntered ->
                    hideThen { onRoomEntered(effect.roomId) }

                is EnterRoomSideEffect.RoomEnterFailed -> {
                    val message =
                        effect.message?.takeIf { it.isNotBlank() }
                            ?: roomEnterFailedMessage
                    showToast(message)
                }
            }
        }
    }

    ChallaBottomSheet(
        title = stringResource(id = R.string.enter_room_title),
        onDismissRequest = onDismiss,
        modifier = modifier.imePadding(),
        sheetState = sheetState,
        icon = {
            Icon(
                painter = painterResource(id = ChallaIcons.Close),
                contentDescription = stringResource(id = R.string.enter_room_close_description),
                tint = ChallaTheme.colors.labelNormal,
                modifier =
                    Modifier
                        .size(24.dp)
                        .noRippleClickOnce { hideThen(onDismiss) },
            )
        },
    ) {
        Box {
            EnterRoomSheetBody(
                state = state,
                onIntent = viewModel::onIntent,
            )
            ChallaSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@Composable
private fun EnterRoomSheetBody(
    state: EnterRoomState,
    onIntent: (EnterRoomIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 1.dp,
            color = ChallaTheme.colors.lineNeutral,
        )
        Column(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ChallaInputBox(
                value = state.code,
                onValueChange = { onIntent(EnterRoomIntent.CodeChanged(it)) },
                placeholder = stringResource(id = R.string.enter_room_code_placeholder),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                onDone = { if (state.canSubmit) onIntent(EnterRoomIntent.EnterClick) },
            )
            ChallaTextButton(
                text = stringResource(id = R.string.enter_room_submit),
                onClick = { onIntent(EnterRoomIntent.EnterClick) },
                enabled = state.canSubmit,
                loading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, name = "EnterRoom - Empty")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun EnterRoomSheetBodyEmptyPreview() {
    ChallaTheme {
        Box(modifier = Modifier.background(ChallaTheme.colors.backgroundLevel1).padding(16.dp)) {
            EnterRoomSheetBody(
                state = EnterRoomState(code = ""),
                onIntent = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "EnterRoom - Filled")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun EnterRoomSheetBodyFilledPreview() {
    ChallaTheme {
        Box(modifier = Modifier.background(ChallaTheme.colors.backgroundLevel1).padding(16.dp)) {
            EnterRoomSheetBody(
                state = EnterRoomState(code = "190329"),
                onIntent = {},
            )
        }
    }
}
