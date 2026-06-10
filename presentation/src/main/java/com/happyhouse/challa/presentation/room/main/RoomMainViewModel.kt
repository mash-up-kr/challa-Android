package com.happyhouse.challa.presentation.room.main

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.room.main.contract.RoomMainUiIntent
import com.happyhouse.challa.presentation.room.main.contract.RoomMainUiSideEffect
import com.happyhouse.challa.presentation.room.main.contract.RoomMainUiState
import com.happyhouse.challa.presentation.room.main.model.RoomMainStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomMainViewModel @Inject constructor() :
    BaseViewModel<RoomMainUiState, RoomMainUiIntent, RoomMainUiSideEffect>(initialState = RoomMainUiState()) {
        init {
            onIntent(RoomMainUiIntent.FetchData)
        }

        override fun onIntent(intent: RoomMainUiIntent) {
            when (intent) {
                RoomMainUiIntent.FetchData -> fetchData()
                RoomMainUiIntent.ShareClick -> postSideEffect(RoomMainUiSideEffect.ShareInviteLink)
                RoomMainUiIntent.MainActionClick -> handleStatusActionClick()
            }
        }

        private fun fetchData() {
            updateState { RoomMainUiState() }
        }

        private fun handleStatusActionClick() {
            when (currentState.status) {
                RoomMainStatus.Shooting -> postSideEffect(RoomMainUiSideEffect.NavigateToCamera)
                RoomMainStatus.Waiting -> Unit
                RoomMainStatus.Published -> postSideEffect(RoomMainUiSideEffect.NavigateToGallery)
            }
        }

        private fun postSideEffect(sideEffect: RoomMainUiSideEffect) {
            viewModelScope.launch {
                sendEffect(sideEffect)
            }
        }
    }
