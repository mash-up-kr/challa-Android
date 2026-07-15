package com.happyhouse.challa.presentation.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.button.ChallaTextButton
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme

/**
 * 좌우·하단에서 12dp 떨어져 떠 있는 형태의 바텀시트.
 *
 * @param title 상단에 노출되는 타이틀 텍스트.
 * @param onDismissRequest 시트 바깥 영역 터치 등으로 시트를 닫으려 할 때 호출.
 * @param icon 타이틀 줄 우측 끝에 위치하는 40x40 아이콘 슬롯.
 * @param content 외부에서 주입하는 시트 본문.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallaBottomSheet(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    icon: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null,
        contentWindowInsets = { WindowInsets.navigationBars },
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
            shape = RoundedCornerShape(32.dp),
            color = ChallaTheme.colors.backgroundLevel1,
        ) {
            Column(
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f),
                        color = ChallaTheme.colors.labelNormal,
                        style = ChallaTheme.typography.bodyLarge.bold,
                    )
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        icon()
                    }
                }
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun ChallaBottomSheetPreview() {
    ChallaTheme {
        ChallaBottomSheet(
            title = "방 만들기",
            onDismissRequest = {},
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    tint = ChallaTheme.colors.labelNormal,
                    contentDescription = null,
                )
            },
            content = {
                Column(
                    modifier = Modifier.padding(top = 12.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "함께 챌린지를 진행할 방을 만들어 보세요.",
                        color = ChallaTheme.colors.labelNormal,
                        style = ChallaTheme.typography.bodyMedium.bold,
                    )
                    Text(
                        text = "방을 만들면 초대 코드가 발급되며, 코드를 공유해 친구들을 초대할 수 있어요.",
                        color = ChallaTheme.colors.labelSubtle,
                        style = ChallaTheme.typography.bodyMedium.bold,
                    )
                    ChallaTextButton(
                        text = "방 만들기",
                        onClick = { },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    )
                }
            },
        )
    }
}
