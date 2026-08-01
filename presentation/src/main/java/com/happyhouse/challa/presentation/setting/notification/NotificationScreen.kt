package com.happyhouse.challa.presentation.setting.notification

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaNavigationIconButton
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigation
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigationVariant
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.layout.ChallaScaffold
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce

@Composable
fun NotificationScreen(
    systemNotificationsEnabled: Boolean,
    serviceNotificationsEnabled: Boolean,
    onBackClick: () -> Unit,
    onSystemNotificationSettingClick: () -> Unit,
    onServiceNotificationEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ChallaScaffold(
        modifier = modifier,
        topBar = {
            ChallaTopNavigation(
                title = stringResource(R.string.notification_title),
                variant = ChallaTopNavigationVariant.SUB,
                leadingIcon = {
                    ChallaNavigationIconButton(
                        icon = ChallaIcons.Left,
                        onClick = onBackClick,
                        contentDescription = stringResource(R.string.notification_back_description),
                    )
                },
            )
        },
    ) { contentPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!systemNotificationsEnabled) {
                SystemNotificationDisabledBanner(
                    onClick = onSystemNotificationSettingClick,
                )
            }

            ServiceNotificationCard(
                enabled = serviceNotificationsEnabled,
                onEnabledChange = onServiceNotificationEnabledChange,
            )
        }
    }
}

@Composable
private fun SystemNotificationDisabledBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ChallaTheme.colors.backgroundLevel3)
                .noRippleClickOnce(
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(ChallaIcons.Error),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = ChallaTheme.colors.statusDestructive,
        )

        Text(
            text = stringResource(R.string.notification_system_disabled),
            modifier =
                Modifier
                    .padding(start = 4.dp)
                    .weight(1f),
            color = ChallaTheme.colors.labelSubtle,
            style = ChallaTheme.typography.bodyXSmall.medium,
        )

        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(ChallaIcons.Right),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = ChallaTheme.colors.labelAlternative,
            )
        }
    }
}

@Composable
private fun ServiceNotificationCard(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ChallaTheme.colors.backgroundLevel1)
                .padding(start = 24.dp, top = 23.dp, end = 20.dp, bottom = 23.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.notification_service_title),
                color = ChallaTheme.colors.labelSubtle,
                style = ChallaTheme.typography.bodyMedium.medium,
            )
            Text(
                text = stringResource(R.string.notification_service_description),
                color = ChallaTheme.colors.labelAlternative,
                style = ChallaTheme.typography.bodyXSmall.medium,
            )
        }

        NotificationSwitch(
            checked = enabled,
            onCheckedChange = onEnabledChange,
        )
    }
}

@Composable
private fun NotificationSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 3.dp,
        label = "notificationSwitchThumbOffset",
    )
    val trackColor by animateColorAsState(
        targetValue =
            if (checked) {
                ChallaTheme.colors.labelNeutral
            } else {
                ChallaTheme.colors.backgroundLevel4
            },
        label = "notificationSwitchTrackColor",
    )

    Box(
        modifier =
            modifier
                .size(48.dp)
                .toggleable(
                    value = checked,
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(width = 47.dp, height = 26.dp)
                    .background(trackColor, CircleShape),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier =
                    Modifier
                        .offset { IntOffset(x = thumbOffset.roundToPx(), y = 0) }
                        .size(20.dp)
                        .background(ChallaTheme.colors.staticWhite, CircleShape),
            )
        }
    }
}

@Preview
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun NotificationScreenPreview() {
    NotificationScreen(
        systemNotificationsEnabled = false,
        serviceNotificationsEnabled = true,
        onBackClick = {},
        onSystemNotificationSettingClick = {},
        onServiceNotificationEnabledChange = {},
    )
}
