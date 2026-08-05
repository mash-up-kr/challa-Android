package com.happyhouse.challa.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.happyhouse.challa.presentation.camera.CameraRoute
import com.happyhouse.challa.presentation.gallery.GalleryRoute
import com.happyhouse.challa.presentation.home.HomeRoute
import com.happyhouse.challa.presentation.login.LoginRoute
import com.happyhouse.challa.presentation.photodetail.PhotoDetailRoute
import com.happyhouse.challa.presentation.profile.CreateProfileRoute
import com.happyhouse.challa.presentation.room.main.RoomMainRoute
import com.happyhouse.challa.presentation.setting.SettingRoute
import com.happyhouse.challa.presentation.setting.theme.ThemeRoute

@Composable
fun ChallaNavHost(
    navigator: ChallaNavigator,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        backStack = navigator.backStack,
        modifier = modifier,
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
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
                        onPhotoClick = { photoId ->
                            navigator.navigate(
                                ChallaRoute.PhotoDetail(roomId = route.roomId, photoId = photoId),
                            )
                        },
                        onShootClick = {
                            navigator.navigate(ChallaRoute.Camera(roomId = route.roomId))
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
                        onLoginSuccess = { isNewUser ->
                            // 신규 유저는 프로필 설정 온보딩으로, 기존 유저는 홈으로 진입한다.
                            navigator.replace(
                                if (isNewUser) ChallaRoute.CreateProfile else ChallaRoute.Home,
                            )
                        },
                    )
                }
                entry<ChallaRoute.CreateProfile> {
                    CreateProfileRoute(
                        onProfileCreated = {
                            navigator.replace(ChallaRoute.Home)
                        },
                    )
                }
                entry<ChallaRoute.Home> {
                    HomeRoute(
                        onNavigateToSetting = {
                            navigator.navigate(ChallaRoute.Setting)
                        },
                        onNavigateToRoom = {
                            // TODO JH: roomId 전달 방식 확정되면 RoomMain에 인자 연결
                            navigator.navigate(ChallaRoute.RoomMain)
                        },
                    )
                }
                entry<ChallaRoute.Setting> {
                    SettingRoute(
                        onBackClick = { navigator.goBack() },
                        onProfileEditClick = {},
                        onThemeClick = {
                            navigator.navigate(ChallaRoute.ThemeSetting)
                        },
                        onNotificationClick = {},
                        onAccountClick = {},
                        onSupportClick = {},
                        onFeedbackClick = {},
                    )
                }
                entry<ChallaRoute.ThemeSetting> {
                    ThemeRoute(
                        onBackClick = { navigator.goBack() },
                    )
                }
            },
    )
}
