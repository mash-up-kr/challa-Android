package com.happyhouse.challa.presentation.home.shareinvite

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import kotlinx.coroutines.launch

/**
 * TODO JH 하드코딩한 색상은 디자인이 완성되면 제거 예정
 */
private val TextPrimary = Color(0xFF111111)
private val TextBody = Color(0xFF555555)
private val TextMuted = Color(0xFF999999)
private val BorderColor = Color(0xFFDDDDDD)
private val DividerColor = Color(0xFFE5E5E5)
private val FooterDivider = Color(0xFFEEEEEE)
private val PlaceholderBg = Color(0xFFF7F7F7)
private val LaterButtonBg = Color(0xFFF4F4F4)
private val KakaoYellow = Color(0xFFFEE500)
private val KakaoLabel = Color(0xFF191919)

@Composable
fun ShareInviteScreen(
    roomId: String,
    roomName: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShareInviteViewModel =
        hiltViewModel<ShareInviteViewModel, ShareInviteViewModel.Factory>(
            creationCallback = { factory -> factory.create(roomId, roomName) },
        ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ShareInviteSideEffect.KakaoShareRequested -> {
                    // TODO JH 카카오 SDK 공유 시트 연동 전까지 임시 토스트
                    Toast
                        .makeText(context, R.string.share_invite_kakao_preparing, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    ShareInviteContent(
        state = state,
        onClose = onClose,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
private fun ShareInviteContent(
    state: ShareInviteState,
    onClose: () -> Unit,
    onIntent: (ShareInviteIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ShareInviteTopBar()

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                RoomCard(roomName = state.roomName)
                SharePlaceholder()
                InviteLinkRow(inviteLink = state.inviteLink)
            }

            ShareInviteFooter(
                state = state,
                onClickLater = onClose,
                onClickKakaoShare = { onIntent(ShareInviteIntent.KakaoShareClick(it)) },
            )
        }
    }
}

@Composable
private fun ShareInviteTopBar(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.share_invite_title),
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        HorizontalDivider(color = DividerColor)
    }
}

@Composable
private fun RoomCard(
    roomName: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(6.dp))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = roomName,
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(id = R.string.share_invite_card_body),
            color = TextBody,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun SharePlaceholder(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = R.string.share_invite_qr_placeholder),
        color = TextMuted,
        fontSize = 13.sp,
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(PlaceholderBg)
                .wrapContentSize(Alignment.Center),
    )
}

@Composable
private fun InviteLinkRow(
    inviteLink: String,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isLinkReady = inviteLink.isNotEmpty()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text =
                inviteLink.ifEmpty {
                    stringResource(id = R.string.share_invite_link_loading)
                },
            color = if (isLinkReady) TextPrimary else TextMuted,
            fontSize = 14.sp,
            maxLines = 1,
            modifier =
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 12.dp),
        )
        Text(
            text = stringResource(id = R.string.share_invite_copy),
            color = if (isLinkReady) TextPrimary else TextMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier =
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(6.dp))
                    .noRippleClickOnce(enabled = isLinkReady) {
                        scope.launch {
                            try {
                                clipboard.setClipEntry(
                                    ClipEntry(ClipData.newPlainText("invite_link", inviteLink)),
                                )
                                Toast
                                    .makeText(context, R.string.share_invite_link_copied, Toast.LENGTH_SHORT)
                                    .show()
                            } catch (e: Exception) {
                                // TODO JH 정책 나오면 정책에따라 수정
                            }
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun ShareInviteFooter(
    state: ShareInviteState,
    onClickLater: () -> Unit,
    onClickKakaoShare: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val kakaoEnabled = state.inviteLink.isNotEmpty() && !state.isLoading

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = FooterDivider)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(id = R.string.share_invite_later),
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .width(120.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(LaterButtonBg)
                        .noRippleClickOnce { onClickLater() }
                        .padding(vertical = 12.dp),
            )
            Text(
                text = stringResource(id = R.string.share_invite_kakao),
                color = if (kakaoEnabled) KakaoLabel else TextMuted,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (kakaoEnabled) KakaoYellow else PlaceholderBg)
                        .noRippleClickOnce(enabled = kakaoEnabled) { onClickKakaoShare(state.inviteLink) }
                        .padding(vertical = 12.dp),
            )
        }
    }
}

@Preview(showBackground = true, name = "ShareInvite - Loading")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ShareInviteLoadingPreview() {
    ShareInviteContent(
        state = ShareInviteState(roomName = "오사카 졸업여행", isLoading = true),
        onClose = {},
        onIntent = {},
    )
}

@Preview(showBackground = true, name = "ShareInvite - Ready")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ShareInviteReadyPreview() {
    ShareInviteContent(
        state =
            ShareInviteState(
                roomName = "오사카 졸업여행",
                inviteLink = "https://chalna.app/r/aB3kZ9",
                isLoading = false,
            ),
        onClose = {},
        onIntent = {},
    )
}
