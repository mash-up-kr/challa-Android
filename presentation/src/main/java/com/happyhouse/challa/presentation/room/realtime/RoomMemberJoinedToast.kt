package com.happyhouse.challa.presentation.room.realtime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaToast
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

/** 방 참여 이벤트의 메시지와 참여자 프로필 이미지를 [SnackbarHostState]에 전달하는 값 객체. */
internal data class RoomMemberJoinedToastVisuals(
    override val message: String,
    val userProfileImageUrl: String?,
) : SnackbarVisuals {
    override val actionLabel: String? = null
    override val withDismissAction: Boolean = false
    override val duration: SnackbarDuration = SnackbarDuration.Short
}

/**
 * 모든 navigation destination 위에 방 참여 토스트를 표시하는 전용 host.
 *
 * 공통 `ChallaSnackbarHost`와 달리 원격 프로필 이미지를 leading content로 표시해야 하므로
 * [RoomMemberJoinedToastVisuals]만 처리한다.
 */
@Composable
internal fun RoomMemberJoinedToastHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
    ) { data ->
        val visuals = data.visuals as? RoomMemberJoinedToastVisuals ?: return@SnackbarHost
        Box(modifier = Modifier.fillMaxSize()) {
            RoomMemberJoinedToast(
                message = visuals.message,
                userProfileImageUrl = visuals.userProfileImageUrl,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(
                            top = RoomMemberJoinedToastTopOffset,
                            start = RoomMemberJoinedToastHorizontalPadding,
                            end = RoomMemberJoinedToastHorizontalPadding,
                        ),
            )
        }
    }
}

@Composable
private fun RoomMemberJoinedToast(
    message: String,
    userProfileImageUrl: String?,
    modifier: Modifier = Modifier,
) {
    ChallaToast(
        heading = message,
        modifier = modifier.fillMaxWidth(),
        leadingContent = {
            val profilePlaceholder = painterResource(ChallaIcons.Profile)
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(userProfileImageUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = profilePlaceholder,
                error = profilePlaceholder,
                fallback = profilePlaceholder,
                modifier =
                    Modifier
                        .size(RoomMemberJoinedProfileImageSize)
                        .clip(CircleShape)
                        .background(ChallaTheme.colors.backgroundLevel3),
            )
        },
    )
}

private val RoomMemberJoinedToastTopOffset = 78.dp
private val RoomMemberJoinedToastHorizontalPadding = 16.dp
private val RoomMemberJoinedProfileImageSize = 32.dp
