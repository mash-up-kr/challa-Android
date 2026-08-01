package com.happyhouse.challa.presentation.setting.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonSize
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonVariant
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaIconButton
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.setting.contract.SettingState

@Composable
fun SettingProfile(
    state: SettingState,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 28.dp, top = 8.dp, end = 20.dp, bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.img_setting_profile_placeholder),
            contentDescription = null,
            modifier = Modifier.size(68.dp),
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = state.nickname,
                color = ChallaTheme.colors.labelNormal,
                style = ChallaTheme.typography.bodyMedium.bold,
            )
            Text(
                text = state.maskedEmail,
                color = ChallaTheme.colors.labelAlternative,
                style = ChallaTheme.typography.bodyMedium.regular,
            )
        }

        ChallaIconButton(
            icon = ChallaIcons.Edit,
            onClick = onEditClick,
            contentDescription = stringResource(R.string.setting_profile_edit_description),
            variant = ChallaButtonVariant.TRANSPARENT,
            size = ChallaButtonSize.LARGE,
        )
    }
}

@Preview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun SettingProfilePreview() {
    SettingProfile(
        state = SettingState(nickname = "나는야멋쟁이토마토", maskedEmail = "juy***@naver.com"),
        onEditClick = {},
    )
}
