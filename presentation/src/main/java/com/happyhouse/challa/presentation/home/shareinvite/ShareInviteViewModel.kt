package com.happyhouse.challa.presentation.home.shareinvite

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ShareInviteViewModel.Factory::class)
class ShareInviteViewModel
    @AssistedInject
    constructor(
        @Assisted("roomId") private val roomId: String,
        @Assisted("roomName") private val roomName: String,
    ) : BaseViewModel<ShareInviteState, ShareInviteIntent, ShareInviteSideEffect>(
            initialState = ShareInviteState(roomName = roomName),
        ) {
        init {
            loadInviteLink()
        }

        override fun onIntent(intent: ShareInviteIntent) {
            when (intent) {
                is ShareInviteIntent.KakaoShareClick -> shareKakao(intent.link)
            }
        }

        private fun loadInviteLink() {
            viewModelScope.launch {
                updateState { copy(isLoading = true) }
                try {
                    delay(1000L) // TODO JH: 초대 링크 발급 API 호출
                    val link = mockInviteLink(roomId)
                    updateState { copy(inviteLink = link, isLoading = false) }
                } catch (e: Exception) {
                    updateState { copy(isLoading = false) }
                }
            }
        }

        private fun shareKakao(link: String) {
            viewModelScope.launch {
                sendEffect(
                    ShareInviteSideEffect.KakaoShareRequested(
                        inviteLink = link,
                        roomName = roomName,
                    ),
                )
            }
        }

        private fun mockInviteLink(roomId: String): String {
            val code =
                roomId
                    .filter { it.isLetterOrDigit() }
                    .takeLast(INVITE_CODE_LENGTH)
                    .ifEmpty { "aB3kZ9" }
            return "https://chalna.app/r/$code"
        }

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted("roomId") roomId: String,
                @Assisted("roomName") roomName: String,
            ): ShareInviteViewModel
        }

        companion object {
            private const val INVITE_CODE_LENGTH = 6
        }
    }
