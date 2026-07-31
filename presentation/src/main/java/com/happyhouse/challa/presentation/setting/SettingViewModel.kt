package com.happyhouse.challa.presentation.setting

import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.setting.contract.SettingIntent
import com.happyhouse.challa.presentation.setting.contract.SettingSideEffect
import com.happyhouse.challa.presentation.setting.contract.SettingState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor() :
    BaseViewModel<SettingState, SettingIntent, SettingSideEffect>(
        initialState = SettingState(),
    ) {
        override fun onIntent(intent: SettingIntent) = Unit
    }
