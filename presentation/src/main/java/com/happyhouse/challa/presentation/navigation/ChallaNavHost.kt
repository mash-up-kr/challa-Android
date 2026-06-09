package com.happyhouse.challa.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.happyhouse.challa.presentation.room.RoomMainScreen
import com.happyhouse.challa.presentation.sample.SampleScreen

@Composable
fun ChallaNavHost(navigator: ChallaNavigator) {
    NavDisplay(
        backStack = navigator.backStack,
        entryProvider =
            entryProvider {
                entry<ChallaRoute.Sample> {
                    SampleScreen(
                        onEnterRoom = {
                            navigator.navigate(ChallaRoute.RoomMain)
                        },
                    )
                }
                entry<ChallaRoute.RoomMain> {
                    RoomMainScreen(
                        onBackClick = {
                            navigator.goBack()
                        },
                    )
                }
            },
    )
}
