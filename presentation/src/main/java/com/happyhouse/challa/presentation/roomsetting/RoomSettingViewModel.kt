package com.happyhouse.challa.presentation.roomsetting

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.result.causeOrNull
import com.happyhouse.challa.domain.result.onFailure
import com.happyhouse.challa.domain.result.onSuccess
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.roomsetting.contract.RoomSettingIntent
import com.happyhouse.challa.presentation.roomsetting.contract.RoomSettingSideEffect
import com.happyhouse.challa.presentation.roomsetting.contract.RoomSettingState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = RoomSettingViewModel.Factory::class)
class RoomSettingViewModel @AssistedInject constructor(
    @Assisted private val roomId: Long,
    @Assisted roomName: String,
    private val roomRepository: RoomRepository,
) : BaseViewModel<RoomSettingState, RoomSettingIntent, RoomSettingSideEffect>(
        initialState = RoomSettingState(roomName = roomName),
    ) {
    override fun onIntent(intent: RoomSettingIntent) {
        when (intent) {
            RoomSettingIntent.RoomNameClick ->
                updateState { copy(isEditRoomNameSheetVisible = true) }

            RoomSettingIntent.EditRoomNameSheetDismiss ->
                updateState { copy(isEditRoomNameSheetVisible = false) }

            is RoomSettingIntent.RoomNameSubmit -> updateRoomName(intent.roomName)
        }
    }

    /**
     * 새 이름을 서버에 저장한다.
     *
     * 저장하는 동안 시트는 열린 채로 버튼에 로딩을 돌리고, 저장이 끝나면 시트가 스스로 닫으며
     * [RoomSettingIntent.EditRoomNameSheetDismiss]를 보낸다. 여기서 시트를 닫지 않는 이유는
     * 닫는 경로를 하나로 두어 상태와 실제 시트가 어긋나지 않게 하기 위함이다.
     *
     * 목록에 보여줄 이름은 저장에 성공한 뒤에만 바꾼다. 실패하면 이전 이름이 남는다.
     */
    private fun updateRoomName(newRoomName: String) {
        if (currentState.isSubmitting) return
        if (newRoomName.isEmpty() || newRoomName == currentState.roomName) return

        // 시트가 로딩 여부를 보고 닫힐 시점을 정하므로, 요청을 띄우기 전에 동기적으로 올린다.
        updateState { copy(isSubmitting = true) }
        viewModelScope.launch {
            roomRepository
                .updateRoomTitle(roomId = roomId, title = newRoomName)
                .onSuccess {
                    updateState { copy(roomName = newRoomName, isSubmitting = false) }
                }.onFailure { failure ->
                    Timber.e(failure.causeOrNull(), "방 이름을 변경하지 못했습니다. roomId=$roomId")
                    updateState { copy(isSubmitting = false) }
                    sendEffect(RoomSettingSideEffect.RoomNameUpdateFailed)
                }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            roomId: Long,
            roomName: String,
        ): RoomSettingViewModel
    }
}
