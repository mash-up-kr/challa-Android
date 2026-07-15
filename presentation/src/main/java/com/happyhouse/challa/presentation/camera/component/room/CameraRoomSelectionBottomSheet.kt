package com.happyhouse.challa.presentation.camera.component.room

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
) {
    ChallaBottomSheet(
        title = stringResource(R.string.camera_room_sheet_title),
        onDismissRequest = onDismissRequest,
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
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            color = ChallaTheme.colors.lineAlternative,
        )
        LazyColumn(
            modifier =
                Modifier
                    .weight(weight = 1f, fill = false)
                    .padding(vertical = 12.dp),
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
}

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
                .heightIn(min = 52.dp)
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
                color =
                    when {
                        room.remainingCount <= 0 -> ChallaTheme.colors.labelDisable
                        room.remainingCount <= 3 -> ChallaTheme.colors.primaryOrange
                        else -> ChallaTheme.colors.primaryYellow
                    },
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

@Preview(showBackground = true)
@Composable
private fun CameraRoomSelectionBottomSheetPreview() {
    ChallaTheme {
        CameraRoomSelectionBottomSheet(
            rooms =
                persistentListOf(
                    CameraRoomUiModel(1L, "방이름1", 6, 24),
                    CameraRoomUiModel(2L, "방이름2방이름2", 6, 24),
                    CameraRoomUiModel(3L, "방이동먹자골목방이동먹자골목방이동", 3, 48),
                    CameraRoomUiModel(4L, "방이름3방이름3방이름3", 3, 48),
                ),
            selectedRoomId = 3L,
            onRoomClick = {},
            onDismissRequest = {},
        )
    }
}
