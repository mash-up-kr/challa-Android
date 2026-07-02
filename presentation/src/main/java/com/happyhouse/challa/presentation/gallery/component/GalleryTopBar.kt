package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

/**
 * 갤러리 상단 바
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryTopBar(
    title: String,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        },
        navigationIcon = {
            TopBarIconButton(
                symbol = stringResource(R.string.gallery_back_icon),
                contentDescription = stringResource(R.string.gallery_back_description),
                onClick = onBackClick,
            )
        },
        actions = {
            TopBarIconButton(
                symbol = stringResource(R.string.gallery_more_icon),
                contentDescription = stringResource(R.string.gallery_more_description),
                onClick = onMoreClick,
            )
        },
    )
}

@Composable
private fun TopBarIconButton(
    symbol: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
    ) {
        Text(
            text = symbol,
            color = Color.Black,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@ComposePreview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryTopBarPreview() {
    GalleryTopBar(
        title = "다낭 4박5일",
        onBackClick = {},
        onMoreClick = {},
    )
}
