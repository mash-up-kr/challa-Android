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
) : BaseViewModel<AccountState, AccountIntent, AccountSideEffect>(initialState = AccountState()) {
    override fun onIntent(intent: AccountIntent) {
        when (intent) {
            AccountIntent.LogoutClick -> handleLogoutClick()
            AccountIntent.WithdrawalConfirmClick -> handleWithdrawalConfirmClick()
        }
    }

    private fun handleLogoutClick() {
        if (currentState.isLoggingOut) return

        viewModelScope.launch {
            updateState { copy(isLoggingOut = true) }

            when (authRepository.logout()) {
                is ChallaResult.Success -> sendEffect(AccountSideEffect.LogoutSuccess)
                is ChallaResult.Failure -> sendEffect(AccountSideEffect.LogoutFailed)
            }

            updateState { copy(isLoggingOut = false) }
        }
    }

    private fun handleWithdrawalConfirmClick() {
        if (currentState.isWithdrawing) return

        viewModelScope.launch {
            updateState { copy(isWithdrawing = true) }

            when (userRepository.withdraw()) {
                is ChallaResult.Success -> sendEffect(AccountSideEffect.WithdrawalSuccess)
                is ChallaResult.Failure -> sendEffect(AccountSideEffect.WithdrawalFailed)
            }

            updateState { copy(isWithdrawing = false) }
        }
    }
}
