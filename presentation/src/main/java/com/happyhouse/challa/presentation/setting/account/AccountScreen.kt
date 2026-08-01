package com.happyhouse.challa.presentation.setting.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaNavigationIconButton
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigation
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigationVariant
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonSize
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonVariant
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaTextButton
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.layout.ChallaScaffold
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce

@Composable
fun AccountScreen(
    nickname: String,
    maskedEmail: String,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChallaScaffold(
        modifier = modifier,
        topBar = {
            ChallaTopNavigation(
                title = stringResource(R.string.account_title),
                variant = ChallaTopNavigationVariant.SUB,
                leadingIcon = {
                    ChallaNavigationIconButton(
                        icon = ChallaIcons.Left,
                        onClick = onBackClick,
                        contentDescription = stringResource(R.string.account_back_description),
                    )
                },
            )
        },
    ) { contentPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AccountProfile(
                    nickname = nickname,
                    maskedEmail = maskedEmail,
                )

                LogoutCard(onClick = onLogoutClick)
            }

            WithdrawButton(
                onClick = onWithdrawClick,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun AccountProfile(
    nickname: String,
    maskedEmail: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.img_setting_profile_placeholder),
            contentDescription = null,
            modifier = Modifier.size(68.dp),
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = nickname,
                color = ChallaTheme.colors.labelNormal,
                textAlign = TextAlign.Center,
                style = ChallaTheme.typography.bodyMedium.bold,
            )
            Text(
                text = maskedEmail,
                color = ChallaTheme.colors.labelAlternative,
                textAlign = TextAlign.Center,
                style = ChallaTheme.typography.bodyMedium.regular,
            )
        }
    }
}

@Composable
private fun LogoutCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ChallaTheme.colors.backgroundLevel1)
                .noRippleClickOnce(
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(start = 24.dp, top = 22.dp, end = 16.dp, bottom = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            painter = painterResource(ChallaIcons.SignOut),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = ChallaTheme.colors.labelAlternative,
        )
        Text(
            text = stringResource(R.string.account_logout),
            modifier = Modifier.weight(1f),
            color = ChallaTheme.colors.labelSubtle,
            style = ChallaTheme.typography.bodyMedium.medium,
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
private fun WithdrawButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChallaTextButton(
        text = stringResource(R.string.account_withdraw),
        onClick = onClick,
        modifier = modifier,
        variant = ChallaButtonVariant.TRANSPARENT,
        size = ChallaButtonSize.LARGE,
        contentColor = ChallaTheme.colors.labelAlternative,
    )
}

@Preview
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun AccountScreenPreview() {
    AccountScreen(
        nickname = "나는야멋쟁이토마토",
        maskedEmail = "hap****@naver.com",
        onBackClick = {},
        onLogoutClick = {},
        onWithdrawClick = {},
    )
}
