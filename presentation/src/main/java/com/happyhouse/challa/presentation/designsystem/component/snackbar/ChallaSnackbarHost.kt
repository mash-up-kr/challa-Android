package com.happyhouse.challa.presentation.designsystem.component.snackbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val DefaultMessageBottomOffset = 10.dp

@Composable
fun ChallaSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 12.dp,
) {
    SnackbarHost(
        hostState = hostState,
        modifier =
            modifier
                .fillMaxSize()
                .navigationBarsPadding(),
    ) { data ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val visuals = data.visuals) {
                is ChallaToastVisuals ->
                    ChallaToast(
                        heading = visuals.message,
                        modifier =
                            Modifier
                                .align(
                                    if (visuals.topOffset != null) {
                                        Alignment.TopCenter
                                    } else {
                                        Alignment.BottomCenter
                                    },
                                )
                                .challaMessageEdgePadding(visuals.topOffset)
                                .padding(horizontal = horizontalPadding),
                        icon = visuals.icon,
                        iconTint = visuals.iconTint,
                    )

                is ChallaSnackbarVisuals ->
                    ChallaSnackbar(
                        content = visuals.content,
                        modifier =
                            Modifier
                                .align(
                                    if (visuals.topOffset != null) {
                                        Alignment.TopCenter
                                    } else {
                                        Alignment.BottomCenter
                                    },
                                )
                                .challaMessageEdgePadding(visuals.topOffset)
                                .padding(horizontal = horizontalPadding),
                        icon = visuals.icon,
                        iconTint = visuals.iconTint,
                        actionLabel = visuals.actionLabel,
                        actionLabelColor = visuals.actionLabelColor,
                        onActionClick =
                            visuals.actionLabel?.let {
                                data::performAction
                            },
                        onCloseClick =
                            data::dismiss.takeIf {
                                visuals.withDismissAction
                            },
                    )

                else ->
                    Snackbar(
                        snackbarData = data,
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = DefaultMessageBottomOffset),
                    )
            }
        }
    }
}

private fun Modifier.challaMessageEdgePadding(topOffset: Dp?): Modifier =
    if (topOffset != null) {
        padding(top = topOffset)
    } else {
        padding(bottom = DefaultMessageBottomOffset)
    }
