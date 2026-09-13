package com.happyhouse.challa.presentation.gallery.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaBottomSheet
import com.happyhouse.challa.presentation.designsystem.foundation.icon.ChallaIconSize
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.clickOnce
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import kotlinx.coroutines.launch

private val KakaoYellow = Color(0xFFFEE500)

private val ShareButtonShape = RoundedCornerShape(12.dp)

/**
 * 초대 코드를 공유하는 방법을 고르는 바텀시트.
 *
 * @param onDismiss 시트를 닫아 사라지게 할 때 호출.
 * @param onKakaoShareClick 카카오톡으로 공유를 선택했을 때 호출. 시트가 내려간 뒤에 실행된다.
 * @param onInviteCodeCopyClick 초대 코드 복사를 선택했을 때 호출. 시트가 내려간 뒤에 실행된다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryShareBottomSheet(
    onDismiss: () -> Unit,
    onKakaoShareClick: () -> Unit,
    onInviteCodeCopyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // 복사 토스트는 시트 뒤 화면에 뜨므로, 내려가는 애니메이션을 태운 뒤 실제 동작을 실행한다.
    fun hideThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismiss()
                action()
            }
        }
    }

    ChallaBottomSheet(
        title = stringResource(R.string.gallery_share_title),
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
        icon = {
            Icon(
                painter = painterResource(ChallaIcons.Close),
                contentDescription = stringResource(R.string.gallery_share_close_description),
                modifier =
                    Modifier
                        .size(ChallaIconSize.V24.dp)
                        .noRippleClickOnce(role = Role.Button) { hideThen {} },
                tint = ChallaTheme.colors.labelNormal,
            )
        },
    ) {
        GalleryShareSheetBody(
            onKakaoShareClick = { hideThen(onKakaoShareClick) },
            onInviteCodeCopyClick = { hideThen(onInviteCodeCopyClick) },
        )
    }
}

@Composable
private fun GalleryShareSheetBody(
    onKakaoShareClick: () -> Unit,
    onInviteCodeCopyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 1.dp,
            color = ChallaTheme.colors.lineNeutral,
        )

        Column(
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ShareButton(
                text = stringResource(R.string.gallery_share_kakao),
                containerColor = KakaoYellow,
                contentColor = ChallaTheme.colors.staticBlack,
                onClick = onKakaoShareClick,
            ) {
                Icon(
                    painter = painterResource(ChallaIcons.Kakao),
                    contentDescription = null,
                    modifier = Modifier.size(ChallaIconSize.V24.dp),
                    tint = ChallaTheme.colors.staticBlack,
                )
            }

            ShareButton(
                text = stringResource(R.string.gallery_invite_code_copy_label),
                containerColor = ChallaTheme.colors.backgroundLevel3,
                contentColor = ChallaTheme.colors.labelNormal,
                onClick = onInviteCodeCopyClick,
            )
        }
    }
}

/**
 * 공유 수단 하나를 고르는 버튼. 시트 안에서만 쓰는 형태라 디자인 시스템 버튼 대신 직접 그린다.
 *
 * @param leadingIcon 텍스트 왼쪽에 붙는 아이콘. 없으면 텍스트만 가운데 정렬된다.
 */
@Composable
private fun ShareButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (RowScope.() -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(ShareButtonShape)
                .background(containerColor)
                .clickOnce(role = Role.Button, onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon?.invoke(this)

        Text(
            text = text,
            color = contentColor,
            style = ChallaTheme.typography.bodyLarge.bold,
        )
    }
}

@Preview(showBackground = true, name = "ShareSheet - 본문")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun GalleryShareSheetBodyPreview() {
    ChallaTheme {
        Box(
            modifier =
                Modifier
                    .background(ChallaTheme.colors.backgroundLevel1)
                    .padding(16.dp),
        ) {
            GalleryShareSheetBody(
                onKakaoShareClick = {},
                onInviteCodeCopyClick = {},
            )
        }
    }
}
