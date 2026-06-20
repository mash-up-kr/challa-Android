package com.happyhouse.challa.presentation.login

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
    @Inject
    constructor() :
    BaseViewModel<LoginState, LoginIntent, LoginSideEffect>(
            initialState = LoginState(isLoading = false),
        ) {
        override fun onIntent(intent: LoginIntent) {
            when (intent) {
                LoginIntent.ClickLogin -> handleLoginClick()
            }
        }

        private fun handleLoginClick() {
            if (currentState.isLoading) return
            viewModelScope.launch {
                updateState { copy(isLoading = true) }
                try {
                    delay(1000L) // TODO JH: API 호출
                    LoginSideEffect.LoginSuccess
                } catch (e: Exception) {
                    LoginSideEffect.LoginFailed
                }.also { sideEffect ->
                    sendEffect(sideEffect)
                }
                updateState { copy(isLoading = false) }
            }
        }
    }
