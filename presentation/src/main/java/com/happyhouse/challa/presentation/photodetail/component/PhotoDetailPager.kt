package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailState.PhotoInfo
import com.happyhouse.challa.presentation.photodetail.previewPhotoDetailPhotos
import timber.log.Timber
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private val PhotoHorizontalPadding = 16.dp
private val IndicatorTopPadding = 16.dp

private val PhotoPageSpacing = PhotoHorizontalPadding * 2

@Composable
fun PhotoDetailPager(
    loaded: PhotoInfo.Loaded,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val photos = loaded.photos

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        // weight를 쓰면 Column이 높이를 다 차지하므로 가운데 정렬은 Arrangement로 잡는다.
        verticalArrangement = Arrangement.Center,
    ) {
        HorizontalPager(
            // 키보드가 올라오면 쓸 수 있는 높이가 줄어든다. 높이를 고정하면 그만큼 잘리므로
            // 남는 높이에 맞춰 줄어들되 PhotoCardHeight는 넘지 않게 한다.
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f, fill = false)
                    .heightIn(max = PhotoCardHeight),
            state = pagerState,
            contentPadding = PaddingValues(horizontal = PhotoHorizontalPadding),
            pageSpacing = PhotoPageSpacing,
            // 페이지 수는 화면이, key는 이 목록이 들고 있어 이어 받는 순간 한 프레임 어긋날 수 있다.
            // 아직 안 받은 자리는 사진 id와 겹치지 않게 음수로 채운다.
            key = { page -> photos.getOrNull(page)?.id ?: -(page.toLong() + 1) },
        ) { page ->
            val photo =
                photos.getOrNull(page) ?: run {
                    Timber.d("아직 받지 못한 자리라 이 프레임은 비워둡니다. page=$page, size=${photos.size}")
                    return@HorizontalPager
                }

            PhotoDetailPage(
                modifier = Modifier.fillMaxSize(),
                photo = photo,
                reactions = loaded.reactionsOf(photo.id),
                burst = loaded.burst,
            )
        }

        PhotoDetailPageIndicator(
            modifier = Modifier.padding(top = IndicatorTopPadding),
            pagerState = pagerState,
        )
    }
}

@ComposePreview(showBackground = true, heightDp = 730)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPagerPreview() {
    val photos = previewPhotoDetailPhotos(count = 24)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        PhotoDetailPager(
            modifier = Modifier.fillMaxWidth(),
            loaded = PhotoInfo.Loaded(photos),
            pagerState = rememberPagerState { photos.size },
        )
    }
}

@ComposePreview(
    showBackground = true,
    heightDp = 730,
    name = "PhotoDetailPager - 사진 1장",
)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPagerSinglePhotoPreview() {
    val photos = previewPhotoDetailPhotos(count = 1)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        PhotoDetailPager(
            modifier = Modifier.fillMaxWidth(),
            loaded = PhotoInfo.Loaded(photos),
            pagerState = rememberPagerState { photos.size },
        )
    }
}
