package com.happyhouse.challa.presentation.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce

enum class ChallaGender {
    MALE,
    FEMALE,
    UNSPECIFIED,
}

@Composable
fun ChallaGenderSelector(
    selectedGender: ChallaGender,
    onGenderClick: (ChallaGender) -> Unit,
    label: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = ChallaTheme.colors.labelNormal,
            style = ChallaTheme.typography.bodySmall.bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1D1D22))
                    .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChallaGender.entries.forEach { gender ->
                GenderOption(
                    gender = gender,
                    selected = selectedGender == gender,
                    onClick = { onGenderClick(gender) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            color = ChallaTheme.colors.labelAlternative,
            style = ChallaTheme.typography.descriptionLarge.bold,
        )
    }
}

@Composable
private fun GenderOption(
    gender: ChallaGender,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .clip(shape)
                .background(
                    if (selected) {
                        Color(0xFF31323C)
                    } else {
                        Color(0xFF24252C)
                    },
                )
                .then(
                    if (selected) {
                        Modifier.border(
                            width = 1.dp,
                            color = Color(0xFF4C4D5D),
                            shape = shape,
                        )
                    } else {
                        Modifier
                    },
                )
                .noRippleClickOnce(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = gender.label,
            color =
                if (selected) {
                    ChallaTheme.colors.labelNormal
                } else {
                    ChallaTheme.colors.labelNeutral
                },
            textAlign = TextAlign.Center,
            style = ChallaTheme.typography.descriptionLarge.bold,
        )
    }
}

private val ChallaGender.label: String
    get() =
        when (this) {
            ChallaGender.MALE -> "남성"
            ChallaGender.FEMALE -> "여성"
            ChallaGender.UNSPECIFIED -> "설정 안 함"
        }

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaGenderSelectorPreview() {
    var selectedGender by rememberSaveable { mutableStateOf(ChallaGender.UNSPECIFIED) }

    ChallaGenderSelector(
        selectedGender = selectedGender,
        onGenderClick = { selectedGender = it },
        label = "성별",
        description = "description입니다",
        modifier = Modifier.width(350.dp),
    )
}
