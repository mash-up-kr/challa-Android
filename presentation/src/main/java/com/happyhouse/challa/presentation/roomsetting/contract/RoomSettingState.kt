package com.happyhouse.challa.presentation.roomsetting.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState

/**
 * @param roomName 목록에 보여줄 방 이름. 서버에 저장된 뒤에만 바뀐다.
 * @param isSubmitting 방 이름 수정 요청이 진행 중인지. 시트의 변경 버튼에 로딩을 돌려 재요청을 막고,
 *  요청이 끝나면 시트가 닫히는 신호가 된다.
 */
@Immutable
data class RoomSettingState(
    val roomName: String = "",
    val isEditRoomNameSheetVisible: Boolean = false,
    val isSubmitting: Boolean = false,
) : UiState
