package com.happyhouse.challa.presentation.photodetail.component

import androidx.annotation.DrawableRes
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
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigation
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigationVariant
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

/**
 * @param onSaveClick 저장할 사진이 없는 상태(로딩·에러·빈 목록)에서는 null을 넘겨 다운로드 아이콘 자체를 노출하지 않는다.
 * @param isSaveEnabled 저장이 진행 중일 때 false로 넘겨 중복 요청을 막는다.
 */
@Composable
fun PhotoDetailTopBar(
    title: String,
    onBackClick: () -> Unit,
    onSaveClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    isSaveEnabled: Boolean = true,
) {
    ChallaTopNavigation(
        modifier = modifier,
        title = title,
        variant = ChallaTopNavigationVariant.SUB,
        leadingIcon = {
            PhotoDetailTopBarIcon(
                icon = ChallaIcons.Left,
                onClick = onBackClick,
                contentDescription = stringResource(R.string.photo_detail_back_description),
            )
        },
        trailingIcon =
            onSaveClick?.let { onClick ->
                {
                    PhotoDetailTopBarIcon(
                        icon = ChallaIcons.Download,
                        onClick = onClick,
                        contentDescription = stringResource(R.string.photo_detail_save_description),
                        enabled = isSaveEnabled,
                    )
                }
            },
    )
}

@Composable
private fun PhotoDetailTopBarIcon(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    contentDescription: String,
    enabled: Boolean = true,
) {
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .noRippleClickOnce(
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = ChallaTheme.colors.labelNeutral,
        )
    }
}

@ComposePreview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailTopBarPreview() {
    PhotoDetailTopBar(
        title = "해피하우스 강릉 여행",
        onBackClick = {},
        onSaveClick = {},
    )
}

@ComposePreview(name = "PhotoDetailTopBar - 저장 중")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailTopBarSavingPreview() {
    PhotoDetailTopBar(
        title = "해피하우스 강릉 여행",
        onBackClick = {},
        onSaveClick = {},
        isSaveEnabled = false,
    )
}

@ComposePreview(name = "PhotoDetailTopBar - 저장 불가")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailTopBarWithoutSavePreview() {
    PhotoDetailTopBar(
        title = "해피하우스 강릉 여행",
        onBackClick = {},
        onSaveClick = null,
    )
}
