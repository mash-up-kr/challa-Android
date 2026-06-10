package com.happyhouse.challa.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.happyhouse.challa.presentation.room.waiting.RoomWaitingRoute
import com.happyhouse.challa.presentation.sample.SampleScreen

@Composable
fun ChallaNavHost(navigator: ChallaNavigator) {
    NavDisplay(
        backStack = navigator.backStack,
        entryProvider =
            entryProvider {
                entry<ChallaRoute.Sample> {
                    SampleScreen(
                        onEnterRoom = {},
                        onEnterWaitingRoom = { room ->
                            navigator.navigate(
                                ChallaRoute.RoomWaiting(
                                    roomId = room.id,
                                    opensAtMillis = room.openAtMills,
                                ),
                            )
                        },
                    )
                }

                entry<ChallaRoute.RoomWaiting> { route ->
                    RoomWaitingRoute(
                        roomId = route.roomId,
                        opensAtMillis = route.opensAtMillis,
                        onBackClick = {
                            navigator.goBack()
                        },
                        onShareClick = {
                            // TODO: 공유 처리
                        },
                        onOpenClick = { _ ->
                            // TODO: 방 열기 처리
                        },
                    )
                }
            },
    )
}
