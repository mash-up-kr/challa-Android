package com.happyhouse.challa.presentation.home.createroom

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.util.clickOnce
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce

/**
 * TODO JH 하드코딩한 색상은 디자인이 완성되면 제거 예정
 */
private val TextPrimary = Color(0xFF111111)
private val TextBody = Color(0xFF555555)
private val TextSecondary = Color(0xFF666666)
private val TextMuted = Color(0xFF999999)
private val BorderColor = Color(0xFFDDDDDD)
private val DividerColor = Color(0xFFE5E5E5)
private val FooterDivider = Color(0xFFEEEEEE)
private val InputBorder = Color(0xFF999999)
private val ButtonEnabled = Color(0xFF111111)
private val ButtonDisabled = Color(0xFFBBBBBB)

@Composable
fun CreateRoomScreen(
    onClose: () -> Unit,
    onRoomCreated: (roomId: String, roomName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateRoomViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                CreateRoomSideEffect.RoomCreationCancelled -> onClose()
                is CreateRoomSideEffect.RoomCreated -> {
                    // TODO JH 방 생성 완료 피드백용 임시 토스트 - ShareInvite 화면 연결 시 제거 예정
                    Toast
                        .makeText(context, "${effect.roomName} 방 생성 완료", Toast.LENGTH_SHORT)
                        .show()
                    onRoomCreated(effect.roomId, effect.roomName)
                }
            }
        }
    }

    CreateRoomContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
private fun CreateRoomContent(
    state: CreateRoomState,
    onIntent: (CreateRoomIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .imePadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CreateRoomTopBar(
                onClickClose = { onIntent(CreateRoomIntent.ClickClose) },
            )

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                NameField(
                    name = state.name,
                    onNameChange = { onIntent(CreateRoomIntent.NameChanged(it)) },
                )
                InfoBox()
            }

            CreateRoomFooter(
                canSubmit = state.canSubmit,
                onClickSubmit = { onIntent(CreateRoomIntent.ClickCreate) },
            )
        }

        if (state.isSubmitting) {
            LoadingOverlay()
        }
    }
}

@Composable
private fun LoadingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures { }
            },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CreateRoomTopBar(
    onClickClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(id = R.string.create_room_close),
                color = TextPrimary,
                fontSize = 16.sp,
                modifier = Modifier.noRippleClickOnce { onClickClose() },
            )
            Text(
                text = stringResource(id = R.string.create_room_title),
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.width(40.dp))
        }
        HorizontalDivider(thickness = 1.dp, color = DividerColor)
    }
}

@Composable
private fun NameField(
    name: String,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.create_room_name_label, ROOM_NAME_MAX_LENGTH),
            color = TextSecondary,
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(6.dp))
        val inputTextStyle =
            TextStyle(
                color = TextPrimary,
                fontSize = 15.sp,
            )
        BasicTextField(
            value = name,
            onValueChange = onNameChange,
            singleLine = true,
            textStyle = inputTextStyle,
            cursorBrush = SolidColor(TextPrimary),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .border(width = 1.dp, color = InputBorder, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .focusRequester(focusRequester),
            decorationBox = { innerTextField ->
                Box {
                    if (name.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.create_room_name_placeholder),
                            style = inputTextStyle.copy(color = TextMuted),
                        )
                    }
                    innerTextField()
                }
            },
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text =
                stringResource(
                    id = R.string.create_room_name_counter,
                    name.length,
                    ROOM_NAME_MAX_LENGTH,
                ),
            color = TextMuted,
            fontSize = 11.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun InfoBox(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(4.dp))
                .padding(12.dp),
    ) {
        Text(
            text = stringResource(id = R.string.create_room_info),
            color = TextBody,
            fontSize = 13.sp,
            lineHeight = 23.sp,
        )
    }
}

@Composable
private fun CreateRoomFooter(
    canSubmit: Boolean,
    onClickSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = 1.dp, color = FooterDivider)
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
        ) {
            SubmitButton(
                enabled = canSubmit,
                onClick = onClickSubmit,
            )
        }
    }
}

@Composable
private fun SubmitButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (enabled) ButtonEnabled else ButtonDisabled
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(bg)
                .clickOnce(enabled = enabled) { onClick() }
                .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(id = R.string.create_room_submit),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Preview(showBackground = true, name = "CreateRoom - Empty")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CreateRoomScreenEmptyPreview() {
    CreateRoomContent(
        state = CreateRoomState(name = ""),
        onIntent = {},
    )
}

@Preview(showBackground = true, name = "CreateRoom - Filled")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CreateRoomScreenFilledPreview() {
    CreateRoomContent(
        state = CreateRoomState(name = "오사카 졸업여행"),
        onIntent = {},
    )
}

@Preview(showBackground = true, name = "CreateRoom - Submitting")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CreateRoomScreenSubmittingPreview() {
    CreateRoomContent(
        state = CreateRoomState(name = "오사카 졸업여행", isSubmitting = true),
        onIntent = {},
    )
}
