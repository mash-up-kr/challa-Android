package com.happyhouse.challa.presentation.designsystem.util

import android.os.SystemClock
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role

/**
 * 짧은 시간 안에 같은 Composable에서 발생한 중복 클릭을 무시한다.
 *
 */
internal fun Modifier.clickOnce(
    enabled: Boolean = true,
    intervalMillis: Long = 200L,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier =
    composed {
        var lastClickTimeMillis by remember { mutableLongStateOf(0L) }
        val currentOnClick by rememberUpdatedState(onClick)

        clickable(
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
        ) {
            val now = SystemClock.elapsedRealtime()
            if (now - lastClickTimeMillis >= intervalMillis) {
                lastClickTimeMillis = now
                currentOnClick()
            }
        }
    }

/**
 * 직접 지정한 [interactionSource]와 [indication]을 사용하면서 중복 클릭을 무시한다.
 *
 * ripple 제거 또는 별도 interaction 상태 관찰이 필요한 클릭 영역에서 사용한다.
 */
internal fun Modifier.clickOnce(
    interactionSource: MutableInteractionSource,
    indication: Indication?,
    enabled: Boolean = true,
    intervalMillis: Long = 200L,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier =
    composed {
        var lastClickTimeMillis by remember { mutableLongStateOf(0L) }
        val currentOnClick by rememberUpdatedState(onClick)

        clickable(
            interactionSource = interactionSource,
            indication = indication,
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
        ) {
            val now = SystemClock.elapsedRealtime()
            if (now - lastClickTimeMillis >= intervalMillis) {
                lastClickTimeMillis = now
                currentOnClick()
            }
        }
    }

/**
 * ripple 없이 클릭하면서 짧은 시간 안에 같은 Composable에서 발생한 중복 클릭을 무시한다.
 */
internal fun Modifier.noRippleClickOnce(
    enabled: Boolean = true,
    intervalMillis: Long = 200L,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier =
    composed {
        clickOnce(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            enabled = enabled,
            intervalMillis = intervalMillis,
            onClickLabel = onClickLabel,
            role = role,
            onClick = onClick,
        )
    }
