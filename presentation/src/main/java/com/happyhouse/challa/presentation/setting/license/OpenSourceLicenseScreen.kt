package com.happyhouse.challa.presentation.setting.license

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaNavigationIconButton
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigation
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigationVariant
import com.happyhouse.challa.presentation.designsystem.component.snackbar.ChallaToastVisuals
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.layout.ChallaScaffold
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import kotlinx.coroutines.launch

@Composable
fun OpenSourceLicenseScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val licenseUrl = stringResource(R.string.open_source_license_film_luts_url)
    val linkOpenFailureMessage = stringResource(R.string.open_source_license_link_open_failure)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val destructiveTint = ChallaTheme.colors.statusDestructive

    ChallaScaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ChallaTheme.colors.backgroundSurface,
        snackbarHostState = snackbarHostState,
        topBar = {
            ChallaTopNavigation(
                title = stringResource(R.string.open_source_license_title),
                variant = ChallaTopNavigationVariant.SUB,
                leadingIcon = {
                    ChallaNavigationIconButton(
                        icon = ChallaIcons.Left,
                        onClick = onBackClick,
                        contentDescription =
                            stringResource(R.string.open_source_license_back_description),
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .background(
                        color = ChallaTheme.colors.backgroundLevel1,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(start = 24.dp, top = 20.dp, end = 20.dp, bottom = 20.dp),
        ) {
            Text(
                text = stringResource(R.string.open_source_license_film_luts_name),
                color = ChallaTheme.colors.labelNormal,
                style = ChallaTheme.typography.bodyMedium.bold,
            )
            Text(
                text = stringResource(R.string.open_source_license_film_luts_notice),
                modifier = Modifier.padding(top = 10.dp),
                color = ChallaTheme.colors.labelNeutral,
                style = ChallaTheme.typography.bodySmall.medium,
            )
            Text(
                text = licenseUrl,
                modifier =
                    Modifier
                        .padding(top = 10.dp)
                        .noRippleClickOnce(
                            onClickLabel = licenseUrl,
                            onClick = {
                                try {
                                    uriHandler.openUri(licenseUrl)
                                } catch (_: IllegalArgumentException) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            ChallaToastVisuals(
                                                message = linkOpenFailureMessage,
                                                icon = ChallaIcons.Error,
                                                iconTint = destructiveTint,
                                            ),
                                        )
                                    }
                                }
                            },
                        ),
                color = ChallaTheme.colors.labelAlternative,
                style = ChallaTheme.typography.bodyXSmall.regular,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}

@Preview
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun OpenSourceLicenseScreenPreview() {
    OpenSourceLicenseScreen(onBackClick = {})
}
