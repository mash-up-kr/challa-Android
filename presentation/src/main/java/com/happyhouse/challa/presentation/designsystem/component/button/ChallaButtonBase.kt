package com.happyhouse.challa.presentation.designsystem.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce

@Composable
internal fun ChallaButtonBase(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    variant: ChallaButtonVariant = ChallaButtonVariant.PRIMARY,
    minHeight: Dp,
    cornerRadius: Dp,
    contentPadding: PaddingValues,
    content: @Composable (contentColor: Color) -> Unit,
) {
    // 로딩 중에는 활성 상태의 색을 유지하되 클릭만 막는다.
    val colorSpec = variant.colorSpec(enabled)

    Box(
        modifier =
            modifier
                .heightIn(min = minHeight)
                .clip(shape = RoundedCornerShape(cornerRadius))
                .background(colorSpec.containerColor)
                .noRippleClickOnce(
                    enabled = enabled && !loading,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        content(colorSpec.contentColor)
    }
}
