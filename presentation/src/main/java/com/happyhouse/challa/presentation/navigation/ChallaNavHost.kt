package com.happyhouse.challa.presentation.navigation

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.happyhouse.challa.presentation.login.LoginScreen
import com.happyhouse.challa.presentation.onboarding.OnboardingScreen
import com.happyhouse.challa.presentation.sample.SampleScreen

@Composable
fun ChallaNavHost(navigator: ChallaNavigator) {
    NavDisplay(
        modifier = Modifier.navigationBarsPadding(),
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
                            // TODO JH: 로그인 성공 후 다음 화면(Home 등) 구현되면 navigator.replace(...) 연결
                        },
                    )
                }
            },
    )
}
