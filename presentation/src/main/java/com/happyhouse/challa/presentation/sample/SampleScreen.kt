package com.happyhouse.challa.presentation.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper

@Composable
fun SampleScreen(
    onEnterRoom: () -> Unit,
    onEnterWaitingRoom: (SampleRoom) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Challa Sample",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onEnterRoom,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF111111),
                    contentColor = Color.White,
                ),
        ) {
            Text(text = "방 들어가기")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                onEnterWaitingRoom(
                    SampleRoom(
                        id = 1L,
                        openAtMills = System.currentTimeMillis() + 10_800_000L,
                    ),
                )
            },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF111111),
                    contentColor = Color.White,
                ),
        ) {
            Text(text = "대기방 입장")
        }
    }
}

data class SampleRoom(
    val id: Long,
    val openAtMills: Long,
)

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun SampleScreenPreview() {
    SampleScreen(
        onEnterRoom = {},
        onEnterWaitingRoom = {},
    )
}
