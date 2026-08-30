package com.happyhouse.challa.presentation.roomsetting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

/** [RoomSettingListItem]을 담는 카드 */
@Composable
fun RoomSettingCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ChallaTheme.colors.backgroundLevel1)
                .padding(start = 24.dp, top = 10.dp, end = 16.dp, bottom = 10.dp),
    ) {
        content()
    }
}
