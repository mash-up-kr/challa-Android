package com.happyhouse.challa.presentation.setting.license

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaToastVisuals
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import kotlinx.coroutines.launch

@Composable
fun OpenSourceLicenseRoute(
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()
    val licenseUrl = stringResource(R.string.open_source_license_film_luts_url)
    val linkOpenFailureMessage = stringResource(R.string.open_source_license_link_open_failure)
    val destructiveIconTint = ChallaTheme.colors.statusDestructive

    OpenSourceLicenseScreen(
        onBackClick = onBackClick,
        onLicenseClick = {
            try {
                uriHandler.openUri(licenseUrl)
            } catch (_: IllegalArgumentException) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        ChallaToastVisuals(
                            message = linkOpenFailureMessage,
                            icon = ChallaIcons.Error,
                            iconTint = destructiveIconTint,
                        ),
                    )
                }
            }
        },
        snackbarHostState = snackbarHostState,
    )
}
