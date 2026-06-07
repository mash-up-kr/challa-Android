package com.happyhouse.challa.presentation.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.model.OnboardingStep
import kotlinx.coroutines.launch

/**
 * TODO JH 하드코딩한 색상은 디자인이 완성되면 제거 예정
 */
private val TextPrimary = Color(0xFF111111)
private val TextSecondary = Color(0xFF666666)
private val IndicatorInactive = Color(0xFFCCCCCC)
private val PlaceholderBg = Color(0xFFEEEEEE)
private val OutlineColor = Color(0xFF999999)
private val SecondaryBtnBg = Color(0xFFF4F4F4)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalSteps = OnboardingStep.entries.size
    val pagerState = rememberPagerState(pageCount = { totalSteps })
    val scope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val isLastPage = currentPage == totalSteps - 1

    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        ) { page ->
            OnboardingPage(step = OnboardingStep.entries[page])
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(totalSteps) { index ->
                    Box(
                        modifier =
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (index == currentPage) TextPrimary else IndicatorInactive),
                    )
                }
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (currentPage > 0) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(currentPage - 1)
                        }
                    },
                    shape = RoundedCornerShape(6.dp),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = SecondaryBtnBg,
                            contentColor = TextPrimary,
                        ),
                    border = BorderStroke(1.dp, OutlineColor),
                ) {
                    Text(text = stringResource(id = R.string.previous), fontSize = 15.sp)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (isLastPage) {
                        onComplete()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(currentPage + 1)
                        }
                    }
                },
                shape = RoundedCornerShape(6.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = TextPrimary,
                        contentColor = Color.White,
                    ),
            ) {
                Text(
                    text =
                        stringResource(
                            id = if (isLastPage) R.string.start else R.string.next,
                        ),
                    fontSize = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun OnboardingPage(
    step: OnboardingStep,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(PlaceholderBg),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = step.titleResId),
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = step.descriptionResId),
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreen(onComplete = {})
}
