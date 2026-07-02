package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailTopBar(
    title: String,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        // TODO: 디자인 확정 시 designsystem 토큰 색상으로 교체.
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black,
                scrolledContainerColor = Color.Unspecified,
                navigationIconContentColor = Color.White,
                titleContentColor = Color.White,
                actionIconContentColor = Color.White,
            ),
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
                symbol = stringResource(R.string.photo_detail_back_icon),
                contentDescription = stringResource(R.string.photo_detail_back_description),
                onClick = onBackClick,
            )
        },
        actions = {
            // TODO: 디자인 확정 후 메뉴 시트/드롭다운 연결
            TopBarIconButton(
                symbol = stringResource(R.string.photo_detail_more_icon),
                contentDescription = stringResource(R.string.photo_detail_more_description),
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
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@ComposePreview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailTopBarPreview() {
    PhotoDetailTopBar(
        title = "길고양이를찍으러가자",
        onBackClick = {},
        onMoreClick = {},
    )
}
