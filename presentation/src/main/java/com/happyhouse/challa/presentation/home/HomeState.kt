package com.happyhouse.challa.presentation.home

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import com.happyhouse.challa.presentation.home.model.Room
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class HomeState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val rooms: ImmutableList<Room> = persistentListOf(),
) : UiState
