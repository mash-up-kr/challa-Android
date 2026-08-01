package com.happyhouse.challa.presentation.designsystem.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarHost
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

@Composable
fun ChallaScaffold(
    modifier: Modifier = Modifier,
    containerColor: Color = ChallaTheme.colors.backgroundSurface,
    contentWindowInsets: WindowInsets = WindowInsets.safeDrawing,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Box(modifier = modifier) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = containerColor,
            contentWindowInsets = contentWindowInsets,
            topBar = {
                topBar?.let { bar ->
                    Box(modifier = Modifier.statusBarsPadding()) {
                        bar()
                    }
                }
            },
            bottomBar = {
                bottomBar?.let { bar ->
                    Box(modifier = Modifier.navigationBarsPadding()) {
                        bar()
                    }
                }
            },
            content = content,
        )

        snackbarHostState?.let { hostState ->
            ChallaSnackbarHost(
                hostState = hostState,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
