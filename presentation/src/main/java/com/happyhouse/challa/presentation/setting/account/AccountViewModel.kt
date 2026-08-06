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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : BaseViewModel<AccountState, AccountIntent, AccountSideEffect>(initialState = AccountState()) {
    private var profileReadJob: Job? = null

    init {
        fetchMyProfile()
    }

    override fun onIntent(intent: AccountIntent) {
        when (intent) {
            AccountIntent.ProfileReadRetry -> fetchMyProfile()
            AccountIntent.LogoutClick -> handleLogoutClick()
            AccountIntent.WithdrawalConfirmClick -> handleWithdrawalConfirmClick()
        }
    }

    private fun fetchMyProfile() {
        if (profileReadJob?.isActive == true) return

        profileReadJob =
            viewModelScope.launch {
                when (val result = userRepository.getMyProfile()) {
                    is ChallaResult.Success -> updateState { copy(nickname = result.data.nickname.orEmpty()) }
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
