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
import com.happyhouse.challa.presentation.gallery.GalleryRoute
import com.happyhouse.challa.presentation.home.HomeScreen
import com.happyhouse.challa.presentation.home.createroom.CreateRoomScreen
import com.happyhouse.challa.presentation.login.LoginScreen
import com.happyhouse.challa.presentation.onboarding.OnboardingScreen
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
                        onGalleryClick = {
                            navigator.navigate(ChallaRoute.Gallery(roomId = 0L))
                        },
                    )
                }
                entry<ChallaRoute.Camera> { route ->
                    CameraRoute(
                        roomId = route.roomId,
                        onBackClick = { navigator.goBack() },
                    )
                }
                entry<ChallaRoute.Gallery> { route ->
                    GalleryRoute(
                        roomId = route.roomId,
                        onBackClick = { navigator.goBack() },
                        onPhotoClick = {
                            // TODO: 사진 상세 + 다운로드 화면(#24) 구현되면 navigator.navigate(ChallaRoute.PhotoDetail(...)) 연결
                        },
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
