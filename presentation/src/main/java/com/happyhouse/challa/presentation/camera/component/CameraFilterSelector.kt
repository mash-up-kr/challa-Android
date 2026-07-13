package com.happyhouse.challa.presentation.camera.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
fun CameraFilterSelector(
    selectedFilterIndex: Int,
    onFilterClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
    ) {
        repeat(5) { index ->
            val selected = index == selectedFilterIndex
            Text(
                text = stringResource(R.string.camera_filter_name, index + 1),
                modifier =
                    Modifier.clickable(
                        role = Role.Tab,
                        onClick = { onFilterClick(index) },
                    ),
                color = if (selected) ChallaTheme.colors.labelNormal else ChallaTheme.colors.labelAlternative,
                style = ChallaTheme.typography.bodyMedium.bold,
            )
        }
    }
}

@ComposePreview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CameraFilterSelectorPreview() {
    CameraFilterSelector(
        selectedFilterIndex = 2,
        onFilterClick = {},
    )
}
