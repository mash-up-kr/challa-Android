package com.happyhouse.challa.presentation.login

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.repository.AuthRepository
import com.happyhouse.challa.domain.result.onFailure
import com.happyhouse.challa.domain.result.onSuccess
import com.happyhouse.challa.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) : BaseViewModel<LoginState, LoginIntent, LoginSideEffect>(
            initialState = LoginState(isLoading = false),
        ) {
        override fun onIntent(intent: LoginIntent) {
            when (intent) {
                is LoginIntent.LoginClick -> handleLoginClick(intent.acquireKakaoIdToken)
            }
        }

        private fun handleLoginClick(acquireKakaoIdToken: suspend () -> String) {
            if (currentState.isLoading) return
            viewModelScope.launch {
                updateState { copy(isLoading = true) }
                try {
                    val idToken = acquireKakaoIdToken()
                    authRepository
                        .loginWithKakao(idToken)
                        .onSuccess { sendEffect(LoginSideEffect.LoginSuccess(it.isNewUser)) }
                        .onFailure { sendEffect(LoginSideEffect.LoginFailed) }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    sendEffect(LoginSideEffect.LoginFailed)
                }
                updateState { copy(isLoading = false) }
            }
        }
    }
