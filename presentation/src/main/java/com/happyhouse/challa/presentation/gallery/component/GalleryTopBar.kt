package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigation
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigationVariant
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonSize
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonVariant
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaIconButton
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
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
    // 상태바 인셋은 ChallaScaffold의 topBar 슬롯이 적용한다.
    ChallaTopNavigation(
        modifier = modifier,
        title = title,
        variant = ChallaTopNavigationVariant.SUB,
        leadingIcon = {
            ChallaIconButton(
                icon = ChallaIcons.Left,
                onClick = onBackClick,
                contentDescription = stringResource(R.string.gallery_back_description),
                variant = ChallaButtonVariant.TRANSPARENT,
                size = ChallaButtonSize.MEDIUM,
            )
        },
        trailingIcon = {
            ChallaIconButton(
                icon = ChallaIcons.Setting,
                onClick = onSettingClick,
                contentDescription = stringResource(R.string.gallery_setting_description),
                variant = ChallaButtonVariant.TRANSPARENT,
                size = ChallaButtonSize.MEDIUM,
            )
        },
    )
}

@ComposePreview(showBackground = true, widthDp = 390)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryTopBarPreview() {
    GalleryTopBar(
        title = "친구들과 강릉 여행",
        onBackClick = {},
        onSettingClick = {},
    )
}
