package com.happyhouse.challa.presentation.profile

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.repository.ImageUploadRepository
import com.happyhouse.challa.domain.repository.UserRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.onFailure
import com.happyhouse.challa.domain.result.onSuccess
import com.happyhouse.challa.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

const val NICKNAME_MAX_LENGTH = 10

// 완료 화면을 잠시 보여준 뒤 다음 화면으로 이동하기까지의 지연 시간
private const val PROFILE_COMPLETED_NAVIGATE_DELAY_MS = 2000L

@HiltViewModel
class CreateProfileViewModel
    @Inject
    constructor(
        private val userRepository: UserRepository,
        private val imageUploadRepository: ImageUploadRepository,
    ) : BaseViewModel<CreateProfileState, CreateProfileIntent, CreateProfileSideEffect>(
            initialState = CreateProfileState(),
        ) {
        override fun onIntent(intent: CreateProfileIntent) {
            when (intent) {
                is CreateProfileIntent.NicknameChanged -> onNicknameChanged(intent.nickname)
                is CreateProfileIntent.ProfileImageSelected -> onProfileImageSelected(intent.uri)
                CreateProfileIntent.ProfileImageDeleteClick -> onProfileImageDeleted()
                CreateProfileIntent.DoneClick -> createProfile()
            }
        }

        private fun onNicknameChanged(nickname: String) {
            val isLengthExceeded = nickname.length > NICKNAME_MAX_LENGTH
            if (isLengthExceeded) {
                viewModelScope.launch {
                    sendEffect(CreateProfileSideEffect.NicknameLengthExceeded)
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

        private fun createProfile() {
            if (!currentState.canSubmit) return
            viewModelScope.launch {
                updateState { copy(isSubmitting = true) }

                // 선택한 이미지가 있으면 먼저 업로드해 공개 URL 을 확보한다. 업로드에 실패하면 프로필 저장으로 넘어가지 않는다.
                val profileImageUrl =
                    currentState.profileImageUri?.let { uri ->
                        when (val result = imageUploadRepository.uploadProfileImage(uri)) {
                            is ChallaResult.Success -> result.data
                            is ChallaResult.Failure -> {
                                updateState { copy(isSubmitting = false) }
                                sendEffect(CreateProfileSideEffect.ProfileCreateFailed)
                                return@launch
                            }
                        }
                    }

                userRepository
                    .updateProfile(
                        nickname = currentState.nickname.trim(),
                        profileImageUrl = profileImageUrl,
                    ).onSuccess { profile ->
                        updateState { copy(isSubmitting = false, isCompleted = true) }
                        delay(PROFILE_COMPLETED_NAVIGATE_DELAY_MS)
                        sendEffect(CreateProfileSideEffect.ProfileCreated(profile.nickname.orEmpty()))
                    }.onFailure {
                        updateState { copy(isSubmitting = false) }
                        sendEffect(CreateProfileSideEffect.ProfileCreateFailed)
                    }
            }
        }
    }
