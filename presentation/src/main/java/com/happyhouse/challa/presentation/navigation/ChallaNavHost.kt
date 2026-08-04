package com.happyhouse.challa.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.camera.CameraRoute
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarContent
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarVisuals
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.gallery.GalleryRoute
import com.happyhouse.challa.presentation.home.HomeRoute
import com.happyhouse.challa.presentation.home.createroom.CreateRoomScreen
import com.happyhouse.challa.presentation.home.shareinvite.ShareInviteScreen
import com.happyhouse.challa.presentation.login.LoginRoute
import com.happyhouse.challa.presentation.photodetail.PhotoDetailRoute
import com.happyhouse.challa.presentation.profile.CreateProfileRoute
import com.happyhouse.challa.presentation.room.main.RoomMainRoute
import com.happyhouse.challa.presentation.setting.SettingRoute
import com.happyhouse.challa.presentation.setting.account.AccountRoute
import com.happyhouse.challa.presentation.setting.notification.NotificationRoute
import com.happyhouse.challa.presentation.setting.theme.ThemeRoute
import kotlinx.coroutines.launch

@Composable
fun ChallaNavHost(
    navigator: ChallaNavigator,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val logoutSuccessMessage = stringResource(R.string.account_logout_success)

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
                        snackbarHostState = snackbarHostState,
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
                        onNavigateToCreateRoom = {
                            navigator.navigate(ChallaRoute.CreateRoom)
                        },
                        onNavigateToInviteCode = {
                            // TODO JH: 초대 코드 입력 화면 구현되면 navigator.navigate(...) 연결
                        },
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
                        onNotificationClick = {
                            navigator.navigate(ChallaRoute.Notification)
                        },
                        onAccountClick = {
                            navigator.navigate(ChallaRoute.Account)
                        },
                        onSupportClick = {},
                        onFeedbackClick = {},
                    )
                }
                entry<ChallaRoute.ThemeSetting> {
                    ThemeRoute(
                        onBackClick = { navigator.goBack() },
                    )
                }
                entry<ChallaRoute.Notification> {
                    NotificationRoute(
                        onBackClick = { navigator.goBack() },
                    )
                }
                entry<ChallaRoute.Account> {
                    AccountRoute(
                        onBackClick = { navigator.goBack() },
                        onLogoutSuccess = {
                            navigator.clearAndNavigate(ChallaRoute.Login)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    ChallaSnackbarVisuals(
                                        content =
                                            ChallaSnackbarContent.HeadingOnly(
                                                heading = logoutSuccessMessage,
                                            ),
                                        icon = ChallaIcons.Check,
                                    ),
                                )
                            }
                        },
                        onWithdrawSuccess = {
                            navigator.clearAndNavigate(ChallaRoute.Login)
                        },
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
