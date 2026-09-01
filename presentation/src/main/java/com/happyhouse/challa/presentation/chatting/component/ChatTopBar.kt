package com.happyhouse.challa.presentation.chatting.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaNavigationIconButton
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigation
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigationVariant
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper

@Composable
fun ChatTopBar(
    roomName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChallaTopNavigation(
        modifier = modifier,
        title = roomName,
        variant = ChallaTopNavigationVariant.SUB,
        leadingIcon = {
            ChallaNavigationIconButton(
                icon = ChallaIcons.Left,
                onClick = onBackClick,
                contentDescription = stringResource(R.string.chat_back_description),
            )
        },
    )
}

@Preview
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChatTopBarPreview() {
    ChatTopBar(
        roomName = "해피하우스 강릉 여행",
        onBackClick = {},
    )
}
