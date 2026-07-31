package com.happyhouse.challa.presentation.setting.theme

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.model.PrimaryTheme
import com.happyhouse.challa.domain.repository.ThemeRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.setting.theme.contract.ThemeIntent
import com.happyhouse.challa.presentation.setting.theme.contract.ThemeSideEffect
import com.happyhouse.challa.presentation.setting.theme.contract.ThemeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * primary theme 선택과 저장 상태를 관리합니다.
 *
 * 화면에는 선택값을 즉시 반영하고, 연속 선택은 conflated channel로 처리해 저장 중 들어온 여러 값 가운데 가장 최근 값만 대기시킵니다.
 * 저장에 실패하면 Repository가 마지막으로 방출한 값으로 화면 상태를 복구합니다.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
) : BaseViewModel<ThemeState, ThemeIntent, ThemeSideEffect>(
        initialState = ThemeState(),
    ) {
    private val themeUpdates = Channel<PrimaryTheme>(capacity = Channel.CONFLATED)
    private var persistedTheme = PrimaryTheme.LEMONADE

    init {
        observePrimaryTheme()
        processThemeUpdates()
    }

    override fun onIntent(intent: ThemeIntent) {
        when (intent) {
            is ThemeIntent.ThemeSelect -> selectTheme(intent.theme)
        }
    }

    private fun observePrimaryTheme() {
        viewModelScope.launch {
            themeRepository.primaryTheme.collect { theme ->
                persistedTheme = theme
                if (!currentState.isSaving) {
                    updateState {
                        copy(
                            selectedTheme = theme,
                            isSaveFailed = false,
                        )
                    }
                }
            }
        }
    }

    private fun selectTheme(theme: PrimaryTheme) {
        if (theme == currentState.selectedTheme && !currentState.isSaveFailed) return

        updateState {
            copy(
                selectedTheme = theme,
                isSaving = true,
                isSaveFailed = false,
            )
        }
        themeUpdates.trySend(theme)
    }

    private fun processThemeUpdates() {
        viewModelScope.launch {
            for (theme in themeUpdates) {
                when (themeRepository.updatePrimaryTheme(theme)) {
                    is ChallaResult.Success -> finishUpdate(theme, isFailed = false)
                    is ChallaResult.Failure -> finishUpdate(theme, isFailed = true)
                }
            }
        }
    }

    private fun finishUpdate(
        theme: PrimaryTheme,
        isFailed: Boolean,
    ) {
        if (currentState.selectedTheme != theme) return

        updateState {
            copy(
                selectedTheme = if (isFailed) persistedTheme else selectedTheme,
                isSaving = false,
                isSaveFailed = isFailed,
            )
        }
    }
}
