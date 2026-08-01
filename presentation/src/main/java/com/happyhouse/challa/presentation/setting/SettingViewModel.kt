package com.happyhouse.challa.presentation.setting

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.repository.ThemeRepository
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
            initialState = SettingState(),
        ) {
        init {
            viewModelScope.launch {
                themeRepository.primaryTheme.collect { theme ->
                    updateState { copy(primaryTheme = theme.toUiModel()) }
                }
            }
        }

        override fun onIntent(intent: SettingIntent) = Unit
    }
