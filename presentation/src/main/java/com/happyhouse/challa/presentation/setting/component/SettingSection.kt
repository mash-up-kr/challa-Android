package com.happyhouse.challa.presentation.setting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.component.ChallaList
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

@Composable
fun SettingSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = ChallaTheme.colors.backgroundLevel1,
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(start = 24.dp, top = 10.dp, end = 16.dp, bottom = 10.dp),
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
            color = ChallaTheme.colors.labelNormal,
            style = ChallaTheme.typography.bodyXSmall.bold,
        )
        content()
    }
}

@Preview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun SettingSectionPreview() {
    SettingSection(title = "앱 설정") {
        ChallaList(
            text = "테마",
            leadingIcon = ChallaIcons.Palette,
            trailingText = "레몬에이드",
            onClick = {},
        )
        ChallaList(
            text = "알림",
            leadingIcon = ChallaIcons.Bell,
            onClick = {},
        )
    }
}
