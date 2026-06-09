package com.happyhouse.challa.presentation.room.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper

@Composable
internal fun BottomActions(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = {},
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(width = 1.dp, color = Color(0xFF9E9E9E)),
            colors =
                ButtonDefaults.outlinedButtonColors(
                    contentColor = RoomBlack,
                ),
        ) {
            Text(
                text = "초대 링크 공유",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Button(
            onClick = {},
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = RoomBlack,
                    contentColor = RoomWhite,
                ),
        ) {
            Text(
                text = "촬영하기",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun BottomActionsPreview() {
    BottomActions()
}
