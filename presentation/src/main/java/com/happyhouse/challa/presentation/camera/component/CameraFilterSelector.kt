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
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
fun CameraFilterSelector(
    filterCount: Int,
    selectedFilterIndex: Int,
    onFilterClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (filterCount >= SCROLLABLE_FILTER_COUNT) {
        val listState = rememberLazyListState()

        LazyRow(
            modifier =
                modifier
                    .fillMaxWidth()
                    .fadingHorizontalEdges(),
            state = listState,
            contentPadding = PaddingValues(horizontal = FILTER_CONTENT_PADDING),
            horizontalArrangement = Arrangement.spacedBy(FILTER_ITEM_SPACING),
        ) {
            items(
                count = filterCount,
                key = { it },
            ) { index ->
                CameraFilterItem(
                    index = index,
                    selected = index == selectedFilterIndex,
                    onClick = { onFilterClick(index) },
                )
            }
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FILTER_ITEM_SPACING, Alignment.CenterHorizontally),
        ) {
            repeat(filterCount) { index ->
                CameraFilterItem(
                    index = index,
                    selected = index == selectedFilterIndex,
                    onClick = { onFilterClick(index) },
                )
            }
        }
    }
}

@Composable
private fun CameraFilterItem(
    index: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = stringResource(R.string.camera_filter_name, index + 1),
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
        filterCount = 10,
        selectedFilterIndex = 0,
        onFilterClick = {},
    )
}

private const val SCROLLABLE_FILTER_COUNT = 5
private val FILTER_ITEM_SPACING = 20.dp
private val FILTER_CONTENT_PADDING = 20.dp
