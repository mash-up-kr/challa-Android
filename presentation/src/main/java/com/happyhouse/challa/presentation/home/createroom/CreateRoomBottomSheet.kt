package com.happyhouse.challa.presentation.home.createroom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
 * 홈에서 방 만들기를 선택하면 뜨는 방 생성 바텀시트.
 *
 * @param onDismiss 시트를 닫아 사라지게 할 때 호출.
 * @param onRoomCreated 방 생성이 완료됐을 때 (roomId, roomName)과 함께 호출. 방 상세 화면으로의 이동은 호출부에서 처리한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomBottomSheet(
    onDismiss: () -> Unit,
    onRoomCreated: (roomId: Long, roomName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateRoomViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val destructiveTint = ChallaTheme.colors.statusDestructive

    // 스크림/뒤로가기 외의 경로(닫기 아이콘·방 생성 완료)로 닫을 때 내려가는 애니메이션을 태운 뒤 실제 콜백을 실행한다.
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
        viewModel.onIntent(CreateRoomIntent.Reset)
    }

    // 시트가 사라질 때(스크림/뒤로가기 포함) 진행 중이던 방 생성 코루틴을 취소한다.
    // 취소하지 않으면 수신자가 없는 사이 발행된 RoomCreated 이펙트가 채널에 걸려 있다가
    // 시트를 다시 열 때 뒤늦게 소비되어 의도치 않게 방 상세로 이동할 수 있다.
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.onIntent(CreateRoomIntent.Reset)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is CreateRoomSideEffect.RoomCreated ->
                    hideThen { onRoomCreated(effect.roomId, effect.roomName) }

                is CreateRoomSideEffect.RoomCreateFailed -> {
                    val message =
                        effect.message?.takeIf { it.isNotBlank() }
                            ?: context.getString(R.string.create_room_failed)
                    showToast(message)
                }
            }
        }
    }

    ChallaBottomSheet(
        title = stringResource(id = R.string.create_room_title),
        onDismissRequest = onDismiss,
        modifier = modifier.imePadding(),
        sheetState = sheetState,
        icon = {
            Icon(
                painter = painterResource(id = ChallaIcons.Close),
                contentDescription = stringResource(id = R.string.create_room_close_description),
                tint = ChallaTheme.colors.labelNormal,
                modifier =
                    Modifier
                        .size(24.dp)
                        .noRippleClickOnce { hideThen(onDismiss) },
            )
        },
    ) {
        Box {
            CreateRoomSheetBody(
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
private fun CreateRoomSheetBody(
    state: CreateRoomState,
    onIntent: (CreateRoomIntent) -> Unit,
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
            NameField(
                name = state.name,
                onNameChange = { onIntent(CreateRoomIntent.NameChanged(it)) },
            )
            ShotCountField(
                selected = state.shotCount,
                onShotCountChange = { onIntent(CreateRoomIntent.ShotCountChanged(it)) },
            )
            ChallaTextButton(
                text = stringResource(id = R.string.create_room_submit),
                onClick = { onIntent(CreateRoomIntent.CreateClick) },
                enabled = state.canSubmit,
                loading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun NameField(
    name: String,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ChallaInputBox(
            value = name,
            onValueChange = onNameChange,
            placeholder = stringResource(id = R.string.create_room_name_placeholder),
        )
    }
}

@Composable
private fun ShotCountField(
    selected: ShotCount,
    onShotCountChange: (ShotCount) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(id = R.string.create_room_shot_count_label),
            color = ChallaTheme.colors.labelAlternative,
            style = ChallaTheme.typography.bodyXSmall.medium,
            textAlign = TextAlign.Center,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShotCount.entries.forEach { shotCount ->
                ShotCountOption(
                    shotCount = shotCount,
                    selected = selected == shotCount,
                    onClick = { onShotCountChange(shotCount) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ShotCountOption(
    shotCount: ShotCount,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier =
            modifier
                .clip(shape)
                .background(if (selected) ChallaTheme.colors.backgroundLevel4 else ChallaTheme.colors.backgroundLevel2)
                .then(
                    if (selected) {
                        Modifier.border(width = 1.5.dp, color = ChallaTheme.colors.lineNormal, shape = shape)
                    } else {
                        Modifier
                    },
                )
                .noRippleClickOnce(onClick = onClick)
                .padding(vertical = 16.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(id = R.string.create_room_shot_count_option, shotCount.count),
            color = if (selected) ChallaTheme.colors.labelNormal else ChallaTheme.colors.labelNeutral,
            textAlign = TextAlign.Center,
            style = ChallaTheme.typography.bodyMedium.bold,
        )
    }
}

@Preview(showBackground = true, name = "CreateRoom - Empty")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CreateRoomSheetBodyEmptyPreview() {
    ChallaTheme {
        Box(modifier = Modifier.background(ChallaTheme.colors.backgroundLevel1).padding(16.dp)) {
            CreateRoomSheetBody(
                state = CreateRoomState(name = ""),
                onIntent = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "CreateRoom - Filled")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CreateRoomSheetBodyFilledPreview() {
    ChallaTheme {
        Box(modifier = Modifier.background(ChallaTheme.colors.backgroundLevel1).padding(16.dp)) {
            CreateRoomSheetBody(
                state = CreateRoomState(name = "오사카 졸업여행"),
                onIntent = {},
            )
        }
    }
}
