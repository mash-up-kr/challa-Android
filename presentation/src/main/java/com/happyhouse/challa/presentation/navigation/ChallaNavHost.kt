package com.happyhouse.challa.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
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
                            // TODO JH: Login 화면 구현 후 navigator.replace(ChallaRoute.Login)
                        },
                    )
                }
            },
    )
}
