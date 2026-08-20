package com.happyhouse.challa.presentation.setting.account

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.repository.AuthRepository
import com.happyhouse.challa.domain.repository.UserRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.setting.account.contract.AccountIntent
import com.happyhouse.challa.presentation.setting.account.contract.AccountSideEffect
import com.happyhouse.challa.presentation.setting.account.contract.AccountState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : BaseViewModel<AccountState, AccountIntent, AccountSideEffect>(
        initialState =
            userRepository.profile.value?.let { profile ->
                AccountState(
                    nickname = profile.nickname.orEmpty(),
                    profileImageUrl = profile.profileImageUrl,
                )
            } ?: AccountState(),
    ) {
    init {
        observeProfile()
        fetchMyProfile()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            userRepository.profile.collect { profile ->
                updateState {
                    copy(
                        nickname = profile?.nickname.orEmpty(),
                        profileImageUrl = profile?.profileImageUrl,
                    )
                }
            }
        }
    }

    override fun onIntent(intent: AccountIntent) {
        when (intent) {
            AccountIntent.ProfileReadRetry -> fetchMyProfile(forceRefresh = true)
            AccountIntent.LogoutClick -> handleLogoutClick()
            AccountIntent.WithdrawalConfirmClick -> handleWithdrawalConfirmClick()
        }
    }

    private fun fetchMyProfile(forceRefresh: Boolean = false) {
        if (!forceRefresh && userRepository.profile.value != null) return

        viewModelScope.launch {
            when (val result = userRepository.getMyProfile()) {
                is ChallaResult.Success -> Unit
                is ChallaResult.Failure -> sendEffect(AccountSideEffect.ProfileReadFailed)
            }
        }
    }

    private fun handleLogoutClick() {
        if (currentState.isProcessing) return

        updateState { copy(isLoggingOut = true) }

        viewModelScope.launch {
            when (authRepository.logout()) {
                is ChallaResult.Success -> sendEffect(AccountSideEffect.LogoutSuccess)
                is ChallaResult.Failure -> sendEffect(AccountSideEffect.LogoutFailed)
            }

            updateState { copy(isLoggingOut = false) }
        }
    }

    private fun handleWithdrawalConfirmClick() {
        if (currentState.isProcessing) return

        updateState { copy(isWithdrawing = true) }

        viewModelScope.launch {
            when (userRepository.withdraw()) {
                is ChallaResult.Success -> sendEffect(AccountSideEffect.WithdrawalSuccess)
                is ChallaResult.Failure -> sendEffect(AccountSideEffect.WithdrawalFailed)
            }

            updateState { copy(isWithdrawing = false) }
        }
    }
}
