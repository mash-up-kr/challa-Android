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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun MemberCard(
    memberInitials: ImmutableList<String>,
    maxMemberCount: Int,
) {
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
            text = "멤버 (${memberInitials.size} / $maxMemberCount)",
            color = RoomBlack,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            memberInitials.forEachIndexed { index, initial ->
                MemberAvatar(
                    text = initial,
                    color = memberAvatarColors[index % memberAvatarColors.size],
                )
            }
            if (memberInitials.size < maxMemberCount) {
                AddMemberAvatar()
            }
        }
    }
}

private val memberAvatarColors =
    persistentListOf(
        Color(0xFFFFDCA8),
        Color(0xFFA9D8F3),
        Color(0xFFC9E7C8),
    )

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
    MemberCard(
        memberInitials = persistentListOf("박", "김", "이"),
        maxMemberCount = 12,
    )
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
