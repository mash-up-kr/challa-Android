package com.happyhouse.challa.presentation.room.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce

@Composable
internal fun RoomTopBar(onBackClick: () -> Unit) {
    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .noRippleClickOnce(role = Role.Button, onClick = onBackClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "<",
                    color = RoomBlack,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = "오사카 졸업여행",
                modifier = Modifier.weight(1f),
                color = RoomBlack,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .noRippleClickOnce(role = Role.Button, onClick = {}),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "...",
                    color = RoomBlack,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = RoomBorder,
        )
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomTopBarPreview() {
    RoomTopBar(onBackClick = {})
}
