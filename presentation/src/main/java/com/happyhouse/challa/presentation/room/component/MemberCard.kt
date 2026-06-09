package com.happyhouse.challa.presentation.room.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper

@Composable
internal fun MemberCard() {
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
        Text(
            text = "멤버 (3/12)",
            color = RoomBlack,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MemberAvatar(text = "윤", color = Color(0xFFFFDCA8))
            MemberAvatar(text = "김", color = Color(0xFFA9D8F3))
            MemberAvatar(text = "이", color = Color(0xFFC9E7C8))
            AddMemberAvatar()
        }
    }
}

@Composable
private fun MemberAvatar(
    text: String,
    color: Color,
) {
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color)
                .border(width = 1.dp, color = RoomBorder, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = RoomBlack,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun MemberCardPreview() {
    MemberCard()
}

@Composable
private fun AddMemberAvatar() {
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(width = 1.dp, color = Color(0xFF9E9E9E), shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+",
            color = Color(0xFF8B8B8B),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
