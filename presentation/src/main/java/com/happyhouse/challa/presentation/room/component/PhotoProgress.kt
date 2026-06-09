package com.happyhouse.challa.presentation.room.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper

@Composable
internal fun PhotoProgress() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text =
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = RoomBlack,
                            fontSize = 58.sp,
                        ),
                    ) {
                        append("12")
                    }
                    withStyle(SpanStyle(color = RoomGray300)) {
                        append("/24")
                    }
                },
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "찍은 사진",
            color = RoomGray700,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(RoomGray100),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(0.5f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(RoomBlack),
            )
        }
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoProgressPreview() {
    PhotoProgress()
}
