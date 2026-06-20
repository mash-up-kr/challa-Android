package com.happyhouse.challa.presentation.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper

/**
 * TODO JH 하드코딩한 색상은 디자인이 완성되면 제거 예정
 */
private val TextPrimary = Color(0xFF111111)
private val TextSecondary = Color(0xFF888888)
private val PlaceholderBg = Color(0xFFEEEEEE)
private val KakaoYellow = Color(0xFFFEE500)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
private fun LoginContent(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = R.string.app_name_kr),
                color = TextPrimary,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(id = R.string.app_name_en),
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier =
                    Modifier
                        .size(width = 160.dp, height = 120.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(PlaceholderBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(id = R.string.login_logo_placeholder),
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = { onIntent(LoginIntent.ClickLogin) },
                enabled = !state.isLoading,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                shape = RoundedCornerShape(6.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = KakaoYellow,
                        contentColor = Color.Black,
                    ),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.login_kakao),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Text(
                text = termsAnnotatedString(),
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun termsAnnotatedString(): AnnotatedString =
    buildAnnotatedString {
        append(stringResource(id = R.string.login_terms_prefix))
        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
            append(stringResource(id = R.string.login_terms_of_service))
        }
        append(stringResource(id = R.string.login_terms_middle))
        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
            append(stringResource(id = R.string.login_privacy_policy))
        }
        append(stringResource(id = R.string.login_terms_suffix))
    }

@Preview(showBackground = true, name = "Login")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun LoginScreenPreview() {
    LoginContent(
        state = LoginState(isLoading = false),
        onIntent = {},
    )
}
