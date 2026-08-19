package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaCardItem
import com.happyhouse.challa.presentation.designsystem.component.ChallaCardType
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.gallery.contract.GalleryFilmSlotUiModel
import com.happyhouse.challa.presentation.gallery.contract.GalleryPhotoUiModel
import com.happyhouse.challa.presentation.gallery.previewGalleryFilmSlots
import com.happyhouse.challa.presentation.gallery.previewGalleryPhotos
import kotlinx.collections.immutable.ImmutableList
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private const val GALLERY_COLUMN_COUNT = 4

/** 받아둔 사진의 끝에서 이만큼 남았을 때 다음 페이지를 미리 요청한다. */
private const val LOAD_MORE_PREFETCH_ITEM_COUNT = GALLERY_COLUMN_COUNT * 2

private val GalleryGridSpacing = 10.dp
private val GalleryGridHorizontalPadding = 16.dp
private val GalleryGridVerticalPadding = 20.dp

/**
 * 인화 전: 번호만 있는 빈 필름 슬롯 그리드
 *
 * @param state 인화 완료 그리드와 같은 것을 넘기면 전환해도 스크롤 위치가 유지된다.
 * @param loadedPhotoCount 블러로 그릴 사진을 실제로 받아둔 칸 수. 필름은 전체 칸을 미리 그려
 *   항목 수가 고정이라, 스크롤 끝이 아니라 이 지점을 기준으로 다음 페이지를 요청한다.
 * @param extraBottomPadding 위에 떠 있는 하단 바에 마지막 줄이 가리지 않도록 더하는 여백
 */
@Composable
fun GalleryFilmSlotGrid(
    slots: ImmutableList<GalleryFilmSlotUiModel>,
    loadedPhotoCount: Int,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    extraBottomPadding: Dp = 0.dp,
) {
    GalleryGridLayout(
        modifier = modifier,
        state = state,
        loadedItemCount = loadedPhotoCount,
        onLoadMore = onLoadMore,
        extraBottomPadding = extraBottomPadding,
    ) {
        items(
            items = slots,
            key = { slot -> slot.order },
        ) { slot ->
            val slotState = slot.state

            ChallaCardItem(
                order = slot.order,
                type =
                    when (slotState) {
                        GalleryFilmSlotUiModel.State.Empty -> ChallaCardType.NotCaptured
                        is GalleryFilmSlotUiModel.State.Captured ->
                            ChallaCardType.PrintWaiting(imageUrl = slotState.imageUrl)
                    },
                contentDescription =
                    when (slotState) {
                        GalleryFilmSlotUiModel.State.Empty ->
                            stringResource(R.string.gallery_empty_slot_description, slot.order)

                        is GalleryFilmSlotUiModel.State.Captured ->
                            stringResource(R.string.gallery_film_slot_description, slot.order)
                    },
            )
        }
    }
}

/**
 * 인화 완료: 공개된 방 사진 그리드
 *
 * @param state 인화 전 그리드와 같은 것을 넘기면 전환해도 스크롤 위치가 유지된다.
 * @param extraBottomPadding 위에 떠 있는 하단 바에 마지막 줄이 가리지 않도록 더하는 여백
 */
@Composable
fun GalleryPhotoGrid(
    photos: ImmutableList<GalleryPhotoUiModel>,
    onPhotoClick: (Long) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    extraBottomPadding: Dp = 0.dp,
) {
    GalleryGridLayout(
        modifier = modifier,
        state = state,
        loadedItemCount = photos.size,
        onLoadMore = onLoadMore,
        extraBottomPadding = extraBottomPadding,
    ) {
        items(
            items = photos,
            key = { photo -> photo.id },
        ) { photo ->
            ChallaCardItem(
                order = photo.order,
                type = ChallaCardType.Printed(imageUrl = photo.imageUrl),
                contentDescription = stringResource(R.string.gallery_photo_content_description, photo.order),
                onClickLabel = stringResource(R.string.gallery_open_photo),
                onClick = { onPhotoClick(photo.id) },
            )
        }
    }
}

@Composable
private fun GalleryGridLayout(
    loadedItemCount: Int,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    extraBottomPadding: Dp = 0.dp,
    content: LazyGridScope.() -> Unit,
) {
    LoadMoreEffect(
        gridState = state,
        loadedItemCount = loadedItemCount,
        onLoadMore = onLoadMore,
    )

    LazyVerticalGrid(
        modifier = modifier,
        state = state,
        columns = GridCells.Fixed(GALLERY_COLUMN_COUNT),
        contentPadding =
            PaddingValues(
                start = GalleryGridHorizontalPadding,
                end = GalleryGridHorizontalPadding,
                top = GalleryGridVerticalPadding,
                bottom = GalleryGridVerticalPadding + extraBottomPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(GalleryGridSpacing),
        verticalArrangement = Arrangement.spacedBy(GalleryGridSpacing),
        content = content,
    )
}

/** 받아온 뒤에도 사용자가 끝에 머물러 있으면 이어서 요청해야 하므로 [loadedItemCount]도 키로 둔다. */
@Composable
private fun LoadMoreEffect(
    gridState: LazyGridState,
    loadedItemCount: Int,
    onLoadMore: () -> Unit,
) {
    val reachedLoadMoreThreshold by remember(gridState, loadedItemCount) {
        derivedStateOf {
            val lastVisibleIndex =
                gridState.layoutInfo.visibleItemsInfo
                    .lastOrNull()
                    ?.index ?: return@derivedStateOf false

            lastVisibleIndex >= loadedItemCount - LOAD_MORE_PREFETCH_ITEM_COUNT
        }
    }

    LaunchedEffect(reachedLoadMoreThreshold, loadedItemCount) {
        if (reachedLoadMoreThreshold) onLoadMore()
    }
}

@ComposePreview(showBackground = true, widthDp = 390)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryFilmSlotGridPreview() {
    val slots = previewGalleryFilmSlots()

    GalleryFilmSlotGrid(
        slots = slots,
        loadedPhotoCount = slots.size,
        onLoadMore = {},
    )
}

@ComposePreview(showBackground = true, widthDp = 390)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryPhotoGridPreview() {
    GalleryPhotoGrid(
        photos = previewGalleryPhotos(count = 12),
        onPhotoClick = {},
        onLoadMore = {},
    )
}
