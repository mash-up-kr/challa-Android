package com.happyhouse.challa.presentation.login.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.login.model.LoginOnboardingPage
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private val TitleTopPadding = 36.dp
private val TitleHorizontalPadding = 16.dp

@Composable
fun LoginOnboardingPager(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier,
    ) { page ->
        LoginOnboardingPageContent(
            page = LoginOnboardingPage.entries[page],
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun LoginOnboardingPageContent(
    page: LoginOnboardingPage,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
        )
        Text(
            text = stringResource(id = page.titleRes),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = TitleTopPadding, start = TitleHorizontalPadding, end = TitleHorizontalPadding),
            color = ChallaTheme.colors.labelNormal,
            style = ChallaTheme.typography.headingLarge.medium,
            textAlign = TextAlign.Center,
        )
    }
}

@ComposePreview(showBackground = true, widthDp = 390)
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun LoginOnboardingPagerPreview() {
    LoginOnboardingPager(
        pagerState = rememberPagerState { LoginOnboardingPage.entries.size },
        modifier = Modifier.fillMaxWidth(),
    )
}
