package com.happyhouse.challa.presentation.setting.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaDrawer
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonSize
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaButtonVariant
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaTextButton
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

@Composable
internal fun AccountWithdrawalConfirmationDrawer(
    isWithdrawing: Boolean,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AccountWithdrawalConfirmationDrawerContent(
        title = stringResource(R.string.account_withdraw_warning_title),
        description = stringResource(R.string.account_withdraw_warning_description),
        confirmText = stringResource(R.string.account_withdraw_confirm),
        closeText = stringResource(R.string.account_withdraw_close),
        isWithdrawing = isWithdrawing,
        onConfirmClick = onConfirmClick,
        onDismissRequest = onDismissRequest,
    )
}

@Composable
private fun AccountWithdrawalConfirmationDrawerContent(
    title: String,
    description: String,
    confirmText: String,
    closeText: String,
    isWithdrawing: Boolean,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    ChallaDrawer(
        onDismissRequest = {
            if (!isWithdrawing) onDismissRequest()
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                color = ChallaTheme.colors.labelNormal,
                textAlign = TextAlign.Center,
                style = ChallaTheme.typography.headingXSmall.bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                color = ChallaTheme.colors.labelAlternative,
                textAlign = TextAlign.Center,
                style = ChallaTheme.typography.bodyMedium.regular,
            )
            Spacer(modifier = Modifier.height(28.dp))
            ChallaTextButton(
                text = confirmText,
                onClick = onConfirmClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isWithdrawing,
                containerColor = ChallaTheme.colors.statusDestructive.takeUnless { isWithdrawing },
                contentColor = ChallaTheme.colors.labelNormal.takeUnless { isWithdrawing },
            )
            Spacer(modifier = Modifier.height(8.dp))
            ChallaTextButton(
                text = closeText,
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isWithdrawing,
                variant = ChallaButtonVariant.TRANSPARENT,
                size = ChallaButtonSize.MEDIUM,
            )
        }
    }
}

@Composable
internal fun AccountWithdrawalCompletedDrawer(onConfirmClick: () -> Unit) {
    AccountWithdrawalCompletedDrawerContent(
        title = stringResource(R.string.account_withdraw_completed_title),
        confirmText = stringResource(R.string.account_withdraw_completed_confirm),
        onConfirmClick = onConfirmClick,
    )
}

@Composable
private fun AccountWithdrawalCompletedDrawerContent(
    title: String,
    confirmText: String,
    onConfirmClick: () -> Unit,
) {
    ChallaDrawer(onDismissRequest = {}) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                color = ChallaTheme.colors.labelNormal,
                textAlign = TextAlign.Center,
                style = ChallaTheme.typography.headingXSmall.bold,
            )
            Spacer(modifier = Modifier.height(28.dp))
            ChallaTextButton(
                text = confirmText,
                onClick = onConfirmClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "Account withdrawal - confirmation")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun AccountWithdrawalConfirmationDrawerPreview() {
    AccountWithdrawalConfirmationDrawerContent(
        title = "모든 기록이 사라져요",
        description = "탈퇴 시 참여 중이던 방에서 자동으로 나가져요",
        confirmText = "그래도 탈퇴하기",
        closeText = "닫기",
        isWithdrawing = false,
        onConfirmClick = {},
        onDismissRequest = {},
    )
}

@Preview(name = "Account withdrawal - completed")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun AccountWithdrawalCompletedDrawerPreview() {
    AccountWithdrawalCompletedDrawerContent(
        title = "회원 탈퇴가\n정상적으로 완료 됐어요",
        confirmText = "확인",
        onConfirmClick = {},
    )
}
