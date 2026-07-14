package com.happyhouse.challa.presentation.camera.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonSize
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaTextButton
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

@Composable
internal fun CameraPermissionOverlay(
    isCheckingPermission: Boolean,
    onRequestPermissionClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPermanentlyDenied: Boolean = false,
) {
    Box(
        modifier = modifier.background(ChallaTheme.colors.backgroundLevel1),
        contentAlignment = Alignment.Center,
    ) {
        if (isCheckingPermission) {
            CircularProgressIndicator()
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.camera_permission_required_title),
                    color = ChallaTheme.colors.labelNormal,
                    textAlign = TextAlign.Center,
                    style = ChallaTheme.typography.bodyLarge.bold,
                )
                Text(
                    text =
                        stringResource(
                            if (isPermanentlyDenied) {
                                R.string.camera_permission_settings_description
                            } else {
                                R.string.camera_permission_required_description
                            },
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    color = ChallaTheme.colors.labelNeutral,
                    textAlign = TextAlign.Center,
                    style = ChallaTheme.typography.bodyXSmall.medium,
                )
                ChallaTextButton(
                    text =
                        stringResource(
                            if (isPermanentlyDenied) {
                                R.string.camera_permission_settings_button
                            } else {
                                R.string.camera_permission_request_button
                            },
                        ),
                    onClick = onRequestPermissionClick,
                    modifier = Modifier.padding(top = 40.dp),
                    size = ChallaButtonSize.MEDIUM,
                )
            }
        }
    }
}

@Preview(
    name = "권한 확인 중",
    showBackground = true,
    widthDp = 265,
    heightDp = 353,
)
@Composable
private fun CameraPermissionCheckingPreview() {
    ChallaTheme {
        CameraPermissionOverlay(
            isCheckingPermission = true,
            onRequestPermissionClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(
    name = "권한 요청",
    showBackground = true,
    widthDp = 265,
    heightDp = 353,
)
@Composable
private fun CameraPermissionRequestPreview() {
    ChallaTheme {
        CameraPermissionOverlay(
            isCheckingPermission = false,
            onRequestPermissionClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(
    name = "권한 영구 거부",
    showBackground = true,
    widthDp = 265,
    heightDp = 353,
)
@Composable
private fun CameraPermissionPermanentlyDeniedPreview() {
    ChallaTheme {
        CameraPermissionOverlay(
            isCheckingPermission = false,
            onRequestPermissionClick = {},
            modifier = Modifier.fillMaxSize(),
            isPermanentlyDenied = true,
        )
    }
}
