package com.happyhouse.challa.presentation.invite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.result.onFailure
import com.happyhouse.challa.domain.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 초대 링크로 들어온 방 입장을 앱 화면 범위에서 처리한다.
 *
 * 받은 초대 코드는 보관하지 않는다. 로그인 전처럼 지금 입장할 수 없는 상태에서 들어온 초대는 그대로 흘려보내고
 * 평소대로 로그인 화면을 보여주며, 방에 들어오려면 사용자가 초대 링크를 다시 타고 들어오면 된다.
 *
 * 특정 화면이 아닌 Activity 범위에 살면서, 링크를 받는 `MainActivity` 와 입장 가능 여부를 판단하고 입장 결과로
 * 화면을 옮기는 `ChallaNavHost` 가 같은 인스턴스를 공유한다.
 */
@HiltViewModel
class RoomInviteViewModel
    @Inject
    constructor(
        private val roomRepository: RoomRepository,
    ) : ViewModel() {
        private val _invitations = Channel<String>()

        /** 링크로 들어온 초대 코드. 지금 입장할 수 있는 화면일 때만 [enterRoom] 으로 이어진다. */
        val invitations: Flow<String> = _invitations.receiveAsFlow()

        private val _events = Channel<RoomInviteEvent>()
        val events: Flow<RoomInviteEvent> = _events.receiveAsFlow()

        private var enterRoomJob: Job? = null

        /** 초대 링크로 앱에 들어왔을 때 호출한다. */
        fun onInvitationReceived(invitationCode: String) {
            viewModelScope.launch {
                _invitations.send(invitationCode)
            }
        }

        /** 초대 코드로 방에 입장한다. */
        fun enterRoom(invitationCode: String) {
            if (enterRoomJob?.isActive == true) return

            enterRoomJob =
                viewModelScope.launch {
                    roomRepository
                        .enterRoom(code = invitationCode)
                        .onSuccess { entered ->
                            _events.send(RoomInviteEvent.RoomEntered(roomId = entered.id))
                        }.onFailure {
                            _events.send(RoomInviteEvent.RoomEnterFailed)
                        }
                }
        }
    }
