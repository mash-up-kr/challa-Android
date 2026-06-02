package com.happyhouse.challa.presentation.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<STATE : UiState, INTENT : UiIntent, SIDE_EFFECT : UiSideEffect>(
    initialState: STATE,
) : ViewModel() {
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<STATE> = _uiState

    private val _uiEffect = Channel<SIDE_EFFECT>()
    val uiEffect: Flow<SIDE_EFFECT> = _uiEffect.receiveAsFlow()

    protected val currentState: STATE
        get() = uiState.value

    protected fun updateState(reducer: STATE.() -> STATE) {
        _uiState.update(reducer)
    }

    protected suspend fun sendEffect(sideEffect: SIDE_EFFECT) {
        _uiEffect.send(sideEffect)
    }

    abstract fun onIntent(intent: INTENT)
}
