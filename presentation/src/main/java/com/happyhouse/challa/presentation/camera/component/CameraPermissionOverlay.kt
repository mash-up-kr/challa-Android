package com.happyhouse.challa.presentation.camera.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

@Composable
fun CameraPermissionOverlay(
    isCheckingPermission: Boolean,
    onRequestPermissionClick: () -> Unit,
    modifier: Modifier = Modifier,
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
                    text = stringResource(R.string.camera_permission_required_description),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    color = ChallaTheme.colors.labelNeutral,
                    textAlign = TextAlign.Center,
                    style = ChallaTheme.typography.bodyXSmall.medium,
                )
                Button(
                    onClick = onRequestPermissionClick,
                    modifier = Modifier.padding(top = 40.dp),
                ) {
                    Text(text = stringResource(R.string.camera_permission_request_button))
                }
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
            modifier = Modifier.fillMaxSize(),
            isCheckingPermission = true,
            onRequestPermissionClick = {},
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
            modifier = Modifier.fillMaxSize(),
            isCheckingPermission = false,
            onRequestPermissionClick = {},
        )
    }
}
