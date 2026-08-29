package com.happyhouse.challa.presentation.roomsetting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaBottomSheet
import com.happyhouse.challa.presentation.designsystem.component.ChallaInputBox
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaTextButton
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import com.happyhouse.challa.presentation.model.ROOM_NAME_MAX_LENGTH
import kotlinx.coroutines.launch

/**
 * 방 설정 화면에서 방 이름을 선택하면 뜨는 방 이름 수정 바텀시트.
 *
 * 변경을 누르면 시트는 열린 채로 버튼에 로딩을 돌리고, 저장이 끝난 뒤에야 닫힌다.
 * 로딩 중에는 버튼이 눌리지 않으므로 응답을 기다리는 동안 다시 제출되지 않는다.
 *
 * @param roomName 시트를 열 때 입력란에 채워지는 현재 방 이름.
 * @param isSubmitting 저장 요청이 진행 중인지. 버튼 로딩과 닫히는 시점을 결정한다.
 * @param onDismiss 시트를 닫아 사라지게 할 때 호출.
 * @param onConfirm 변경 버튼을 눌렀을 때 앞뒤 공백을 제거한 새 방 이름과 함께 호출.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoomNameBottomSheet(
    roomName: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var name by rememberSaveable(roomName) { mutableStateOf(roomName) }
    var awaitingResult by rememberSaveable { mutableStateOf(false) }

    // 스크림/뒤로가기 외의 경로(닫기 아이콘)로 닫을 때 내려가는 애니메이션을 태운 뒤 실제 콜백을 실행한다.
    fun hideThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) action()
        }
    }

    // 저장이 끝나면(성공·실패 모두) 시트가 스스로 내려간다. 닫는 경로를 onDismiss 하나로 두어야
    // 시트만 내려가고 상태는 열린 채로 남는 조합이 생기지 않는다.
    // 요청이 시작된 걸 실제로 본 뒤에만 닫으므로, 로딩이 올라오기 전 프레임에 닫혀버리지 않는다.
    LaunchedEffect(isSubmitting) {
        when {
            isSubmitting -> awaitingResult = true
            awaitingResult -> {
                sheetState.hide()
                onDismiss()
            }
        }
    }

    ChallaBottomSheet(
        title = stringResource(id = R.string.edit_room_name_title),
        onDismissRequest = onDismiss,
        modifier = modifier.imePadding(),
        sheetState = sheetState,
        icon = {
            Icon(
                painter = painterResource(id = ChallaIcons.Close),
                contentDescription = stringResource(id = R.string.edit_room_name_close_description),
                tint = ChallaTheme.colors.labelNormal,
                modifier =
                    Modifier
                        .size(24.dp)
                        .noRippleClickOnce { hideThen(onDismiss) },
            )
        },
    ) {
        EditRoomNameSheetBody(
            name = name,
            isSubmitting = isSubmitting,
            onNameChange = { name = it.take(ROOM_NAME_MAX_LENGTH) },
            onSubmit = {
                val newRoomName = name.trim()
                // 바뀐 게 없으면 저장할 것도 없으니 그냥 닫는다.
                if (newRoomName.isEmpty() || newRoomName == roomName) {
                    hideThen(onDismiss)
                } else {
                    onConfirm(newRoomName)
                }
            },
        )
    }
}

@Composable
private fun EditRoomNameSheetBody(
    name: String,
    onNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    isSubmitting: Boolean = false,
) {
    val canSubmit = name.isNotBlank()

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
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            ChallaInputBox(
                value = name,
                onValueChange = onNameChange,
                placeholder = stringResource(id = R.string.edit_room_name_placeholder),
                // 버튼과 달리 키보드 완료는 로딩 여부를 모르므로 여기서 막는다.
                onDone = { if (canSubmit && !isSubmitting) onSubmit() },
            )
            ChallaTextButton(
                text = stringResource(id = R.string.edit_room_name_submit),
                onClick = onSubmit,
                enabled = canSubmit,
                loading = isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, name = "EditRoomName - Filled")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun EditRoomNameSheetBodyFilledPreview() {
    ChallaTheme {
        Box(modifier = Modifier.background(ChallaTheme.colors.backgroundLevel1).padding(16.dp)) {
            EditRoomNameSheetBody(
                name = "친구들과 유럽 여행",
                onNameChange = {},
                onSubmit = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "EditRoomName - Submitting")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun EditRoomNameSheetBodySubmittingPreview() {
    ChallaTheme {
        Box(modifier = Modifier.background(ChallaTheme.colors.backgroundLevel1).padding(16.dp)) {
            EditRoomNameSheetBody(
                name = "친구들과 유럽 여행",
                isSubmitting = true,
                onNameChange = {},
                onSubmit = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "EditRoomName - Empty")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun EditRoomNameSheetBodyEmptyPreview() {
    ChallaTheme {
        Box(modifier = Modifier.background(ChallaTheme.colors.backgroundLevel1).padding(16.dp)) {
            EditRoomNameSheetBody(
                name = "",
                onNameChange = {},
                onSubmit = {},
            )
        }
    }
}
