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
import com.happyhouse.challa.presentation.home.HomeScreen
import com.happyhouse.challa.presentation.home.createroom.CreateRoomScreen
import com.happyhouse.challa.presentation.login.LoginScreen
import com.happyhouse.challa.presentation.onboarding.OnboardingScreen
import com.happyhouse.challa.presentation.photodetail.PhotoDetailRoute
import com.happyhouse.challa.presentation.sample.SampleScreen

@Composable
fun ChallaNavHost(
    navigator: ChallaNavigator,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        modifier = Modifier.navigationBarsPadding(),
        backStack = navigator.backStack,
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        predictivePopTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
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
                entry<ChallaRoute.PhotoDetail> { route ->
                    PhotoDetailRoute(
                        roomId = route.roomId,
                        photoId = route.photoId,
                        onBackClick = { navigator.goBack() },
                    )
                }
                entry<ChallaRoute.Onboarding> {
                    OnboardingScreen(
                        onComplete = {
                            navigator.replace(ChallaRoute.Login)
                        },
                    )
                }
                entry<ChallaRoute.Login> {
                    LoginScreen(
                        onLoginSuccess = {
                            navigator.replace(ChallaRoute.Home)
                        },
                    )
                }
                entry<ChallaRoute.Home> {
                    HomeScreen(
                        onNavigateToInviteCode = {
                            // TODO JH: 초대 코드 입력 화면 구현되면 navigator.navigate(...) 연결
                        },
                        onNavigateToCreateRoom = {
                            navigator.navigate(ChallaRoute.CreateRoom)
                        },
                        onNavigateToRoom = { _ ->
                            // TODO JH: 방 상태별 화면(Gallery/Waiting/RoomMain) 구현되면 navigator.navigate(...) 연결
                        },
                    )
                }
                entry<ChallaRoute.CreateRoom> {
                    CreateRoomScreen(
                        onClose = {
                            navigator.goBack()
                        },
                        onRoomCreated = { _, _ ->
                            // TODO JH: ShareInvite 화면 구현되면 replace로 연결. 임시로 Home으로 복귀.
                            navigator.goBack()
                        },
                    )
                }
            },
    )
}
