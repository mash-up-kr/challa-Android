package com.happyhouse.challa.presentation.login.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private val DotSize = 10.dp
private val DotSpacing = 8.dp

@Composable
fun LoginOnboardingIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DotSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { page ->
            Box(
                modifier =
                    Modifier
                        .size(DotSize)
                        .background(
                            color =
                                if (page == currentPage) {
                                    ChallaTheme.colors.primaryYellow
                                } else {
                                    ChallaTheme.colors.labelDisable
                                },
                            shape = CircleShape,
                        ),
            )
        }
    }
}

@ComposePreview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun LoginOnboardingIndicatorPreview() {
    LoginOnboardingIndicator(
        pageCount = 5,
        currentPage = 1,
    )
}
