package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.foundation.layout.statusBarsPadding
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
 * @param onSaveClick 저장할 사진이 없는 상태(로딩·에러·빈 목록)에서는 null을 넘겨 다운로드 아이콘 자체를 노출하지 않는다.
 */
@Composable
fun PhotoDetailTopBar(
    title: String,
    onBackClick: () -> Unit,
    onSaveClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    ChallaTopNavigation(
        modifier = modifier.statusBarsPadding(),
        title = title,
        variant = ChallaTopNavigationVariant.SUB,
        leadingIcon = {
            ChallaIconButton(
                icon = ChallaIcons.Left,
                onClick = onBackClick,
                contentDescription = stringResource(R.string.photo_detail_back_description),
                variant = ChallaButtonVariant.TRANSPARENT,
                size = ChallaButtonSize.MEDIUM,
            )
        },
        trailingIcon =
            onSaveClick?.let { onClick ->
                {
                    ChallaIconButton(
                        icon = ChallaIcons.Download,
                        onClick = onClick,
                        contentDescription = stringResource(R.string.photo_detail_save_description),
                        variant = ChallaButtonVariant.TRANSPARENT,
                        size = ChallaButtonSize.MEDIUM,
                    )
                }
            },
    )
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailTopBarPreview() {
    PhotoDetailTopBar(
        title = "해피하우스 강릉 여행",
        onBackClick = {},
        onSaveClick = {},
    )
}

@ComposePreview(
    showBackground = true,
    backgroundColor = 0xFF111111,
    name = "PhotoDetailTopBar - 저장 불가",
)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailTopBarWithoutSavePreview() {
    PhotoDetailTopBar(
        title = "해피하우스 강릉 여행",
        onBackClick = {},
        onSaveClick = null,
    )
}
