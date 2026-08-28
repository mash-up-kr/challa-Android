package com.happyhouse.challa.presentation.profile

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState

@Immutable
data class SettingProfileState(
    val mode: ProfileSettingMode = ProfileSettingMode.CREATE,
    val nickname: String = "",
    val profileImageUri: String? = null,
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false,
    /** 완료 화면을 다 보여주고 홈과 같은 모습으로 바뀌는 중인지 */
    val isEnteringHome: Boolean = false,
    val isNicknameLengthExceeded: Boolean = false,
) : UiState {
    val canSubmit: Boolean
        get() = nickname.trim().isNotEmpty() && !isSubmitting && !isCompleted
}

enum class ProfileSettingMode {
    CREATE,
    EDIT,
}
