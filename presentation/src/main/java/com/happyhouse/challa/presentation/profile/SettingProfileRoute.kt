package com.happyhouse.challa.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaBottomSheet
import com.happyhouse.challa.presentation.designsystem.component.ChallaInputBox
import com.happyhouse.challa.presentation.designsystem.component.ChallaNavigationIconButton
import com.happyhouse.challa.presentation.designsystem.component.ChallaProfileImage
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigation
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigationVariant
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonSize
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonVariant
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaTextButton
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarHost
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaToastVisuals
import com.happyhouse.challa.presentation.designsystem.foundation.layout.LayoutTokens
import com.happyhouse.challa.presentation.designsystem.foundation.motion.MotionTokens
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.layout.ChallaScaffold
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import kotlinx.coroutines.launch

@Composable
fun SettingProfileRoute(
    onProfileCreated: (nickname: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingProfileViewModel =
        hiltViewModel<SettingProfileViewModel, SettingProfileViewModel.Factory>(
            creationCallback = { factory ->
                factory.create(
                    mode = ProfileSettingMode.CREATE,
                    nickname = "",
                    profileImageUrl = null,
                )
            },
        ),
) {
    ProfileSettingRoute(
        onBackClick = {},
        onProfileCreated = onProfileCreated,
        onProfileUpdated = {},
        modifier = modifier,
        viewModel = viewModel,
    )
}

@Composable
fun EditProfileRoute(
    initialNickname: String,
    initialProfileImageUrl: String?,
    onBackClick: () -> Unit,
    onProfileUpdated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingProfileViewModel =
        hiltViewModel<SettingProfileViewModel, SettingProfileViewModel.Factory>(
            creationCallback = { factory ->
                factory.create(
                    mode = ProfileSettingMode.EDIT,
                    nickname = initialNickname,
                    profileImageUrl = initialProfileImageUrl,
                )
            },
        ),
) {
    ProfileSettingRoute(
        onBackClick = onBackClick,
        onProfileCreated = {},
        onProfileUpdated = onProfileUpdated,
        modifier = modifier,
        viewModel = viewModel,
    )
}

@Composable
private fun ProfileSettingRoute(
    onBackClick: () -> Unit,
    onProfileCreated: (nickname: String) -> Unit,
    onProfileUpdated: () -> Unit,
    modifier: Modifier,
    viewModel: SettingProfileViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val profileCreateFailedMessage = stringResource(R.string.create_profile_submit_failure)
    val profileUpdateFailedMessage = stringResource(R.string.edit_profile_submit_failure)
    val nicknameLengthExceededMessage =
        stringResource(R.string.create_profile_nickname_length_exceeded, NICKNAME_MAX_LENGTH)
    val destructiveIconTint = ChallaTheme.colors.statusDestructive

    var isImageSourceSheetVisible by rememberSaveable { mutableStateOf(false) }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            uri?.let { viewModel.onIntent(SettingProfileIntent.ProfileImageSelected(it.toString())) }
        }

    fun showToast(message: String) {
        snackbarHostState.currentSnackbarData?.dismiss()
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                ChallaToastVisuals(
                    message = message,
                    icon = ChallaIcons.Error,
                    iconTint = destructiveIconTint,
                    topOffset = 112.dp,
                ),
            )
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is SettingProfileSideEffect.ProfileCreated -> onProfileCreated(effect.nickname)
                SettingProfileSideEffect.ProfileUpdated -> onProfileUpdated()
                SettingProfileSideEffect.ProfileCreateFailed -> showToast(profileCreateFailedMessage)
                SettingProfileSideEffect.ProfileUpdateFailed -> showToast(profileUpdateFailedMessage)
                SettingProfileSideEffect.NicknameLengthExceeded -> showToast(nicknameLengthExceededMessage)
            }
        }
    }

    SettingProfileScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick,
        onEditImageClick = { isImageSourceSheetVisible = true },
        modifier = modifier,
    )

    ChallaSnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.fillMaxSize(),
    )

    if (isImageSourceSheetVisible) {
        ProfileImageSourceBottomSheet(
            canDeleteImage = state.profileImageUri != null,
            onSelectFromAlbumClick = {
                isImageSourceSheetVisible = false
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            onDeleteImageClick = {
                isImageSourceSheetVisible = false
                viewModel.onIntent(SettingProfileIntent.ProfileImageDeleteClick)
            },
            onDismissRequest = { isImageSourceSheetVisible = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileImageSourceBottomSheet(
    canDeleteImage: Boolean,
    onSelectFromAlbumClick: () -> Unit,
    onDeleteImageClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChallaBottomSheet(
        title = stringResource(id = R.string.create_profile_image_source_title),
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChallaTextButton(
                text = stringResource(id = R.string.create_profile_image_source_album),
                onClick = onSelectFromAlbumClick,
                variant = ChallaButtonVariant.NEUTRAL,
                modifier = Modifier.fillMaxWidth(),
            )
            if (canDeleteImage) {
                ChallaTextButton(
                    text = stringResource(id = R.string.create_profile_image_source_delete),
                    onClick = onDeleteImageClick,
                    variant = ChallaButtonVariant.DESTRUCTIVE,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            ChallaTextButton(
                text = stringResource(id = R.string.create_profile_image_source_close),
                onClick = onDismissRequest,
                variant = ChallaButtonVariant.TRANSPARENT,
                size = ChallaButtonSize.MEDIUM,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SettingProfileScreen(
    state: SettingProfileState,
    onIntent: (SettingProfileIntent) -> Unit,
    onBackClick: () -> Unit,
    onEditImageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChallaScaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        topBar = {
            ChallaTopNavigation(
                title =
                    stringResource(
                        id =
                            when (state.mode) {
                                ProfileSettingMode.CREATE -> R.string.create_profile_title
                                ProfileSettingMode.EDIT -> R.string.edit_profile_title
                            },
                    ),
                variant = ChallaTopNavigationVariant.SUB,
                leadingIcon =
                    when (state.mode) {
                        ProfileSettingMode.CREATE -> null
                        ProfileSettingMode.EDIT -> {
                            {
                                ChallaNavigationIconButton(
                                    icon = ChallaIcons.Left,
                                    onClick = onBackClick,
                                    contentDescription = stringResource(R.string.edit_profile_back_description),
                                )
                            }
                        }
                    },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 홈으로 넘어갈 때 인사말이 홈 문구로 크로스페이드된다.
                Crossfade(
                    targetState = state.isEnteringHome,
                    animationSpec = enterHomeSpec(),
                    label = "SettingProfileHeadline",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                ) { isEnteringHome ->
                    Text(
                        text =
                            headlineText(
                                nickname = state.nickname,
                                isCompleted = state.isCompleted,
                                isEnteringHome = isEnteringHome,
                            ),
                        color = ChallaTheme.colors.labelNormal,
                        style = ChallaTheme.typography.headingSmall.bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                ProfileCard(
                    nickname = state.nickname,
                    profileImageUri = state.profileImageUri,
                    isCompleted = state.isCompleted,
                    isEnteringHome = state.isEnteringHome,
                    isSubmitting = state.isSubmitting,
                    isNicknameLengthExceeded = state.isNicknameLengthExceeded,
                    onNicknameChange = { onIntent(SettingProfileIntent.NicknameChanged(it)) },
                    onEditImageClick = onEditImageClick,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
            }

            // 완료된 뒤에도 버튼 자리를 비워 둬야 위쪽 컨텐츠가 같은 높이에 머무른다.
            // canSubmit 이 완료 상태에서 false 라 투명한 버튼이 눌리지도 않는다.
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(LayoutTokens.ContentBottomReserve),
                contentAlignment = Alignment.BottomCenter,
            ) {
                ChallaTextButton(
                    text =
                        stringResource(
                            id =
                                when (state.mode) {
                                    ProfileSettingMode.CREATE -> R.string.create_profile_submit
                                    ProfileSettingMode.EDIT -> R.string.edit_profile_submit
                                },
                        ),
                    onClick = { onIntent(SettingProfileIntent.DoneClick) },
                    enabled = state.canSubmit,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .alpha(if (state.isCompleted) 0f else 1f)
                            .padding(horizontal = 16.dp)
                            // 홈 화면 버튼과 같은 아래 여백을 둬야 두 화면의 버튼이 같은 높이에 놓인다.
                            .padding(bottom = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun headlineText(
    nickname: String,
    isCompleted: Boolean,
    isEnteringHome: Boolean,
): AnnotatedString =
    when {
        // 홈과 같은 문구로 끝나야 이동한 뒤에도 화면이 이어져 보인다.
        isEnteringHome ->
            buildAnnotatedString {
                withStyle(SpanStyle(color = ChallaTheme.colors.primary)) {
                    append(nickname)
                }
                append("\n")
                append(stringResource(id = R.string.home_empty_subtitle))
            }

        isCompleted ->
            buildAnnotatedString {
                withStyle(SpanStyle(color = ChallaTheme.colors.primary)) {
                    append(nickname)
                }
                append(stringResource(id = R.string.create_profile_completed_greeting))
            }

        else -> AnnotatedString(stringResource(id = R.string.create_profile_headline))
    }

/** 완료 화면이 홈과 같은 모습으로 바뀌는 동안 쓰는 공통 애니메이션 스펙 */
private fun <T> enterHomeSpec() =
    tween<T>(
        durationMillis = PROFILE_ENTER_HOME_DURATION_MS,
        easing = MotionTokens.EaseOut,
    )

@Composable
private fun ProfileCard(
    nickname: String,
    profileImageUri: String?,
    isCompleted: Boolean,
    isEnteringHome: Boolean,
    isSubmitting: Boolean,
    isNicknameLengthExceeded: Boolean,
    onNicknameChange: (String) -> Unit,
    onEditImageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 홈에는 카드 배경이 없으므로 넘어가는 동안 배경만 서서히 지운다.
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isEnteringHome) 0f else 1f,
        animationSpec = enterHomeSpec(),
        label = "ProfileCardBackground",
    )

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ChallaTheme.colors.backgroundLevel1.copy(alpha = backgroundAlpha))
                .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProfileImagePicker(
            profileImageUri = profileImageUri,
            isEditable = !isCompleted && !isSubmitting,
            onClick = onEditImageClick,
        )

        // 입력창이 위쪽 간격과 함께 접히면서 카드가 줄고, 가운데 정렬 덕분에 내용 전체가 홈 위치로 내려간다.
        AnimatedVisibility(
            visible = !isEnteringHome,
            exit = shrinkVertically(animationSpec = enterHomeSpec()) + fadeOut(animationSpec = enterHomeSpec()),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(20.dp))

                ChallaInputBox(
                    value = nickname,
                    onValueChange = onNicknameChange,
                    placeholder = stringResource(id = R.string.create_profile_nickname_placeholder),
                    enabled = !isCompleted && !isSubmitting,
                    isError = isNicknameLengthExceeded,
                )
            }
        }
    }
}

@Composable
private fun ProfileImagePicker(
    profileImageUri: String?,
    isEditable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(LayoutTokens.ProfileImageSize)
                .then(
                    if (isEditable) Modifier.noRippleClickOnce(onClick = onClick) else Modifier,
                ),
    ) {
        ChallaProfileImage(
            profileImageUrl = profileImageUri,
            modifier = Modifier.fillMaxSize(),
            fallbackIcon = R.drawable.ic_profile_setting,
        )

        if (isEditable) {
            Image(
                painter = painterResource(id = R.drawable.ic_profile_camera),
                contentDescription =
                    stringResource(id = R.string.create_profile_image_edit_description),
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp),
            )
        }
    }
}

@Preview(showBackground = true, name = "SettingProfile - Empty")
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun SettingProfileScreenEmptyPreview() {
    SettingProfileScreen(
        state = SettingProfileState(),
        onIntent = {},
        onBackClick = {},
        onEditImageClick = {},
    )
}

@Preview(showBackground = true, name = "SettingProfile - Filled")
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun SettingProfileScreenFilledPreview() {
    SettingProfileScreen(
        state = SettingProfileState(nickname = "찰나"),
        onIntent = {},
        onBackClick = {},
        onEditImageClick = {},
    )
}

@Preview(showBackground = true, name = "SettingProfile - Completed")
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun SettingProfileScreenCompletedPreview() {
    SettingProfileScreen(
        state = SettingProfileState(nickname = "찰나", isCompleted = true),
        onIntent = {},
        onBackClick = {},
        onEditImageClick = {},
    )
}
