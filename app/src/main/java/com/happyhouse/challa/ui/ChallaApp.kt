package com.happyhouse.challa.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.navigation.ChallaNavHost

@Composable
fun ChallaApp(viewModel: ChallaAppViewModel = hiltViewModel()) {
    val startRoute by viewModel.startRoute.collectAsState()
    val primaryTheme by viewModel.primaryTheme.collectAsState()

    ChallaTheme(primaryTheme = primaryTheme) {
        // 저장된 토큰 확인 전(null)에는 초기 화면을 확정할 수 없으므로 렌더링을 보류한다.
        val route = startRoute ?: return@ChallaTheme
        val appState = rememberChallaAppState(route)

        ChallaNavHost(
            navigator = appState.navigator,
        )
    }
}
