package com.happyhouse.challa.presentation.camera.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

@Composable
fun CameraTopBar(
    onBackClick: () -> Unit,
    remainingCount: Int,
    totalCount: Int,
    onFlashClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        RoundIconButton(
            iconRes = R.drawable.ic_close,
            contentDescription = stringResource(R.string.camera_close_button),
            onClick = onBackClick,
        )
        Text(
            text = stringResource(R.string.camera_remaining_counter, remainingCount, totalCount),
            color = ChallaTheme.colors.staticWhite,
            style = ChallaTheme.typography.bodyMedium.bold,
        )
        RoundIconButton(
            iconRes = R.drawable.ic_light_off,
            contentDescription = stringResource(R.string.camera_flash_description),
            onClick = onFlashClick,
        )
    }
}
