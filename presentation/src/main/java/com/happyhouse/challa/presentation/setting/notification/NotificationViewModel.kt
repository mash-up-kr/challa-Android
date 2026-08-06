package com.happyhouse.challa.presentation.setting.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.repository.NotificationRepository
import com.happyhouse.challa.domain.result.ChallaResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel
    @Inject
    constructor(
        private val notificationRepository: NotificationRepository,
    ) : ViewModel() {
        private val _saveFailure = Channel<Unit>()
        val saveFailure: Flow<Unit> = _saveFailure.receiveAsFlow()

        val isEnabled: StateFlow<Boolean> =
            notificationRepository.isEnabled
                .mapNotNull { result ->
                    when (result) {
                        is ChallaResult.Success -> result.data
                        is ChallaResult.Failure -> null
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = true,
                )

        fun onEnabledChange(enabled: Boolean) {
            viewModelScope.launch {
                if (notificationRepository.setEnabled(enabled) is ChallaResult.Failure) {
                    _saveFailure.send(Unit)
                }
            }
        }
    }
