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
import kotlinx.coroutines.launch

/**
 * 방 설정 화면에서 방 이름을 선택하면 뜨는 방 이름 수정 바텀시트.
 *
 * @param roomName 시트를 열 때 입력란에 채워지는 현재 방 이름.
 * @param onDismiss 시트를 닫아 사라지게 할 때 호출.
 * @param onConfirm 변경 버튼을 눌렀을 때 앞뒤 공백을 제거한 새 방 이름과 함께 호출.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoomNameBottomSheet(
    roomName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var name by rememberSaveable(roomName) { mutableStateOf(roomName) }

    // 스크림/뒤로가기 외의 경로(닫기 아이콘·변경 완료)로 닫을 때 내려가는 애니메이션을 태운 뒤 실제 콜백을 실행한다.
    fun hideThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) action()
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
            onNameChange = { name = it },
            onSubmit = { hideThen { onConfirm(name.trim()) } },
        )
    }
}

@Composable
private fun EditRoomNameSheetBody(
    name: String,
    onNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
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
                onDone = { if (canSubmit) onSubmit() },
            )
            ChallaTextButton(
                text = stringResource(id = R.string.edit_room_name_submit),
                onClick = onSubmit,
                enabled = canSubmit,
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
