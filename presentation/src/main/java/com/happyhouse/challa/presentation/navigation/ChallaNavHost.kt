package com.happyhouse.challa.presentation.navigation

import android.content.Intent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.camera.CameraRoute
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarContent
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarVisuals
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaToastVisuals
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.gallery.GalleryRoute
import com.happyhouse.challa.presentation.home.HomeRoute
import com.happyhouse.challa.presentation.login.LoginRoute
import com.happyhouse.challa.presentation.photodetail.PhotoDetailRoute
import com.happyhouse.challa.presentation.profile.EditProfileRoute
import com.happyhouse.challa.presentation.profile.SettingProfileRoute
import com.happyhouse.challa.presentation.roomsetting.RoomSettingScreen
import com.happyhouse.challa.presentation.setting.SettingRoute
import com.happyhouse.challa.presentation.setting.account.AccountRoute
import com.happyhouse.challa.presentation.setting.notification.NotificationRoute
import com.happyhouse.challa.presentation.setting.theme.ThemeRoute
import kotlinx.coroutines.launch

// TODO: 배포를 위해 임시로 추가. 삭제 예정
private const val REPORT_FORM_URL = "https://forms.gle/FNhiTp6wt5Qxt3De8"

@Composable
fun ChallaNavHost(
    navigator: ChallaNavigator,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val logoutSuccessMessage = stringResource(R.string.account_logout_success)
    val profileUpdateSuccessMessage = stringResource(R.string.setting_profile_update_success)

    // TODO: 배포를 위해 임시로 추가. 제거 예정 for 범준
    val featureNotReadyMessage = stringResource(R.string.setting_feature_not_ready)

    // TODO: 배포를 위해 임시로 추가. 제거 예정 for 범준
    fun showFeatureNotReadyToast() {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                ChallaToastVisuals(message = featureNotReadyMessage),
            )
        }
    }

    // TODO: 배포를 위해 임시로 추가. 삭제 예정
    val context = LocalContext.current

    // TODO: 배포를 위해 임시로 추가. 삭제 예정
    fun openReportForm() {
        val intent = Intent(Intent.ACTION_VIEW, REPORT_FORM_URL.toUri())
        context.startActivity(intent)
    }

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
                        onSettingClick = { roomName ->
                            navigator.navigate(
                                ChallaRoute.RoomSetting(roomId = route.roomId, roomName = roomName),
                            )
                        },
                    )
                }
                entry<ChallaRoute.RoomSetting> { route ->
                    RoomSettingScreen(
                        roomName = route.roomName,
                        onBackClick = { navigator.goBack() },
                        // TODO: 배포를 위해 임시로 추가. 제거 예정
                        onRoomNameClick = { showFeatureNotReadyToast() },
                        // TODO: 배포를 위해 임시로 추가. 제거 예정
                        onCoverImageClick = { showFeatureNotReadyToast() },
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
                        // TODO: 배포를 위해 임시로 추가. 제거 예정 for 범준
                        onSupportClick = { showFeatureNotReadyToast() },
                        // TODO: 배포를 위해 임시로 추가. 제거 예정 for 범준
                        onFeedbackClick = { showFeatureNotReadyToast() },
                        // TODO: 배포를 위해 임시로 추가. 삭제 예정
                        onReportClick = { openReportForm() },
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
            },
    )
}
