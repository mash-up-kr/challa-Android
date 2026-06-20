package com.happyhouse.challa.presentation.room.main

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.model.Room
import com.happyhouse.challa.presentation.model.RoomStatus
import com.happyhouse.challa.presentation.room.main.contract.RoomMainIntent
import com.happyhouse.challa.presentation.room.main.contract.RoomMainSideEffect
import com.happyhouse.challa.presentation.room.main.contract.RoomMainState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomMainViewModel @Inject constructor() :
    BaseViewModel<RoomMainState, RoomMainIntent, RoomMainSideEffect>(
        initialState = RoomMainState.Loading,
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
            updateState { createSampleState() }
        }

        private fun postSideEffect(sideEffect: RoomMainSideEffect) {
            viewModelScope.launch {
                sendEffect(sideEffect)
            }
        }

        private fun createSampleState(): RoomMainState =
            RoomMainState.Content(
                room =
                    Room(
                        id = "room-id",
                        name = "해피하우스 프작모",
                        status = RoomStatus.Shooting(taken = 11),
                    ),
                memberInitials = persistentListOf("박", "김", "이"),
                maxMemberCount = 12,
            )
    }
