package com.happyhouse.challa.presentation.setting.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaNavigationIconButton
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigation
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigationVariant
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.layout.ChallaScaffold
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.challaBackgroundGlow
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import com.happyhouse.challa.presentation.setting.theme.model.ThemeUiModel

@Composable
fun ThemeScreen(
    selectedTheme: ThemeUiModel,
    onBackClick: () -> Unit,
    onThemeClick: (ThemeUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    ChallaScaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ChallaTheme.colors.backgroundSurface,
        topBar = {
            ChallaTopNavigation(
                title = stringResource(R.string.theme_title),
                variant = ChallaTopNavigationVariant.SUB,
                leadingIcon = {
                    ChallaNavigationIconButton(
                        icon = ChallaIcons.Left,
                        onClick = onBackClick,
                        contentDescription = stringResource(R.string.theme_back_description),
                    )
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(ChallaTheme.colors.backgroundSurface)
                    .challaBackgroundGlow(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(
                            color = ChallaTheme.colors.backgroundLevel1,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(start = 24.dp, top = 10.dp, end = 20.dp, bottom = 10.dp),
            ) {
                ThemeUiModel.entries.forEach { theme ->
                    ThemeOptionRow(
                        theme = theme,
                        selected = theme == selectedTheme,
                        onClick = { onThemeClick(theme) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    theme: ThemeUiModel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .semantics { this.selected = selected }
                .noRippleClickOnce(
                    role = Role.RadioButton,
                    onClick = onClick,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier.size(18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(14.dp)
                        .background(theme.color(), CircleShape),
            )
        }

        Text(
            text = stringResource(theme.titleRes),
            modifier = Modifier.weight(1f),
            color = ChallaTheme.colors.labelSubtle,
            style = ChallaTheme.typography.bodyMedium.medium,
        )

        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(ChallaIcons.Check),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint =
                    if (selected) {
                        ChallaTheme.colors.labelNormal
                    } else {
                        ChallaTheme.colors.labelDisable
                    },
            )
        }
    }
}

@Composable
private fun ThemeUiModel.color(): Color =
    when (this) {
        ThemeUiModel.LEMONADE -> ChallaTheme.colors.primaryYellow
        ThemeUiModel.RASPBERRY -> ChallaTheme.colors.primaryPink
        ThemeUiModel.ORANGE -> ChallaTheme.colors.primaryOrange
        ThemeUiModel.CIDER -> ChallaTheme.colors.primarySky
        ThemeUiModel.BLUEBERRY -> ChallaTheme.colors.primaryBlue
        ThemeUiModel.ACAI_BOWL -> ChallaTheme.colors.primaryPurple
    }

@Preview
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun ThemeScreenPreview() {
    var selectedTheme by remember { mutableStateOf(ThemeUiModel.LEMONADE) }

    ThemeScreen(
        selectedTheme = selectedTheme,
        onBackClick = {},
        onThemeClick = { selectedTheme = it },
    )
}
