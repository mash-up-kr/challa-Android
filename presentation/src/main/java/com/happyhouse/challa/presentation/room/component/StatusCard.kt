package com.happyhouse.challa.presentation.room.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper

@Composable
internal fun StatusCard() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = RoomBorder,
                    shape = RoundedCornerShape(6.dp),
                )
                .padding(horizontal = 18.dp, vertical = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "상태",
                modifier = Modifier.weight(1f),
                color = RoomBlack,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Box(
                modifier =
                    Modifier
                        .border(
                            width = 1.dp,
                            color = RoomBlack,
                            shape = RoundedCornerShape(1000.dp),
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "촬영중",
                    color = RoomBlack,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "24장 완성 시 자동으로 3시간 카운트다운이 시작됩니다.",
            color = RoomGray700,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp,
        )
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun StatusCardPreview() {
    StatusCard()
}
