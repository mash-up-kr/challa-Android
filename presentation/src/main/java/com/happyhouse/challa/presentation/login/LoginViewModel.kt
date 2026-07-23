package com.happyhouse.challa.presentation.login

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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
                is LoginIntent.LoginClick -> handleLoginClick(intent.acquireKakaoToken)
            }
        }

        private fun handleLoginClick(acquireKakaoToken: suspend () -> String) {
            if (currentState.isLoading) return
            viewModelScope.launch {
                updateState { copy(isLoading = true) }
                try {
                    // TODO JH: API 연동
                    val kakaoAccessToken = acquireKakaoToken()
                    sendEffect(LoginSideEffect.LoginSuccess)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    sendEffect(LoginSideEffect.LoginFailed)
                }
                updateState { copy(isLoading = false) }
            }
        }
    }
