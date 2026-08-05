package com.happyhouse.challa.presentation.camera.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.camera.model.CameraFilterUiModel
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
internal fun CameraFilterSelector(
    filters: ImmutableList<CameraFilterUiModel>,
    selectedFilterIndex: Int,
    onFilterClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (filters.size >= SCROLLABLE_FILTER_COUNT) {
        val listState = rememberLazyListState()

        LazyRow(
            modifier =
                modifier
                    .fillMaxWidth()
                    .fadingHorizontalEdges(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            items(
                count = filters.size,
                key = { index ->
                    when (val filter = filters[index]) {
                        CameraFilterUiModel.Original -> "original"
                        is CameraFilterUiModel.Remote -> filter.fileUrl
                    }
                },
            ) { index ->
                CameraFilterItem(
                    filter = filters[index],
                    selected = index == selectedFilterIndex,
                    onClick = { onFilterClick(index) },
                )
            }
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
        ) {
            filters.forEachIndexed { index, filter ->
                CameraFilterItem(
                    filter = filter,
                    selected = index == selectedFilterIndex,
                    onClick = { onFilterClick(index) },
                )
            }
        }
    }
}

@Composable
private fun CameraFilterItem(
    filter: CameraFilterUiModel,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text =
            when (filter) {
                CameraFilterUiModel.Original -> stringResource(R.string.camera_filter_original)
                is CameraFilterUiModel.Remote -> filter.name
            },
        modifier =
            Modifier.noRippleClickOnce(
                role = Role.Tab,
                onClick = onClick,
            ),
        color = if (selected) ChallaTheme.colors.labelNormal else ChallaTheme.colors.labelAlternative,
        style = ChallaTheme.typography.bodyMedium.bold,
    )
}

private fun Modifier.fadingHorizontalEdges(): Modifier =
    graphicsLayer {
        compositingStrategy = CompositingStrategy.Offscreen
    }.drawWithContent {
        drawContent()

        drawRect(
            brush =
                Brush.horizontalGradient(
                    colorStops =
                        arrayOf(
                            0f to Color.Transparent,
                            0.15f to Color.Black,
                            0.5f to Color.Black,
                            0.85f to Color.Black,
                            1f to Color.Transparent,
                        ),
                ),
            blendMode = BlendMode.DstIn,
        )
    }

@ComposePreview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CameraFilterSelectorPreview() {
    CameraFilterSelector(
        filters =
            persistentListOf(
                CameraFilterUiModel.Original,
                CameraFilterUiModel.Remote("필터1", "https://example.com/filter1.cube"),
                CameraFilterUiModel.Remote("필터2", "https://example.com/filter2.cube"),
            ),
        selectedFilterIndex = 0,
        onFilterClick = {},
    )
}

private const val SCROLLABLE_FILTER_COUNT = 5
