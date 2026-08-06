package com.happyhouse.challa.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaBottomSheet
import com.happyhouse.challa.presentation.designsystem.component.ChallaInputBox
import com.happyhouse.challa.presentation.designsystem.component.ChallaNavigationIconButton
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigation
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigationVariant
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonSize
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonVariant
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaTextButton
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaSnackbarHost
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaToastVisuals
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import kotlinx.coroutines.launch

@Composable
fun CreateProfileRoute(
    onProfileCreated: (nickname: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateProfileViewModel =
        hiltViewModel<CreateProfileViewModel, CreateProfileViewModel.Factory>(
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
    viewModel: CreateProfileViewModel =
        hiltViewModel<CreateProfileViewModel, CreateProfileViewModel.Factory>(
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
    viewModel: CreateProfileViewModel,
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
            uri?.let { viewModel.onIntent(CreateProfileIntent.ProfileImageSelected(it.toString())) }
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
                is CreateProfileSideEffect.ProfileCreated -> onProfileCreated(effect.nickname)
                CreateProfileSideEffect.ProfileUpdated -> onProfileUpdated()
                CreateProfileSideEffect.ProfileCreateFailed -> showToast(profileCreateFailedMessage)
                CreateProfileSideEffect.ProfileUpdateFailed -> showToast(profileUpdateFailedMessage)
                CreateProfileSideEffect.NicknameLengthExceeded -> showToast(nicknameLengthExceededMessage)
            }
        }
    }

    CreateProfileScreen(
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
                viewModel.onIntent(CreateProfileIntent.ProfileImageDeleteClick)
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
private fun CreateProfileScreen(
    state: CreateProfileState,
    onIntent: (CreateProfileIntent) -> Unit,
    onBackClick: () -> Unit,
    onEditImageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEditMode = state.mode == ProfileSettingMode.EDIT

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(ChallaTheme.colors.backgroundSurface)
                .statusBarsPadding()
                .imePadding(),
    ) {
        ChallaTopNavigation(
            title =
                stringResource(
                    id =
                        if (isEditMode) {
                            R.string.edit_profile_title
                        } else {
                            R.string.create_profile_title
                        },
                ),
            variant = ChallaTopNavigationVariant.SUB,
            leadingIcon =
                if (isEditMode) {
                    {
                        ChallaNavigationIconButton(
                            icon = ChallaIcons.Left,
                            onClick = onBackClick,
                            contentDescription = stringResource(R.string.edit_profile_back_description),
                        )
                    }
                } else {
                    null
                },
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = headlineText(nickname = state.nickname, isCompleted = state.isCompleted),
                color = ChallaTheme.colors.labelNormal,
                style = ChallaTheme.typography.headingSmall.bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
            )

            ProfileCard(
                nickname = state.nickname,
                profileImageUri = state.profileImageUri,
                isCompleted = state.isCompleted,
                isSubmitting = state.isSubmitting,
                isNicknameLengthExceeded = state.isNicknameLengthExceeded,
                onNicknameChange = { onIntent(CreateProfileIntent.NicknameChanged(it)) },
                onEditImageClick = onEditImageClick,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
        }

        if (!state.isCompleted) {
            ChallaTextButton(
                text =
                    stringResource(
                        id =
                            if (isEditMode) {
                                R.string.edit_profile_submit
                            } else {
                                R.string.create_profile_submit
                            },
                    ),
                onClick = { onIntent(CreateProfileIntent.DoneClick) },
                enabled = state.canSubmit,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = 12.dp)
                        .navigationBarsPadding(),
            )
        }
    }
}

@Composable
private fun headlineText(
    nickname: String,
    isCompleted: Boolean,
): AnnotatedString =
    if (isCompleted) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = ChallaTheme.colors.primaryYellow)) {
                append(nickname)
            }
            append(stringResource(id = R.string.create_profile_completed_greeting))
        }
    } else {
        AnnotatedString(stringResource(id = R.string.create_profile_headline))
    }

@Composable
private fun ProfileCard(
    nickname: String,
    profileImageUri: String?,
    isCompleted: Boolean,
    isSubmitting: Boolean,
    isNicknameLengthExceeded: Boolean,
    onNicknameChange: (String) -> Unit,
    onEditImageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ChallaTheme.colors.backgroundLevel1)
                .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProfileImagePicker(
            profileImageUri = profileImageUri,
            isEditable = !isCompleted && !isSubmitting,
            onClick = onEditImageClick,
        )

        ChallaInputBox(
            value = nickname,
            onValueChange = onNicknameChange,
            placeholder = stringResource(id = R.string.create_profile_nickname_placeholder),
            enabled = !isCompleted && !isSubmitting,
            isError = isNicknameLengthExceeded,
        )
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
                .size(80.dp)
                .then(
                    if (isEditable) Modifier.noRippleClickOnce(onClick = onClick) else Modifier,
                ),
    ) {
        ProfileImage(
            profileImageUri = profileImageUri,
            modifier = Modifier.fillMaxSize(),
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

@Composable
private fun ProfileImage(
    profileImageUri: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(CircleShape)
                .background(ChallaTheme.colors.backgroundLevel3),
        contentAlignment = Alignment.Center,
    ) {
        if (profileImageUri == null) {
            Image(
                painter = painterResource(R.drawable.ic_profile_setting),
                contentDescription = stringResource(id = R.string.create_profile_image_description),
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(profileImageUri)
                        .crossfade(true)
                        .build(),
                contentDescription = stringResource(id = R.string.create_profile_image_description),
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(ChallaTheme.colors.backgroundLevel3),
                error = ColorPainter(ChallaTheme.colors.backgroundLevel3),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(showBackground = true, name = "CreateProfile - Empty")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CreateProfileScreenEmptyPreview() {
    ChallaTheme {
        CreateProfileScreen(
            state = CreateProfileState(),
            onIntent = {},
            onBackClick = {},
            onEditImageClick = {},
        )
    }
}

@Preview(showBackground = true, name = "CreateProfile - Filled")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CreateProfileScreenFilledPreview() {
    ChallaTheme {
        CreateProfileScreen(
            state = CreateProfileState(nickname = "찰나"),
            onIntent = {},
            onBackClick = {},
            onEditImageClick = {},
        )
    }
}

@Preview(showBackground = true, name = "CreateProfile - Completed")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CreateProfileScreenCompletedPreview() {
    ChallaTheme {
        CreateProfileScreen(
            state = CreateProfileState(nickname = "찰나", isCompleted = true),
            onIntent = {},
            onBackClick = {},
            onEditImageClick = {},
        )
    }
}
