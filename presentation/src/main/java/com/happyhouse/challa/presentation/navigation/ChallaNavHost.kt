package com.happyhouse.challa.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.happyhouse.challa.presentation.home.HomeScreen
import com.happyhouse.challa.presentation.home.createroom.CreateRoomScreen
import com.happyhouse.challa.presentation.login.LoginScreen
import com.happyhouse.challa.presentation.onboarding.OnboardingScreen
import com.happyhouse.challa.presentation.sample.SampleScreen

@Composable
fun ChallaNavHost(navigator: ChallaNavigator) {
    NavDisplay(
        backStack = navigator.backStack,
        entryProvider =
            entryProvider {
                entry<ChallaRoute.Sample> {
                    SampleScreen()
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
