package com.happyhouse.challa.presentation.profile

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.repository.ImageUploadRepository
import com.happyhouse.challa.domain.repository.UserRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.onFailure
import com.happyhouse.challa.domain.result.onSuccess
import com.happyhouse.challa.presentation.base.BaseViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val NICKNAME_MAX_LENGTH = 10

// 완료 화면을 잠시 보여준 뒤 홈으로 넘어가는 애니메이션을 시작하기까지의 지연 시간
private const val PROFILE_COMPLETED_NAVIGATE_DELAY_MS = 2000L

/**
 * 완료 화면이 홈과 같은 모습으로 바뀌는 애니메이션 시간.
 *
 * 이 애니메이션이 끝난 시점에 두 화면의 모습이 같아지므로, 그때 홈으로 이동해야 전환이 이어져 보인다.
 */
internal const val PROFILE_ENTER_HOME_DURATION_MS = 600

@HiltViewModel(assistedFactory = SettingProfileViewModel.Factory::class)
class SettingProfileViewModel @AssistedInject constructor(
    @Assisted mode: ProfileSettingMode,
    @Assisted("nickname") nickname: String,
    @Assisted("profileImageUrl") private val initialProfileImageUrl: String?,
    private val userRepository: UserRepository,
    private val imageUploadRepository: ImageUploadRepository,
) : BaseViewModel<SettingProfileState, SettingProfileIntent, SettingProfileSideEffect>(
        initialState =
            SettingProfileState(
                mode = mode,
                nickname = nickname,
                profileImageUri = initialProfileImageUrl,
            ),
    ) {
    override fun onIntent(intent: SettingProfileIntent) {
        when (intent) {
            is SettingProfileIntent.NicknameChanged -> onNicknameChanged(intent.nickname)
            is SettingProfileIntent.ProfileImageSelected -> onProfileImageSelected(intent.uri)
            SettingProfileIntent.ProfileImageDeleteClick -> onProfileImageDeleted()
            SettingProfileIntent.DoneClick -> saveProfile()
        }
    }

    private fun onNicknameChanged(nickname: String) {
        val isLengthExceeded = nickname.length > NICKNAME_MAX_LENGTH
        if (isLengthExceeded) {
            viewModelScope.launch {
                sendEffect(SettingProfileSideEffect.NicknameLengthExceeded)
            }
        }
        val truncated = nickname.take(NICKNAME_MAX_LENGTH)
        updateState { copy(nickname = truncated, isNicknameLengthExceeded = isLengthExceeded) }
    }

    private fun onProfileImageSelected(uri: String) {
        updateState { copy(profileImageUri = uri) }
    }

    private fun onProfileImageDeleted() {
        updateState { copy(profileImageUri = null) }
    }

    private fun saveProfile() {
        if (!currentState.canSubmit) return
        viewModelScope.launch {
            updateState { copy(isSubmitting = true) }

            val isProfileImageChanged = currentState.profileImageUri != initialProfileImageUrl
            val profileImageUrl =
                if (isProfileImageChanged) {
                    currentState.profileImageUri?.let { uri ->
                        when (val result = imageUploadRepository.uploadProfileImage(uri)) {
                            is ChallaResult.Success -> result.data
                            is ChallaResult.Failure -> {
                                handleSaveFailure()
                                return@launch
                            }
                        }
                    }
                } else {
                    currentState.profileImageUri
                }

            userRepository
                .updateProfile(
                    nickname = currentState.nickname,
                    profileImageUrl = profileImageUrl,
                ).onSuccess {
                    if (currentState.mode == ProfileSettingMode.EDIT) {
                        updateState { copy(isSubmitting = false) }
                        sendEffect(SettingProfileSideEffect.ProfileUpdated)
                    } else {
                        updateState { copy(isSubmitting = false, isCompleted = true) }
                        delay(PROFILE_COMPLETED_NAVIGATE_DELAY_MS)
                        updateState { copy(isEnteringHome = true) }
                        delay(PROFILE_ENTER_HOME_DURATION_MS.toLong())
                        sendEffect(SettingProfileSideEffect.ProfileCreated(currentState.nickname))
                    }
                }.onFailure {
                    handleSaveFailure()
                }
        }
    }

    private suspend fun handleSaveFailure() {
        updateState { copy(isSubmitting = false) }
        sendEffect(
            if (currentState.mode == ProfileSettingMode.EDIT) {
                SettingProfileSideEffect.ProfileUpdateFailed
            } else {
                SettingProfileSideEffect.ProfileCreateFailed
            },
        )
    }

    @AssistedFactory
    interface Factory {
        fun create(
            mode: ProfileSettingMode,
            @Assisted("nickname") nickname: String,
            @Assisted("profileImageUrl") profileImageUrl: String?,
        ): SettingProfileViewModel
    }
}
