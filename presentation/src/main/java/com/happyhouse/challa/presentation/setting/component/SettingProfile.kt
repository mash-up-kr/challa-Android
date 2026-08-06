package com.happyhouse.challa.presentation.setting.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
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
        val placeholder = painterResource(R.drawable.img_setting_profile_placeholder)
        if (state.profileImageUrl == null) {
            Image(
                painter = placeholder,
                contentDescription = null,
                modifier = Modifier.size(68.dp),
            )
        } else {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(state.profileImageUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = placeholder,
                error = placeholder,
                modifier =
                    Modifier
                        .size(68.dp)
                        .clip(CircleShape),
            )
        }

        Text(
            text = state.nickname,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
            color = ChallaTheme.colors.labelNormal,
            style = ChallaTheme.typography.bodyMedium.bold,
        )

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
        state =
            SettingState(
                nickname = "나는야멋쟁이토마토",
                profileImageUrl = "https://example.com/profile.jpg",
            ),
        onEditClick = {},
    )
}
