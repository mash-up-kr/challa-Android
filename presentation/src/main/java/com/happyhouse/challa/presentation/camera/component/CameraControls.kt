package com.happyhouse.challa.presentation.camera.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
fun CameraTopBar(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    remainingCount: Int,
    totalCount: Int,
    isFlashOn: Boolean,
    isFlashEnabled: Boolean,
    onFlashClick: () -> Unit,
) {
    Box(modifier = modifier) {
        CameraIconButton(
            modifier = Modifier.align(Alignment.CenterStart),
            text = stringResource(R.string.camera_close_icon),
            textColor = Color.White,
            contentDescription = stringResource(R.string.camera_close_button),
            onClick = onBackClick,
        )

        CameraCounter(
            modifier = Modifier.align(Alignment.Center),
            remainingCount = remainingCount,
            totalCount = totalCount,
        )

        CameraIconButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            text = stringResource(R.string.camera_flash_icon),
            textColor =
                when {
                    !isFlashEnabled -> Color(0xFF666666)
                    isFlashOn -> Color(0xFFFFD000)
                    else -> Color(0xFFFFB300)
                },
            contentDescription = stringResource(R.string.camera_flash_description),
            enabled = isFlashEnabled,
            onClick = onFlashClick,
        )
    }
}

@Composable
private fun CameraCounter(
    modifier: Modifier = Modifier,
    remainingCount: Int,
    totalCount: Int,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF2B2B2B))
                .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.camera_remaining_counter, remainingCount, totalCount),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun CameraBottomBar(
    modifier: Modifier = Modifier,
    onSwitchCameraClick: () -> Unit,
    onShutterClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        CameraIconButton(
            modifier = Modifier.width(48.dp),
            text = stringResource(R.string.camera_switch_icon),
            textColor = Color.White,
            contentDescription = stringResource(R.string.camera_switch_description),
            onClick = onSwitchCameraClick,
        )

        ShutterButton(onClick = onShutterClick)

        PreviewEmptyLabel(modifier = Modifier.width(64.dp))
    }
}

@Composable
private fun ShutterButton(onClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.camera_shutter_description),
                    onClick = onClick,
                ),
    )
}

@Composable
private fun PreviewEmptyLabel(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = stringResource(R.string.camera_preview_empty),
        color = Color(0xFF8D8D8D),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        lineHeight = 18.sp,
    )
}

@Composable
private fun CameraIconButton(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color,
    contentDescription: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .size(48.dp)
                .clip(CircleShape)
                .semantics {
                    this.contentDescription = contentDescription
                }
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    onClickLabel = contentDescription,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF000000)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CameraTopBarPreview() {
    CameraTopBar(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color.Black)
                .padding(horizontal = 28.dp, vertical = 14.dp),
        onBackClick = {},
        remainingCount = 12,
        totalCount = 24,
        isFlashOn = true,
        isFlashEnabled = true,
        onFlashClick = {},
    )
}

@ComposePreview(showBackground = true, backgroundColor = 0xFF000000)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CameraBottomBarPreview() {
    CameraBottomBar(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color.Black)
                .padding(horizontal = 56.dp, vertical = 16.dp),
        onSwitchCameraClick = {},
        onShutterClick = {},
    )
}
