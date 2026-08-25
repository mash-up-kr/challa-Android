package com.happyhouse.challa.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.camera.CameraRoute
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarContent
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarHost
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarVisuals
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.gallery.GalleryRoute
import com.happyhouse.challa.presentation.home.HomeRoute
import com.happyhouse.challa.presentation.login.LoginRoute
import com.happyhouse.challa.presentation.photodetail.PhotoDetailRoute
import com.happyhouse.challa.presentation.profile.EditProfileRoute
import com.happyhouse.challa.presentation.profile.SettingProfileRoute
import com.happyhouse.challa.presentation.room.realtime.RoomMemberJoinedToastHost
import com.happyhouse.challa.presentation.room.realtime.RoomMemberJoinedToastVisuals
import com.happyhouse.challa.presentation.room.realtime.RoomRealtimeViewModel
import com.happyhouse.challa.presentation.room.realtime.toDisplayMessage
import com.happyhouse.challa.presentation.setting.SettingRoute
import com.happyhouse.challa.presentation.setting.account.AccountRoute
import com.happyhouse.challa.presentation.setting.license.OpenSourceLicenseRoute
import com.happyhouse.challa.presentation.setting.notification.NotificationRoute
import com.happyhouse.challa.presentation.setting.theme.ThemeRoute
import kotlinx.coroutines.launch

@Composable
fun ChallaNavHost(
    navigator: ChallaNavigator,
    modifier: Modifier = Modifier,
) {
    val roomRealtimeViewModel: RoomRealtimeViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val roomMemberJoinedHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val logoutSuccessMessage = stringResource(R.string.account_logout_success)
    val profileUpdateSuccessMessage = stringResource(R.string.setting_profile_update_success)
    val roomMemberJoinedSuffix = stringResource(R.string.room_member_joined_suffix)
    val currentRoute = navigator.currentRoute

    LifecycleStartEffect(roomRealtimeViewModel) {
        roomRealtimeViewModel.startObserving()

        onStopOrDispose {
            roomRealtimeViewModel.pauseObserving()
        }
    }

    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            is ChallaRoute.RoomScoped -> roomRealtimeViewModel.addObservedRoom(currentRoute.roomId)
            ChallaRoute.Login,
            ChallaRoute.SettingProfile,
            -> roomRealtimeViewModel.stopObserving()

            else -> Unit
        }
    }

    LaunchedEffect(roomRealtimeViewModel) {
        roomRealtimeViewModel.events.collect { event ->
            val message = event.toDisplayMessage()
            launch {
                roomMemberJoinedHostState.showSnackbar(
                    RoomMemberJoinedToastVisuals(
                        message = message.leadingText + message.roomTitle + roomMemberJoinedSuffix,
                        userProfileImageUrl = event.userProfileImageUrl,
                    ),
                )
            }
        }
    }

    Box(modifier = modifier) {
        NavDisplay(
            backStack = navigator.backStack,
            modifier = Modifier.fillMaxSize(),
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
                                    ChallaRoute.PhotoDetail(
                                        roomId = route.roomId,
                                        photoId = photoId,
                                    ),
                                )
                            },
                            onShootClick = {
                                navigator.navigate(ChallaRoute.Camera(roomId = route.roomId))
                            },
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
                                    if (isNewUser) ChallaRoute.SettingProfile else ChallaRoute.Home,
                                )
                            },
                        )
                    }
                    entry<ChallaRoute.SettingProfile> {
                        SettingProfileRoute(
                            onProfileCreated = {
                                navigator.replace(ChallaRoute.Home)
                            },
                        )
                    }
                    entry<ChallaRoute.EditProfile> { route ->
                        EditProfileRoute(
                            initialNickname = route.nickname,
                            initialProfileImageUrl = route.profileImageUrl,
                            onBackClick = { navigator.goBack() },
                            onProfileUpdated = {
                                navigator.goBack()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        ChallaSnackbarVisuals(
                                            content =
                                                ChallaSnackbarContent.HeadingOnly(
                                                    heading = profileUpdateSuccessMessage,
                                                ),
                                            icon = ChallaIcons.Check,
                                        ),
                                    )
                                }
                            },
                        )
                    }
                    entry<ChallaRoute.Home> {
                        HomeRoute(
                            onNavigateToSetting = {
                                navigator.navigate(ChallaRoute.Setting)
                            },
                            onNavigateToRoom = { roomId ->
                                navigator.navigate(ChallaRoute.Gallery(roomId = roomId))
                            },
                            onRoomIdsLoaded = roomRealtimeViewModel::replaceObservedRooms,
                        )
                    }
                    entry<ChallaRoute.Setting> {
                        SettingRoute(
                            snackbarHostState = snackbarHostState,
                            onBackClick = { navigator.goBack() },
                            onProfileEditClick = { nickname, profileImageUrl ->
                                navigator.navigate(
                                    ChallaRoute.EditProfile(
                                        nickname = nickname,
                                        profileImageUrl = profileImageUrl,
                                    ),
                                )
                            },
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
                            onOpenSourceLicenseClick = {
                                navigator.navigate(ChallaRoute.OpenSourceLicense)
                            },
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
                    entry<ChallaRoute.OpenSourceLicense> {
                        OpenSourceLicenseRoute(
                            snackbarHostState = snackbarHostState,
                            onBackClick = { navigator.goBack() },
                        )
                    }
                },
        )

        ChallaSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.fillMaxSize(),
        )

        RoomMemberJoinedToastHost(
            hostState = roomMemberJoinedHostState,
        )
    }
}
