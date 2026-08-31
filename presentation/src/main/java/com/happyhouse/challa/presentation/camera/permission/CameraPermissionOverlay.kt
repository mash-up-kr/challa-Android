package com.happyhouse.challa.presentation.camera.permission

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaProgressIndicator
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonSize
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaTextButton
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

/** 카메라 권한을 획득하지 못한 동안 Overlay에 표시할 상호 배타적인 UI 상태입니다. */
@Immutable
internal sealed interface CameraPermissionOverlayState {
    /** 시스템 권한 보유 여부를 확인하고 있는 상태입니다. */
    data object Checking : CameraPermissionOverlayState

    /** 시스템 권한 다이얼로그를 다시 요청할 수 있는 상태입니다. */
    data object Requestable : CameraPermissionOverlayState

    /** 시스템 다이얼로그 대신 앱 설정에서 권한을 허용해야 하는 상태입니다. */
    data object PermanentlyDenied : CameraPermissionOverlayState
}

/**
 * 카메라 권한을 사용할 수 없을 때 현재 [state]에 맞는 안내 UI를 표시합니다.
 *
 * [onRequestPermissionClick]은 요청 가능한 상태에서는 시스템 권한 다이얼로그를, 영구 거부
 * 상태에서는 앱 설정 화면을 여는 동작으로 연결됩니다.
 */
@Composable
internal fun CameraPermissionOverlay(
    state: CameraPermissionOverlayState,
    onRequestPermissionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(ChallaTheme.colors.backgroundLevel1),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            CameraPermissionOverlayState.Checking -> ChallaProgressIndicator()
            CameraPermissionOverlayState.Requestable -> {
                CameraPermissionRequestContent(
                    descriptionRes = R.string.camera_permission_required_description,
                    buttonTextRes = R.string.camera_permission_request_button,
                    onRequestPermissionClick = onRequestPermissionClick,
                )
            }

            CameraPermissionOverlayState.PermanentlyDenied -> {
                CameraPermissionRequestContent(
                    descriptionRes = R.string.camera_permission_settings_description,
                    buttonTextRes = R.string.camera_permission_settings_button,
                    onRequestPermissionClick = onRequestPermissionClick,
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionRequestContent(
    @StringRes descriptionRes: Int,
    @StringRes buttonTextRes: Int,
    onRequestPermissionClick: () -> Unit,
) {
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
            text = stringResource(descriptionRes),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            color = ChallaTheme.colors.labelNeutral,
            textAlign = TextAlign.Center,
            style = ChallaTheme.typography.bodyXSmall.medium,
        )
        ChallaTextButton(
            text = stringResource(buttonTextRes),
            onClick = onRequestPermissionClick,
            modifier = Modifier.padding(top = 40.dp),
            size = ChallaButtonSize.MEDIUM,
        )
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
            state = CameraPermissionOverlayState.Checking,
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
            state = CameraPermissionOverlayState.Requestable,
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
            state = CameraPermissionOverlayState.PermanentlyDenied,
            onRequestPermissionClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
