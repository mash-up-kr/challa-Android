package com.happyhouse.challa.presentation.setting.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.repository.NotificationRepository
import com.happyhouse.challa.domain.result.ChallaResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel
    @Inject
    constructor(
        private val notificationRepository: NotificationRepository,
    ) : ViewModel() {
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

        fun setEnabled(enabled: Boolean) {
            viewModelScope.launch {
                notificationRepository.setEnabled(enabled)
            }
        }
    }
