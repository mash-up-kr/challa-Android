package com.happyhouse.challa.presentation.camera.onboarding

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarContent
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarVisuals
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import kotlinx.coroutines.delay

private const val CAMERA_ONBOARDING_DELAY_MILLIS = 500L

@Composable
internal fun rememberCameraOnboardingVisibility(
    shouldShow: Boolean,
    snackbarHostState: SnackbarHostState,
    onCompleted: () -> Unit,
): Boolean {
    var step by rememberSaveable { mutableStateOf<CameraOnboardingStep?>(null) }
    val currentOnCompleted by rememberUpdatedState(onCompleted)
    val captureCountMessage = stringResource(R.string.camera_onboarding_capture_count_message)
    val cautionMessage = stringResource(R.string.camera_onboarding_caution_message)
    val nextLabel = stringResource(R.string.camera_onboarding_next)
    val confirmLabel = stringResource(R.string.camera_onboarding_confirm)
    val actionLabelColor = ChallaTheme.colors.primary

    LaunchedEffect(shouldShow) {
        if (!shouldShow) {
            step = null
            return@LaunchedEffect
        }

        if (step == null) {
            delay(CAMERA_ONBOARDING_DELAY_MILLIS)
            step = CameraOnboardingStep.CAPTURE_COUNT
        }
    }

    LaunchedEffect(
        step,
        snackbarHostState,
        captureCountMessage,
        cautionMessage,
        nextLabel,
        confirmLabel,
        actionLabelColor,
    ) {
        val currentStep = step ?: return@LaunchedEffect
        if (currentStep == CameraOnboardingStep.CAPTURE_COUNT) {
            snackbarHostState.currentSnackbarData?.dismiss()
        }

        val result =
            snackbarHostState.showSnackbar(
                ChallaSnackbarVisuals(
                    content =
                        ChallaSnackbarContent.HeadingOnly(
                            heading =
                                when (currentStep) {
                                    CameraOnboardingStep.CAPTURE_COUNT -> captureCountMessage
                                    CameraOnboardingStep.CAUTION -> cautionMessage
                                },
                        ),
                    actionLabel =
                        when (currentStep) {
                            CameraOnboardingStep.CAPTURE_COUNT -> nextLabel
                            CameraOnboardingStep.CAUTION -> confirmLabel
                        },
                    actionLabelColor = actionLabelColor,
                    duration = SnackbarDuration.Indefinite,
                ),
            )

        if (result == SnackbarResult.ActionPerformed) {
            when (currentStep) {
                CameraOnboardingStep.CAPTURE_COUNT -> {
                    step = CameraOnboardingStep.CAUTION
                }

                CameraOnboardingStep.CAUTION -> {
                    currentOnCompleted()
                    step = null
                }
            }
        }
    }

    return step != null
}

private enum class CameraOnboardingStep {
    CAPTURE_COUNT,
    CAUTION,
}
