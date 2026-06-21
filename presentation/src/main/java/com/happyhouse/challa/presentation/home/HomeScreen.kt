package com.happyhouse.challa.presentation.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.util.clickOnce
import com.happyhouse.challa.presentation.home.model.Room
import com.happyhouse.challa.presentation.home.model.RoomStatus
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * TODO JH 하드코딩한 색상은 디자인이 완성되면 제거 예정
 */
private val TextPrimary = Color(0xFF111111)
private val TextSecondary = Color(0xFF666666)
private val TextMuted = Color(0xFF999999)
private val BorderColor = Color(0xFFDDDDDD)
private val DividerColor = Color(0xFFE5E5E5)
private val ChipBg = Color(0xFFF4F4F4)
private val PlaceholderBg = Color(0xFFEEEEEE)

@Composable
fun HomeScreen(
    onNavigateToInviteCode: () -> Unit,
    onNavigateToCreateRoom: () -> Unit,
    onNavigateToRoom: (Room) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                HomeSideEffect.InviteCodeEntryRequested -> onNavigateToInviteCode()
                HomeSideEffect.RoomCreationRequested -> onNavigateToCreateRoom()
                is HomeSideEffect.RoomSelected -> onNavigateToRoom(effect.room)
            }
        }
    }

    HomeContent(
        state = state,
        onIntent = { intent ->
            // TODO JH 클릭 피드백용 임시 토스트 - 실제 동작 연결되면 제거 예정
            val message =
                when (intent) {
                    HomeIntent.ClickInviteCode -> "초대코드 입력 클릭"
                    HomeIntent.ClickCreateRoom -> "방 만들기 클릭"
                    is HomeIntent.ClickRoom -> "${intent.room.name} 방 클릭"
                }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onIntent(intent)
        },
        modifier = modifier,
    )
}

@Composable
private fun HomeContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
        ) {
            HomeTopBar(
                userName = state.userName,
                onClickInviteCode = { onIntent(HomeIntent.ClickInviteCode) },
            )

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(items = state.rooms, key = { it.id }) { room ->
                        RoomCard(
                            room = room,
                            onClick = { onIntent(HomeIntent.ClickRoom(room)) },
                        )
                    }
                }
            }
        }

        CreateRoomFab(
            onClick = { onIntent(HomeIntent.ClickCreateRoom) },
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
        )
    }
}

@Composable
private fun HomeTopBar(
    userName: String,
    onClickInviteCode: () -> Unit,
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
                text = stringResource(id = R.string.home_greeting, userName),
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ChipBg)
                        .border(width = 1.dp, color = TextMuted, shape = RoundedCornerShape(4.dp))
                        .clickOnce { onClickInviteCode() }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.home_invite_code),
                    color = TextPrimary,
                    fontSize = 12.sp,
                )
            }
        }
        HorizontalDivider(color = DividerColor)
    }
}

@Composable
private fun RoomCard(
    room: Room,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(6.dp))
                .clickOnce { onClick() }
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(PlaceholderBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(id = R.string.home_cover_placeholder),
                color = TextMuted,
                fontSize = 10.sp,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = room.name,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = room.status.badgeText(),
                color = TextSecondary,
                fontSize = 12.sp,
            )
        }
        Text(
            text = "›",
            color = TextMuted,
            fontSize = 18.sp,
        )
    }
}

@Composable
private fun RoomStatus.badgeText(): String =
    when (this) {
        is RoomStatus.Shooting ->
            stringResource(id = R.string.home_badge_shooting, taken, total)
        is RoomStatus.Waiting ->
            stringResource(id = R.string.home_badge_waiting, dDay, remaining.formatHhMm())
        RoomStatus.Opened ->
            stringResource(id = R.string.home_badge_opened)
        is RoomStatus.Expiring ->
            stringResource(id = R.string.home_badge_expiring, dDay)
    }

private fun Duration.formatHhMm(): String {
    val totalMinutes = inWholeMinutes
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return "%02d:%02d".format(hours, minutes)
}

@Composable
private fun CreateRoomFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(56.dp)
                .shadow(elevation = 4.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(TextPrimary)
                .clickOnce { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        // TODO JH: 텍스트가 아닌 다른 아이콘으로 변경될 임시 코드라 하드코딩으로 두었다.
        Text(
            text = "+",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Preview(showBackground = true, name = "Home")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun HomeScreenPreview() {
    HomeContent(
        state =
            HomeState(
                isLoading = false,
                userName = "윤서연",
                rooms =
                    persistentListOf(
                        Room("1", "오사카 졸업여행", RoomStatus.Shooting(12, 24)),
                        Room("2", "제주 워크샵", RoomStatus.Waiting(0, 2.hours + 47.minutes)),
                        Room("3", "다낭 4박5일", RoomStatus.Opened),
                        Room("4", "부산 1박", RoomStatus.Expiring(2)),
                    ),
            ),
        onIntent = {},
    )
}

@Preview(showBackground = true, name = "Home - Loading")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun HomeScreenLoadingPreview() {
    HomeContent(
        state = HomeState(isLoading = true, userName = ""),
        onIntent = {},
    )
}
