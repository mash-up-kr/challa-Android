package com.happyhouse.challa.presentation.setting

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.repository.ThemeRepository
import com.happyhouse.challa.domain.repository.UserRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.setting.contract.SettingIntent
import com.happyhouse.challa.presentation.setting.contract.SettingSideEffect
import com.happyhouse.challa.presentation.setting.contract.SettingState
import com.happyhouse.challa.presentation.setting.theme.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel
    @Inject
    constructor(
        private val themeRepository: ThemeRepository,
        private val userRepository: UserRepository,
    ) : BaseViewModel<SettingState, SettingIntent, SettingSideEffect>(
            initialState = SettingState(email = "abc@email.com"),
        ) {
        private var profileReadJob: Job? = null
        private var themeReadJob: Job? = null

        init {
            onIntent(SettingIntent.FetchData)
        }

        override fun onIntent(intent: SettingIntent) {
            when (intent) {
                SettingIntent.FetchData -> fetchData()
                SettingIntent.ProfileReadRetry -> fetchMyProfile()
                SettingIntent.ThemeReadRetry -> themeRepository.retryPrimaryThemeRead()
            }
        }

        private fun fetchData() {
            fetchMyProfile()
            fetchPrimaryTheme()
        }

        private fun fetchMyProfile() {
            if (profileReadJob?.isActive == true) return

            profileReadJob =
                viewModelScope.launch {
                    updateState { copy(isProfileLoading = true) }

                    when (val result = userRepository.getMyProfile()) {
                        is ChallaResult.Success ->
                            updateState {
                                copy(
                                    nickname = result.data.nickname.orEmpty(),
                                    profileImageUrl = result.data.profileImageUrl,
                                    isProfileLoading = false,
                                )
                            }

                        is ChallaResult.Failure -> {
                            updateState { copy(isProfileLoading = false) }
                            sendEffect(SettingSideEffect.ProfileReadFailed)
                        }
                    }
                }
        }

        private fun fetchPrimaryTheme() {
            if (themeReadJob?.isActive == true) {
                themeRepository.retryPrimaryThemeRead()
                return
            }

            themeReadJob =
                viewModelScope.launch {
                    themeRepository.primaryTheme.collect { result ->
                        when (result) {
                            is ChallaResult.Success ->
                                updateState { copy(primaryTheme = result.data.toUiModel()) }

                            is ChallaResult.Failure -> sendEffect(SettingSideEffect.ThemeReadFailed)
                        }
                    }
                }
        }
    }
