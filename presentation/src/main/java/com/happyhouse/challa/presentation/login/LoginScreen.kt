package com.happyhouse.challa.presentation.login

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.clickOnce

// 카카오 브랜드 컬러. 카카오 로그인 버튼 외에는 쓰지 않으므로 이 화면에만 둔다.
private val KakaoYellow = Color(0xFFFEE500)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findActivity()

    // 로그인 화면은 배경이 어두우므로 상태바 아이콘을 흰색으로. 화면을 벗어나면 원복한다.
    val view = LocalView.current
    DisposableEffect(Unit) {
        val controller = WindowCompat.getInsetsController(activity.window, view)
        val previous = controller.isAppearanceLightStatusBars
        controller.isAppearanceLightStatusBars = false
        onDispose {
            controller.isAppearanceLightStatusBars = previous
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                LoginSideEffect.LoginSuccess -> onLoginSuccess()
                LoginSideEffect.LoginFailed -> {
                    // TODO JH: 디자인 확정되면 수정
                    Toast.makeText(context, "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LoginContent(
        state = state,
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
private fun LoginContent(
    state: LoginState,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(ChallaTheme.colors.backgroundSurface),
    ) {
        BrandingContent(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    // 디자인상 브랜딩 영역은 투명도 10%로 표현된다.
                    .alpha(0.1f),
        )

        KakaoLoginButton(
            isLoading = state.isLoading,
            onClick = onLoginClick,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 8.dp),
        )
    }
}

@Composable
private fun BrandingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.login_brand),
            color = ChallaTheme.colors.staticWhite,
            style = ChallaTheme.typography.headingXLarge.bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Image(
            painter = painterResource(id = R.drawable.img_login_logo),
            contentDescription = null,
            modifier =
                Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
        )
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
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun LoginScreenPreview() {
    ChallaTheme {
        LoginContent(
            state = LoginState(isLoading = false),
            onLoginClick = {},
        )
    }
}
