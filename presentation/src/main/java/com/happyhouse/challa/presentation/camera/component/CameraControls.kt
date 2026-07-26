package com.happyhouse.challa.presentation.camera.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.foundation.icon.ChallaIconSize
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
internal fun CameraControls(
    isFlashEnabled: Boolean,
    isCameraSwitchEnabled: Boolean,
    shutterEnabled: Boolean,
    onFlashClick: () -> Unit,
    onSwitchCameraClick: () -> Unit,
    onShutterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
    ) {
        RoundIconButton(
            iconRes = if (isFlashEnabled) R.drawable.ic_light_on else R.drawable.ic_light_off,
            contentDescription = stringResource(R.string.camera_flash_description),
            onClick = onFlashClick,
        )
        ShutterButton(
            enabled = shutterEnabled,
            onClick = onShutterClick,
        )
        RoundIconButton(
            iconRes = R.drawable.ic_switch_camera,
            contentDescription = stringResource(R.string.camera_switch_description),
            enabled = isCameraSwitchEnabled,
            onClick = onSwitchCameraClick,
        )
    }
}

@Composable
private fun RoundIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(ChallaTheme.colors.backgroundLevel4)
                .noRippleClickOnce(
                    enabled = enabled,
                    role = Role.Button,
                    onClickLabel = contentDescription,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(ChallaIconSize.V24.dp),
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = ChallaTheme.colors.labelNeutral,
        )
    }
}

@Composable
private fun ShutterButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(4.dp, ChallaTheme.colors.primaryYellow, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(ChallaTheme.colors.labelNormal)
                    .noRippleClickOnce(
                        enabled = enabled,
                        role = Role.Button,
                        onClickLabel = stringResource(R.string.camera_shutter_description),
                        onClick = onClick,
                    ),
        )
    }
}

@ComposePreview(showBackground = true)
@Composable
private fun CameraControlsPreview() {
    ChallaTheme {
        Box(
            modifier =
                Modifier
                    .background(Color.Black)
                    .padding(12.dp),
        ) {
            CameraControls(
                isFlashEnabled = false,
                isCameraSwitchEnabled = true,
                shutterEnabled = true,
                onFlashClick = {},
                onSwitchCameraClick = {},
                onShutterClick = {},
            )
        }
    }
}
