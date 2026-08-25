package com.happyhouse.challa.presentation.roomsetting.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.foundation.icon.ChallaIconSize
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce

/** 캐럿을 감싸는 터치 영역. 실제 클릭은 행 전체가 받으므로 크기만 맞춘다. */
private val CaretBoxSize = 32.dp

/** 선행 라벨과 후행 텍스트가 모두 길 때 둘이 붙지 않도록 두는 최소 간격. */
private val TrailingMinGap = 10.dp

/**
 * 방 설정 카드 안의 리스트 한 줄.
 *
 * 디자인시스템의 ChallaListItem과 글자 두께·색이 달라 이 화면 스펙대로 따로 그린다.
 */
@Composable
fun RoomSettingListItem(
    text: String,
    @DrawableRes leadingIcon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .noRippleClickOnce(
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 아이콘과 라벨은 가중치 없이 먼저 너비를 확보한다.
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(leadingIcon),
                contentDescription = null,
                modifier = Modifier.size(ChallaIconSize.V18.dp),
                tint = ChallaTheme.colors.labelAlternative,
            )

            Text(
                text = text,
                color = ChallaTheme.colors.labelSubtle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = ChallaTheme.typography.bodyMedium.medium,
            )
        }

        // 남은 너비만 차지하므로 후행 텍스트가 길면 여기서 줄임표가 생긴다.
        Row(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = TrailingMinGap),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            trailingText?.let { trailing ->
                Text(
                    text = trailing,
                    modifier = Modifier.weight(1f, fill = false),
                    color = ChallaTheme.colors.labelNormal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = ChallaTheme.typography.bodyMedium.medium,
                )
            }

            Box(
                modifier = Modifier.size(CaretBoxSize),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(ChallaIcons.Right),
                    contentDescription = null,
                    modifier = Modifier.size(ChallaIconSize.V16.dp),
                    tint = ChallaTheme.colors.labelAlternative,
                )
            }
        }
    }
}

@Preview(widthDp = 318)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun RoomSettingListItemPreview() {
    RoomSettingCard {
        RoomSettingListItem(
            text = "방 이름",
            leadingIcon = ChallaIcons.Edit,
            trailingText = "친구들과 강릉 여행",
            onClick = {},
        )
        RoomSettingListItem(
            text = "방 이름",
            leadingIcon = ChallaIcons.Edit,
            trailingText = "친구들과 강릉 여행 그리고 속초까지 다녀오는 2박 3일",
            onClick = {},
        )
        RoomSettingListItem(
            text = "커버 이미지",
            leadingIcon = ChallaIcons.Image,
            onClick = {},
        )
    }
}
