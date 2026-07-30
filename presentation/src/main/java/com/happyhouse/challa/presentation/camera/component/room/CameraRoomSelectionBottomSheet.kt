package com.happyhouse.challa.presentation.camera.component.room

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.camera.model.CameraRoomUiModel
import com.happyhouse.challa.presentation.camera.model.remainingCaptureStatus
import com.happyhouse.challa.presentation.designsystem.component.ChallaBottomSheet
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CameraRoomSelectionBottomSheet(
    rooms: ImmutableList<CameraRoomUiModel>,
    selectedRoomId: Long,
    onRoomClick: (CameraRoomUiModel) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChallaBottomSheet(
        title = stringResource(R.string.camera_room_sheet_title),
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        icon = {
            val closeDescription = stringResource(R.string.camera_room_sheet_close_description)
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .noRippleClickOnce(
                            role = Role.Button,
                            onClickLabel = closeDescription,
                            onClick = onDismissRequest,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(ChallaIcons.Close),
                    contentDescription = closeDescription,
                    modifier = Modifier.size(24.dp),
                    tint = ChallaTheme.colors.labelNeutral,
                )
            }
        },
    ) {
        CameraRoomSelectionContent(
            rooms = rooms,
            selectedRoomId = selectedRoomId,
            onRoomClick = onRoomClick,
        )
    }
}

@Composable
private fun CameraRoomSelectionContent(
    rooms: ImmutableList<CameraRoomUiModel>,
    selectedRoomId: Long,
    onRoomClick: (CameraRoomUiModel) -> Unit,
) {
    val selectedRoomIndex = rooms.indexOfFirst { it.id == selectedRoomId }
    val firstVisibleRoomIndex = maxOf(0, selectedRoomIndex - ROOM_ITEMS_BEFORE_SELECTION)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = firstVisibleRoomIndex)

    HorizontalDivider(
        modifier = Modifier.padding(top = 12.dp),
        color = ChallaTheme.colors.lineAlternative,
    )
    LazyColumn(
        state = listState,
        modifier = Modifier.heightIn(max = 268.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = rooms,
            key = CameraRoomUiModel::id,
        ) { room ->
            CameraRoomSelectionItem(
                room = room,
                selected = room.id == selectedRoomId,
                onClick = { onRoomClick(room) },
            )
        }
    }
}

@Preview(
    name = "방 선택 본문",
    showBackground = true,
    backgroundColor = 0xFF1A1A1A,
)
@Composable
private fun CameraRoomSelectionContentPreview() {
    ChallaTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(ChallaTheme.colors.backgroundLevel1)
                    .padding(horizontal = 16.dp),
        ) {
            CameraRoomSelectionContent(
                rooms = cameraRoomSelectionPreviewRooms,
                selectedRoomId = 3L,
                onRoomClick = {},
            )
        }
    }
}

private val cameraRoomSelectionPreviewRooms =
    persistentListOf(
        CameraRoomUiModel(
            id = 2L,
            name = "방이름2방이름2",
            remainingCount = 6,
            totalCount = 24,
        ),
        CameraRoomUiModel(
            id = 3L,
            name = "방이동먹자골목방이동먹자골목방이동",
            remainingCount = 5,
            totalCount = 48,
        ),
        CameraRoomUiModel(
            id = 4L,
            name = "방이름3방이름3방이름3",
            remainingCount = 0,
            totalCount = 48,
        ),
        CameraRoomUiModel(
            id = 5L,
            name = "방이름4",
            remainingCount = 12,
            totalCount = 24,
        ),
        CameraRoomUiModel(
            id = 1L,
            name = "방이름1",
            remainingCount = 6,
            totalCount = 24,
        ),
    )

@Composable
private fun CameraRoomSelectionItem(
    room: CameraRoomUiModel,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (selected) {
                        ChallaTheme.colors.backgroundLevel4
                    } else {
                        ChallaTheme.colors.backgroundLevel2
                    },
                )
                .then(
                    if (selected) {
                        Modifier.border(
                            1.5.dp,
                            ChallaTheme.colors.lineNormal,
                            RoundedCornerShape(12.dp),
                        )
                    } else {
                        Modifier
                    },
                )
                .noRippleClickOnce(onClick = onClick)
                .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = room.name,
            modifier = Modifier.weight(1f),
            color = if (selected) ChallaTheme.colors.labelNormal else ChallaTheme.colors.labelNeutral,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = ChallaTheme.typography.bodyMedium.bold,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.camera_remaining_count, room.remainingCount),
                color = room.remainingCaptureStatus.toContentColor(),
                style = ChallaTheme.typography.bodyXSmall.medium,
            )
            Text(
                text = stringResource(R.string.camera_total_count, room.totalCount),
                color = ChallaTheme.colors.labelAlternative,
                style = ChallaTheme.typography.bodyXSmall.medium,
            )
        }
    }
}

private const val ROOM_ITEMS_BEFORE_SELECTION = 3
