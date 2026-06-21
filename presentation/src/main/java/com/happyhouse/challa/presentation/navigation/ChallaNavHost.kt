package com.happyhouse.challa.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.happyhouse.challa.presentation.camera.CameraRoute
import com.happyhouse.challa.presentation.login.LoginScreen
import com.happyhouse.challa.presentation.onboarding.OnboardingScreen
import com.happyhouse.challa.presentation.room.main.RoomMainRoute
import com.happyhouse.challa.presentation.sample.SampleScreen

@Composable
fun ChallaNavHost(
    navigator: ChallaNavigator,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        backStack = navigator.backStack,
        modifier = modifier.navigationBarsPadding(),
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        predictivePopTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        entryProvider =
            entryProvider {
                entry<ChallaRoute.Sample> {
                    SampleScreen(
                        onEnterRoom = {
                            navigator.navigate(ChallaRoute.RoomMain)
                        },
                    )
                }
                entry<ChallaRoute.Camera> { route ->
                    CameraRoute(
                        roomId = route.roomId,
                        onBackClick = { navigator.goBack() },
                    )
                }
                entry<ChallaRoute.RoomMain> {
                    RoomMainRoute(
                        onBackClick = {
                            navigator.goBack()
                        },
                        onShareClick = {},
                        onCameraClick = {
                            navigator.navigate(ChallaRoute.Camera(roomId = 1L))
                        },
                        onGalleryClick = {},
                    )
                }
                entry<ChallaRoute.Onboarding> {
                    OnboardingScreen(
                        onComplete = {
                            // login화면으로 보내지 않고 SampleScreen으로 보냄
                            navigator.replace(ChallaRoute.Sample)
                        },
                    )
                }
                entry<ChallaRoute.Login> {
                    LoginScreen(
                        onLoginSuccess = {
                            // TODO JH: 로그인 성공 후 다음 화면(Home 등) 구현되면 navigator.replace(...) 연결
                        },
                    )
                }
            },
    )
}
