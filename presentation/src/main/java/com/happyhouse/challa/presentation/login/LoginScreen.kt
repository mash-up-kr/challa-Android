package com.happyhouse.challa.presentation.login

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.layout.ChallaScaffold
import com.happyhouse.challa.presentation.designsystem.preview.ChallaScreenPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.clickOnce
import com.happyhouse.challa.presentation.login.component.LoginOnboardingIndicator
import com.happyhouse.challa.presentation.login.component.LoginOnboardingPager
import com.happyhouse.challa.presentation.login.model.LoginOnboardingPage

// 카카오 브랜드 컬러. 카카오 로그인 버튼 외에는 쓰지 않으므로 이 화면에만 둔다.
private val KakaoYellow = Color(0xFFFEE500)

private val IndicatorTopPadding = 26.dp

@Composable
fun LoginRoute(
    snackbarHostState: SnackbarHostState,
    onLoginSuccess: (isNewUser: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findActivity()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is LoginSideEffect.LoginSuccess -> onLoginSuccess(effect.isNewUser)
                LoginSideEffect.LoginFailed -> {
                    // TODO JH: 디자인 확정되면 수정
                    Toast.makeText(context, "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LoginScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        // 카카오 SDK 호출은 Activity 가 필요하므로 여기서 클로저로 감싸 ViewModel 에 넘긴다.
        onLoginClick = { viewModel.onIntent(LoginIntent.LoginClick { KakaoLoginClient.login(activity) }) },
        modifier = modifier,
    )
}

private tailrec fun Context.findActivity(): Activity =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> error("Activity 를 찾을 수 없습니다: $this")
    }

@Composable
private fun LoginScreen(
    state: LoginState,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
) {
    val pagerState = rememberPagerState { LoginOnboardingPage.entries.size }

    ChallaScaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ChallaTheme.colors.backgroundSurface,
        snackbarHostState = snackbarHostState,
        bottomBar = {
            KakaoLoginButton(
                isLoading = state.isLoading,
                onClick = onLoginClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp),
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LoginOnboardingPager(
                pagerState = pagerState,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(IndicatorTopPadding))
            LoginOnboardingIndicator(
                pageCount = pagerState.pageCount,
                currentPage = pagerState.currentPage,
            )
        }
    }
}

@Composable
private fun KakaoLoginButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(KakaoYellow)
                .clickOnce(enabled = !isLoading, onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = ChallaTheme.colors.staticBlack,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                painter = painterResource(id = ChallaIcons.Kakao),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = ChallaTheme.colors.staticBlack,
            )
            Text(
                text = stringResource(id = R.string.login_kakao),
                color = ChallaTheme.colors.staticBlack,
                style = ChallaTheme.typography.bodyLarge.bold,
            )
        }
    }
}

@Preview(showBackground = true, name = "Login")
@PreviewWrapper(wrapper = ChallaScreenPreviewWrapper::class)
@Composable
private fun LoginScreenPreview() {
    LoginScreen(
        state = LoginState(isLoading = false),
        onLoginClick = {},
    )
}
