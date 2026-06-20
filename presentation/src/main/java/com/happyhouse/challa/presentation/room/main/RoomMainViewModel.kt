package com.happyhouse.challa.presentation.room.main

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.room.main.contract.RoomMainIntent
import com.happyhouse.challa.presentation.room.main.contract.RoomMainSideEffect
import com.happyhouse.challa.presentation.room.main.contract.RoomMainState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomMainViewModel @Inject constructor() :
    BaseViewModel<RoomMainState, RoomMainIntent, RoomMainSideEffect>(
        initialState = RoomMainState(),
    ) {
        init {
            onIntent(RoomMainIntent.FetchData)
        }

        override fun onIntent(intent: RoomMainIntent) {
            when (intent) {
                RoomMainIntent.FetchData -> fetchData()
                RoomMainIntent.ShareClick -> postSideEffect(RoomMainSideEffect.ShareRequested)
            }
        }

        private fun fetchData() {
            updateState { RoomMainState() }
        }

        private fun postSideEffect(sideEffect: RoomMainSideEffect) {
            viewModelScope.launch {
                sendEffect(sideEffect)
            }
        }
    }
