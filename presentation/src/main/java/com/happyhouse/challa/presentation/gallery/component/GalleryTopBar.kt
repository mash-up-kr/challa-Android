package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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
@Composable
fun GalleryTopBar(
    title: String,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
    ) {
        TopBarIconButton(
            modifier = Modifier.align(Alignment.CenterStart),
            symbol = stringResource(R.string.gallery_back_icon),
            contentDescription = stringResource(R.string.gallery_back_description),
            onClick = onBackClick,
        )

        Text(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 48.dp),
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )

        TopBarIconButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            symbol = stringResource(R.string.gallery_more_icon),
            contentDescription = stringResource(R.string.gallery_more_description),
            onClick = onMoreClick,
        )
    }
}

@Composable
private fun TopBarIconButton(
    symbol: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(
                    role = Role.Button,
                    onClickLabel = contentDescription,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
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
