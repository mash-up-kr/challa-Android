package com.happyhouse.challa.presentation.home

import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.home.model.Room

data class HomeState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val rooms: List<Room> = emptyList(),
) : UiState
