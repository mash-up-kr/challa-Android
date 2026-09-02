package com.happyhouse.challa.presentation.roomcover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaNavigationIconButton
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigation
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigationVariant
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonSize
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaTextButton
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.layout.ChallaScaffold
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.roomcover.component.RoomCoverColorPicker
import com.happyhouse.challa.presentation.roomcover.component.RoomCoverPreviewCard
import com.happyhouse.challa.presentation.roomcover.component.RoomCoverStickerPicker
import com.happyhouse.challa.presentation.roomcover.contract.RoomCoverColorUiModel
import com.happyhouse.challa.presentation.roomcover.contract.RoomCoverIntent
import com.happyhouse.challa.presentation.roomcover.contract.RoomCoverState
import com.happyhouse.challa.presentation.roomcover.contract.RoomCoverStickerUiModel
import kotlinx.collections.immutable.persistentListOf

private val HorizontalPadding = 16.dp

@Composable
fun RoomCoverScreen(
    state: RoomCoverState,
    snackbarHostState: SnackbarHostState,
    onIntent: (RoomCoverIntent) -> Unit,
    onBackClick: () -> Unit,
    onSelectImageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChallaScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHostState = snackbarHostState,
        topBar = {
            ChallaTopNavigation(
                title = stringResource(R.string.room_cover_title),
                variant = ChallaTopNavigationVariant.SUB,
                leadingIcon = {
                    ChallaNavigationIconButton(
                        icon = ChallaIcons.Left,
                        onClick = onBackClick,
                        contentDescription = stringResource(R.string.room_cover_back_description),
                    )
                },
            )
        },
    ) { contentPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
        ) {
            when (val content = state.content) {
                RoomCoverState.Content.Loading ->
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = ChallaTheme.colors.labelNormal,
                    )

                RoomCoverState.Content.Error ->
                    RoomCoverError(
                        modifier = Modifier.align(Alignment.Center),
                        onRetryClick = { onIntent(RoomCoverIntent.RetryClick) },
                    )

                is RoomCoverState.Content.Ready ->
                    RoomCoverEditor(
                        roomName = state.roomName,
                        content = content,
                        onIntent = onIntent,
                        onSelectImageClick = onSelectImageClick,
                    )
            }
        }
    }
}

@Composable
private fun RoomCoverEditor(
    roomName: String,
    content: RoomCoverState.Content.Ready,
    onIntent: (RoomCoverIntent) -> Unit,
    onSelectImageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        RoomCoverPreviewCard(
            roomName = roomName,
            memberCount = content.memberCount,
            cover = content.cover,
            canRemoveImage = content.backgroundImageUrl != null,
            onSelectImageClick = onSelectImageClick,
            onRemoveImageClick = { onIntent(RoomCoverIntent.BackgroundImageRemoveClick) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
        )

        HorizontalDivider(color = ChallaTheme.colors.lineAlternative)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionLabel(text = stringResource(R.string.room_cover_color))
            RoomCoverColorPicker(
                colors = content.colors,
                selectedColorId = content.selectedColorId,
                onColorClick = { color -> onIntent(RoomCoverIntent.ColorClick(color)) },
                modifier = Modifier.padding(horizontal = HorizontalPadding),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionLabel(text = stringResource(R.string.room_cover_sticker))
            RoomCoverStickerPicker(
                stickers = content.stickers,
                selectedStickerId = content.selectedStickerId,
                stickerColor = content.selectedColor,
                onStickerClick = { sticker -> onIntent(RoomCoverIntent.StickerClick(sticker)) },
                modifier = Modifier.padding(horizontal = HorizontalPadding),
            )
        }
    }
}

@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.padding(horizontal = HorizontalPadding),
        color = ChallaTheme.colors.labelNeutral,
        style = ChallaTheme.typography.bodySmall.bold,
    )
}

@Composable
private fun RoomCoverError(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = HorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.room_cover_load_failure),
            color = ChallaTheme.colors.labelNeutral,
            style = ChallaTheme.typography.bodyMedium.medium,
        )
        ChallaTextButton(
            text = stringResource(R.string.room_cover_retry),
            onClick = onRetryClick,
            size = ChallaButtonSize.SMALL,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun RoomCoverScreenReadyPreview() {
    RoomCoverScreen(
        state =
            RoomCoverState(
                roomName = "친구들과 유럽 여행",
                content =
                    RoomCoverState.Content.Ready(
                        memberCount = 12,
                        colors =
                            persistentListOf(
                                RoomCoverColorUiModel(id = 1L, color = Color(0xFFD5F700)),
                                RoomCoverColorUiModel(id = 2L, color = Color(0xFFFF1887)),
                                RoomCoverColorUiModel(id = 3L, color = Color(0xFF10E6D8)),
                            ),
                        stickers =
                            persistentListOf(
                                RoomCoverStickerUiModel(id = 1L, imageUrl = ""),
                                RoomCoverStickerUiModel(id = 2L, imageUrl = ""),
                                RoomCoverStickerUiModel(id = 3L, imageUrl = ""),
                            ),
                        selectedColorId = 1L,
                        selectedStickerId = 1L,
                        backgroundImageUrl = null,
                    ),
            ),
        snackbarHostState = remember { SnackbarHostState() },
        onIntent = {},
        onBackClick = {},
        onSelectImageClick = {},
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun RoomCoverScreenLoadingPreview() {
    RoomCoverScreen(
        state = RoomCoverState(content = RoomCoverState.Content.Loading),
        snackbarHostState = remember { SnackbarHostState() },
        onIntent = {},
        onBackClick = {},
        onSelectImageClick = {},
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun RoomCoverScreenErrorPreview() {
    RoomCoverScreen(
        state = RoomCoverState(content = RoomCoverState.Content.Error),
        snackbarHostState = remember { SnackbarHostState() },
        onIntent = {},
        onBackClick = {},
        onSelectImageClick = {},
    )
}
