package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaNavigationIconButton
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigation
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigationVariant
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
    )
}

@ComposePreview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryTopBarPreview() {
    GalleryTopBar(
        title = "친구들과 강릉 여행",
        onBackClick = {},
    )
}
