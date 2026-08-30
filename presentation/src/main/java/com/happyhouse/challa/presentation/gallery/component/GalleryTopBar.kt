package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaNavigationIconButton
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigation
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigationVariant
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

/**
 * 갤러리 상단 바
 */
@Composable
fun GalleryTopBar(
    title: String,
    onBackClick: () -> Unit,
    onSettingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChallaTopNavigation(
        modifier = modifier,
        title = title,
        variant = ChallaTopNavigationVariant.SUB,
        leadingIcon = {
            ChallaNavigationIconButton(
                icon = ChallaIcons.Left,
                onClick = onBackClick,
                contentDescription = stringResource(R.string.gallery_back_description),
            )
        },
        trailingIcon = {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .noRippleClickOnce(role = Role.Button, onClick = onSettingClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = ChallaIcons.Setting),
                    contentDescription = stringResource(R.string.gallery_setting_description),
                    modifier = Modifier.size(24.dp),
                    tint = ChallaTheme.colors.labelNeutral,
                )
            }
        },
    )
}

@ComposePreview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryTopBarPreview() {
    GalleryTopBar(
        title = "친구들과 강릉 여행",
        onBackClick = {},
        onSettingClick = {},
    )
}
