package com.happyhouse.challa.presentation.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigation
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigationVariant
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce

@Composable
fun HomeScreen(
    onNavigateToCreateRoom: () -> Unit,
    onNavigateToInviteCode: () -> Unit,
    onNavigateToSetting: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        state = state,
        onCreateRoomClick = onNavigateToCreateRoom,
        onInviteCodeClick = onNavigateToInviteCode,
        onSettingClick = onNavigateToSetting,
        modifier = modifier,
    )
}

@Composable
private fun HomeContent(
    state: HomeState,
    onCreateRoomClick: () -> Unit,
    onInviteCodeClick: () -> Unit,
    onSettingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(ChallaTheme.colors.backgroundSurface)
                .homeGlow(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
        ) {
            HomeTopBar(
                onCreateRoomClick = onCreateRoomClick,
                onSettingClick = onSettingClick,
            )

            when {
                state.isLoading ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = ChallaTheme.colors.labelNormal,
                            strokeWidth = 2.dp,
                        )
                    }

                state.isEmpty -> {
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        HomeEmptyMessage(
                            nickname = state.nickname,
                            profileImageUrl = state.profileImageUrl,
                        )
                    }
                    HomeActionButtons(
                        onCreateRoomClick = onCreateRoomClick,
                        onInviteCodeClick = onInviteCodeClick,
                    )
                }

                else -> {
                    // TODO JH: 케이스 2 — 촬영중/촬영완료한 방이 있을 때의 방 목록 구현 예정
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * 화면 하단에 은은하게 깔리는 옐로우 글로우.
 *
 * 피그마의 blur(150) 처리된 ellipse를 대체한다.
 * [androidx.compose.ui.draw.blur]는 API 31 미만에서 동작하지 않으므로 radial gradient로 표현한다.
 */
@Composable
private fun Modifier.homeGlow(): Modifier {
    val glowColor = ChallaTheme.colors.primaryYellow
    return drawBehind {
        val center = Offset(x = size.width / 2f, y = size.height * 0.92f)
        val radius = size.width * 0.95f
        drawRect(
            brush =
                Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.20f), Color.Transparent),
                    center = center,
                    radius = radius,
                ),
        )
    }
}

@Composable
private fun HomeTopBar(
    onCreateRoomClick: () -> Unit,
    onSettingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChallaTopNavigation(
        title = stringResource(id = R.string.home_title),
        modifier = modifier,
        variant = ChallaTopNavigationVariant.MAIN,
        trailingIcon = {
            HomeTopBarAction(
                icon = ChallaIcons.Add,
                contentDescription = stringResource(id = R.string.home_add_description),
                onClick = onCreateRoomClick,
            )
            HomeTopBarAction(
                icon = ChallaIcons.Setting,
                contentDescription = stringResource(id = R.string.home_setting_description),
                onClick = onSettingClick,
            )
        },
    )
}

@Composable
private fun HomeTopBarAction(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(40.dp)
                .noRippleClickOnce(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = ChallaTheme.colors.labelNeutral,
        )
    }
}

@Composable
private fun HomeEmptyMessage(
    nickname: String,
    profileImageUrl: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = nickname,
            color = ChallaTheme.colors.primaryYellow,
            textAlign = TextAlign.Center,
            style = ChallaTheme.typography.headingSmall.bold,
        )
        Text(
            text = stringResource(id = R.string.home_empty_subtitle),
            color = ChallaTheme.colors.labelNormal,
            textAlign = TextAlign.Center,
            style = ChallaTheme.typography.headingSmall.bold,
        )
        Spacer(modifier = Modifier.height(24.dp))
        HomeProfileImage(profileImageUrl = profileImageUrl)
    }
}

@Composable
private fun HomeProfileImage(
    profileImageUrl: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(ChallaTheme.colors.backgroundLevel3),
        contentAlignment = Alignment.Center,
    ) {
        if (profileImageUrl == null) {
            Icon(
                painter = painterResource(id = ChallaIcons.Profile),
                contentDescription = stringResource(id = R.string.home_profile_description),
                modifier = Modifier.size(80.dp),
                tint = ChallaTheme.colors.labelNeutral,
            )
        } else {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(profileImageUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = stringResource(id = R.string.home_profile_description),
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(ChallaTheme.colors.backgroundLevel3),
                error = ColorPainter(ChallaTheme.colors.backgroundLevel3),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun HomeActionButtons(
    onCreateRoomClick: () -> Unit,
    onInviteCodeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HomeActionButton(
            text = stringResource(id = R.string.home_create_room),
            containerColor = ChallaTheme.colors.primaryYellow,
            onClick = onCreateRoomClick,
        )
        HomeActionButton(
            text = stringResource(id = R.string.home_enter_invite_code),
            containerColor = ChallaTheme.colors.labelNormal,
            onClick = onInviteCodeClick,
        )
    }
}

@Composable
private fun HomeActionButton(
    text: String,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(containerColor)
                .noRippleClickOnce(role = Role.Button, onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = ChallaTheme.colors.staticBlack,
            style = ChallaTheme.typography.bodyLarge.bold,
        )
    }
}

@Preview(showBackground = true, name = "Home - Empty")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun HomeEmptyPreview() {
    ChallaTheme {
        HomeContent(
            state =
                HomeState(
                    isLoading = false,
                    nickname = "나는야멋쟁이토마토",
                    profileImageUrl = null,
                ),
            onCreateRoomClick = {},
            onInviteCodeClick = {},
            onSettingClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Home - Loading")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun HomeLoadingPreview() {
    ChallaTheme {
        HomeContent(
            state = HomeState(isLoading = true),
            onCreateRoomClick = {},
            onInviteCodeClick = {},
            onSettingClick = {},
        )
    }
}
