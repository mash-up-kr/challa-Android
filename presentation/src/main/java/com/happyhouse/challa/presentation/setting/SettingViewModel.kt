package com.happyhouse.challa.presentation.setting

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.repository.ThemeRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.setting.contract.SettingIntent
import com.happyhouse.challa.presentation.setting.contract.SettingSideEffect
import com.happyhouse.challa.presentation.setting.contract.SettingState
import com.happyhouse.challa.presentation.setting.theme.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel
    @Inject
    constructor(
        private val themeRepository: ThemeRepository,
    ) : BaseViewModel<SettingState, SettingIntent, SettingSideEffect>(
            initialState =
                SettingState(
                    nickname = "나는야멋쟁이토마토",
                    maskedEmail = "juy***@naver.com",
                ),
            // TODO BJ: 실제 api 추가되면 수정
        ) {
        init {
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

        override fun onIntent(intent: SettingIntent) {
            when (intent) {
                SettingIntent.ThemeReadRetry -> themeRepository.retryPrimaryThemeRead()
            }
        }
    }
