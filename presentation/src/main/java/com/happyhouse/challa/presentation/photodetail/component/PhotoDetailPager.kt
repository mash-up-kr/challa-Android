package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel
import com.happyhouse.challa.presentation.photodetail.previewPhotoDetailPhotos
import kotlinx.collections.immutable.ImmutableList
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

// Figma(390x844) 기준 상단바 아래 영역 730dp = 위 여백 94 + 카드·인디케이터 503 + 아래 여백 133.
// 화면 높이가 달라져도 같은 비율로 배치되도록 남는 공간을 두 여백에 나눠 준다.
private const val TOP_SPACE_WEIGHT = 94f
private const val BOTTOM_SPACE_WEIGHT = 133f

private val PhotoHorizontalPadding = 16.dp
private val IndicatorTopPadding = 16.dp

private val PhotoPageSpacing = PhotoHorizontalPadding * 2

@Composable
fun PhotoDetailPager(
    photos: ImmutableList<PhotoDetailUiModel>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(TOP_SPACE_WEIGHT))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val photoWidth = maxWidth - PhotoHorizontalPadding * 2

            HorizontalPager(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(photoWidth / PHOTO_DETAIL_ASPECT_RATIO),
                state = pagerState,
                contentPadding = PaddingValues(horizontal = PhotoHorizontalPadding),
                pageSpacing = PhotoPageSpacing,
                key = { page -> photos[page].id },
            ) { page ->
                PhotoDetailPage(
                    modifier = Modifier.fillMaxSize(),
                    photo = photos[page],
                )
            }
        }

        PhotoDetailPageIndicator(
            modifier = Modifier.padding(top = IndicatorTopPadding),
            pagerState = pagerState,
        )

        Spacer(modifier = Modifier.weight(BOTTOM_SPACE_WEIGHT))
    }
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF111111, heightDp = 730)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPagerPreview() {
    val photos = previewPhotoDetailPhotos(count = 24)

    PhotoDetailPager(
        modifier = Modifier.fillMaxSize(),
        photos = photos,
        pagerState = rememberPagerState { photos.size },
    )
}

@ComposePreview(
    showBackground = true,
    backgroundColor = 0xFF111111,
    heightDp = 730,
    name = "PhotoDetailPager - 사진 1장",
)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPagerSinglePhotoPreview() {
    val photos = previewPhotoDetailPhotos(count = 1)

    PhotoDetailPager(
        modifier = Modifier.fillMaxSize(),
        photos = photos,
        pagerState = rememberPagerState { photos.size },
    )
}
