package com.happyhouse.challa.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.happyhouse.challa.presentation.camera.CameraRoute
import com.happyhouse.challa.presentation.sample.SampleScreen

@Composable
fun ChallaNavHost(navigator: ChallaNavigator) {
    NavDisplay(
        backStack = navigator.backStack,
        entryProvider =
            entryProvider {
                entry<ChallaRoute.Sample> {
                    SampleScreen(
                        onCameraClick = {
                            navigator.navigate(ChallaRoute.Camera(roomId = 0L))
                        },
                    )
                }
                entry<ChallaRoute.Camera> { route ->
                    CameraRoute(
                        roomId = route.roomId,
                        onBackClick = { navigator.goBack() },
                    )
                }
            },
    )
}
