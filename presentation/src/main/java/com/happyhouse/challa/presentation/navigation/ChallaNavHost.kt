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
import com.happyhouse.challa.presentation.home.shareinvite.ShareInviteScreen
import com.happyhouse.challa.presentation.login.LoginRoute
import com.happyhouse.challa.presentation.photodetail.PhotoDetailRoute
import com.happyhouse.challa.presentation.room.main.RoomMainRoute
import com.happyhouse.challa.presentation.setting.SettingRoute

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
                entry<ChallaRoute.Camera> { route ->
                    CameraRoute(
                        roomId = route.roomId,
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
                entry<ChallaRoute.RoomMain> {
                    RoomMainRoute(
                        onBackClick = {
                            navigator.goBack()
                        },
                        onCameraClick = {
                            navigator.navigate(ChallaRoute.Camera(roomId = 1L))
                        },
                        onGalleryClick = {},
                    )
                }
                entry<ChallaRoute.PhotoDetail> { route ->
                    PhotoDetailRoute(
                        roomId = route.roomId,
                        photoId = route.photoId,
                        onBackClick = { navigator.goBack() },
                    )
                }
                entry<ChallaRoute.Login> {
                    LoginRoute(
                        onLoginSuccess = {
                            navigator.replace(ChallaRoute.Home)
                        },
                    )
                }
                entry<ChallaRoute.Home> {
                    HomeScreen(
                        onNavigateToSetting = {
                            navigator.navigate(ChallaRoute.Setting)
                        },
                        onNavigateToCreateRoom = {
                            navigator.navigate(ChallaRoute.CreateRoom)
                        },
                        onNavigateToRoom = { _ ->
                            // TODO JH: 방 상태별 화면(Gallery/Waiting/RoomMain) 구현되면 navigator.navigate(...) 연결
                        },
                    )
                }
                entry<ChallaRoute.Setting> {
                    SettingRoute(
                        onBackClick = { navigator.goBack() },
                        onProfileEditClick = {},
                        onThemeClick = {},
                        onNotificationClick = {},
                        onAccountClick = {},
                        onSupportClick = {},
                        onFeedbackClick = {},
                    )
                }
                entry<ChallaRoute.CreateRoom> {
                    CreateRoomScreen(
                        onClose = {
                            navigator.goBack()
                        },
                        onRoomCreated = { roomId, roomName ->
                            navigator.replace(
                                ChallaRoute.ShareInvite(roomId = roomId, roomName = roomName),
                            )
                        },
                    )
                }
                entry<ChallaRoute.ShareInvite> { route ->
                    ShareInviteScreen(
                        roomId = route.roomId,
                        roomName = route.roomName,
                        onClose = {
                            navigator.goBack()
                        },
                    )
                }
            },
    )
}
