package com.happyhouse.challa.presentation.home

import androidx.annotation.DrawableRes
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigation
import com.happyhouse.challa.presentation.designsystem.component.ChallaTopNavigationVariant
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.designsystem.util.noRippleClickOnce
import com.happyhouse.challa.presentation.home.contract.HomeState
import com.happyhouse.challa.presentation.home.model.PrintState
import com.happyhouse.challa.presentation.home.model.RoomUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private val SHOOTING_CARD_WIDTH = 200.dp
private val SHOOTING_CARD_HEIGHT = 266.dp
private val FILM_CARD_WIDTH = 100.dp
private val FILM_CARD_HEIGHT = 127.dp

/** 필름 카드가 겹쳐 쌓일 때 카드 하나가 오른쪽으로 밀려나는 간격 */
private val FILM_CARD_STEP = 83.dp

/** 필름 카드가 쌓일 때 카드 순서(0-based)별 회전 각도(도, 시계방향 +). 반시계 5° → 시계 5° → 반시계 5° → 0° 반복 */
private val FILM_CARD_ROTATIONS = listOf(-5.05f, 5.85f, -5.99f, 0f)

private fun filmCardRotation(index: Int): Float = FILM_CARD_ROTATIONS[index % FILM_CARD_ROTATIONS.size]

/** 필름 스택에 미리보기로 노출하는 사진(더보기 카드 제외) 최대 개수 */
private const val FILM_PREVIEW_MAX = 3

@Composable
fun HomeRoute(
    onNavigateToCreateRoom: () -> Unit,
    onNavigateToInviteCode: () -> Unit,
    onNavigateToSetting: () -> Unit,
    onNavigateToRoom: (roomId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        onCreateRoomClick = onNavigateToCreateRoom,
        onInviteCodeClick = onNavigateToInviteCode,
        onSettingClick = onNavigateToSetting,
        onRoomClick = onNavigateToRoom,
        modifier = modifier,
    )
}

@Composable
private fun HomeScreen(
    state: HomeState,
    onCreateRoomClick: () -> Unit,
    onInviteCodeClick: () -> Unit,
    onSettingClick: () -> Unit,
    onRoomClick: (roomId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(ChallaTheme.colors.backgroundSurface)
                .homeGlow(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
        ) {
            HomeTopBar(
                onCreateRoomClick = onCreateRoomClick,
                onEnterRoomClick = onInviteCodeClick,
                onSettingClick = onSettingClick,
            )

            when {
                state.isLoading ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = ChallaTheme.colors.labelNormal,
                            strokeWidth = 2.dp,
                        )
                    }

                state.isEmpty -> {
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        HomeEmptyMessage(
                            nickname = state.nickname,
                            profileImageUrl = state.profileImageUrl,
                        )
                    }
                    HomeActionButtons(
                        onCreateRoomClick = onCreateRoomClick,
                        onInviteCodeClick = onInviteCodeClick,
                    )
                }

                else ->
                    HomeRoomsContent(
                        shootingRooms = state.shootingRooms,
                        completedRooms = state.completedRooms,
                        onRoomClick = onRoomClick,
                        modifier = Modifier.weight(1f),
                    )
            }
        }
    }
}

/**
 * 촬영중/촬영완료한 방이 있을 때(케이스 2)의 홈 본문.
 *
 * 촬영 중 방은 가로 스크롤 카드로, 촬영 완료 방은 세로 목록으로 노출한다.
 */
@Composable
private fun HomeRoomsContent(
    shootingRooms: ImmutableList<RoomUiModel.Shooting>,
    completedRooms: ImmutableList<RoomUiModel.Completed>,
    onRoomClick: (roomId: String) -> Unit,
    onSettingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        if (shootingRooms.isNotEmpty()) {
            HomeShootingSection(
                rooms = shootingRooms,
                onRoomClick = onRoomClick,
            )
        }

        if (shootingRooms.isNotEmpty() && completedRooms.isNotEmpty()) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = ChallaTheme.colors.lineNormal,
            )
        }

        if (completedRooms.isNotEmpty()) {
            HomeCompletedSection(
                rooms = completedRooms,
                onRoomClick = onRoomClick,
            )
        }
    }
}

@Composable
private fun HomeShootingSection(
    rooms: ImmutableList<RoomUiModel.Shooting>,
    onRoomClick: (roomId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(id = R.string.home_section_shooting),
            modifier = Modifier.padding(horizontal = 16.dp),
            color = ChallaTheme.colors.labelNeutral,
            style = ChallaTheme.typography.bodySmall.bold,
        )
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            rooms.forEach { room ->
                HomeShootingCard(
                    room = room,
                    onClick = { onRoomClick(room.id) },
                )
            }
        }
    }
}

@Composable
private fun HomeShootingCard(
    room: RoomUiModel.Shooting,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(width = SHOOTING_CARD_WIDTH, height = SHOOTING_CARD_HEIGHT)
                .clip(RoundedCornerShape(12.dp))
                .background(ChallaTheme.colors.backgroundLevel2)
                .noRippleClickOnce(role = Role.Button, onClick = onClick),
    ) {
        RoomAsyncImage(
            imageUrl = room.coverImageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
        // 어둡게 깔아 텍스트 가독성 확보
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Black.copy(alpha = 0.1f)),
                        ),
                    ),
        )
        // 상단 옐로우 글로우
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops =
                                arrayOf(
                                    0f to ChallaTheme.colors.primaryYellow.copy(alpha = 0.2f),
                                    0.76f to Color.Transparent,
                                ),
                        ),
                    ),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = room.name,
                    color = ChallaTheme.colors.labelNormal,
                    style = ChallaTheme.typography.bodyMedium.bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                HomeParticipantCount(
                    count = room.participantCount,
                    iconSize = 14.dp,
                    textStyle = ChallaTheme.typography.descriptionLarge.bold,
                )
            }

            Row(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ChallaTheme.colors.primaryYellow)
                        .padding(horizontal = 11.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = ChallaIcons.Camera),
                    contentDescription = stringResource(id = R.string.home_taken_count_description),
                    modifier = Modifier.size(22.dp),
                    tint = ChallaTheme.colors.staticBlack,
                )
                Text(
                    text = room.takenCount.toString(),
                    color = ChallaTheme.colors.staticBlack,
                    style = ChallaTheme.typography.bodyMedium.bold,
                )
            }
        }
    }
}

@Composable
private fun HomeCompletedSection(
    rooms: ImmutableList<RoomUiModel.Completed>,
    onRoomClick: (roomId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(id = R.string.home_section_completed),
            color = ChallaTheme.colors.labelNeutral,
            style = ChallaTheme.typography.bodySmall.bold,
        )
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            rooms.forEach { room ->
                HomeCompletedRoom(
                    room = room,
                    onClick = { onRoomClick(room.id) },
                )
            }
        }
    }
}

@Composable
private fun HomeCompletedRoom(
    room: RoomUiModel.Completed,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .noRippleClickOnce(role = Role.Button, onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HomePrintStateChip(printState = room.printState)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = room.name,
                    modifier = Modifier.weight(1f, fill = false),
                    color = ChallaTheme.colors.labelNormal,
                    style = ChallaTheme.typography.bodyLarge.bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                HomeParticipantCount(
                    count = room.participantCount,
                    iconSize = 18.dp,
                    textStyle = ChallaTheme.typography.bodySmall.bold,
                )
            }
        }
        HomeFilmStack(
            imageUrls = room.photoImageUrls,
            totalPhotoCount = room.totalPhotoCount,
        )
    }
}

@Composable
private fun HomePrintStateChip(
    printState: PrintState,
    modifier: Modifier = Modifier,
) {
    val label: String
    val containerColor: Color
    val borderColor: Color
    val textColor: Color
    when (printState) {
        PrintState.WAITING -> {
            label = stringResource(id = R.string.home_print_waiting)
            containerColor = ChallaTheme.colors.backgroundLevel1
            borderColor = ChallaTheme.colors.lineNormal
            textColor = ChallaTheme.colors.labelAlternative
        }

        PrintState.COMPLETED -> {
            label = stringResource(id = R.string.home_print_completed)
            containerColor = ChallaTheme.colors.primaryYellow.copy(alpha = 0.08f)
            borderColor = ChallaTheme.colors.primaryYellow.copy(alpha = 0.2f)
            textColor = ChallaTheme.colors.primaryYellow
        }
    }

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(100.dp))
                .background(containerColor)
                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(100.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp),
    ) {
        Text(
            text = label,
            color = textColor,
            style = ChallaTheme.typography.descriptionLarge.medium,
        )
    }
}

/**
 * 촬영 완료한 방의 필름 미리보기.
 *
 * 사진을 살짝 겹쳐 쌓아 보여주고, 남은 장수가 있으면 마지막에 "더보기" 카드로 개수를 표기한다.
 */
@Composable
private fun HomeFilmStack(
    imageUrls: ImmutableList<String>,
    totalPhotoCount: Int,
    modifier: Modifier = Modifier,
) {
    val previews = imageUrls.take(FILM_PREVIEW_MAX)
    val remaining = totalPhotoCount - previews.size
    val cardCount = previews.size + if (remaining > 0) 1 else 0

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .height(FILM_CARD_HEIGHT),
    ) {
        // 카드가 가용 폭을 넘어 잘리지 않도록 겹침 간격을 좁힌다. (좁은 화면 대응)
        val step =
            if (cardCount > 1) {
                minOf(FILM_CARD_STEP, (maxWidth - FILM_CARD_WIDTH) / (cardCount - 1))
            } else {
                FILM_CARD_STEP
            }

        previews.forEachIndexed { index, url ->
            HomeFilmCard(
                imageUrl = url,
                modifier =
                    Modifier
                        .offset(x = step * index)
                        .rotate(filmCardRotation(index)),
            )
        }
        if (remaining > 0) {
            HomeFilmCard(
                imageUrl = imageUrls.getOrNull(previews.size),
                overflowCount = remaining,
                modifier =
                    Modifier
                        .offset(x = step * previews.size)
                        .rotate(filmCardRotation(previews.size)),
            )
        }
    }
}

@Composable
private fun HomeFilmCard(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    overflowCount: Int? = null,
) {
    Box(
        modifier =
            modifier
                .size(width = FILM_CARD_WIDTH, height = FILM_CARD_HEIGHT)
                .clip(RoundedCornerShape(8.dp))
                .background(ChallaTheme.colors.backgroundLevel3)
                .border(width = 1.dp, color = ChallaTheme.colors.lineNeutral, shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        RoomAsyncImage(
            imageUrl = imageUrl,
            contentDescription = if (overflowCount == null) stringResource(id = R.string.home_room_photo_description) else null,
            modifier = Modifier.fillMaxSize(),
        )
        if (overflowCount != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
            )
            Text(
                text = stringResource(id = R.string.home_film_overflow, overflowCount),
                color = ChallaTheme.colors.staticWhite,
                style = ChallaTheme.typography.bodyMedium.medium,
            )
        }
    }
}

@Composable
private fun HomeParticipantCount(
    count: Int,
    iconSize: Dp,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = ChallaIcons.Person),
            contentDescription = stringResource(id = R.string.home_participant_description),
            modifier = Modifier.size(iconSize),
            tint = ChallaTheme.colors.labelSubtle,
        )
        Text(
            text = count.toString(),
            color = ChallaTheme.colors.labelSubtle,
            style = textStyle,
        )
    }
}

@Composable
private fun RoomAsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model =
            ImageRequest
                .Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        placeholder = ColorPainter(ChallaTheme.colors.backgroundLevel3),
        error = ColorPainter(ChallaTheme.colors.backgroundLevel3),
        modifier = modifier,
    )
}

/**
 * 화면 하단에 은은하게 깔리는 옐로우 글로우.
 *
 * 피그마의 blur(150) 처리된 ellipse를 대체한다.
 * [androidx.compose.ui.draw.blur]는 API 31 미만에서 동작하지 않으므로 radial gradient로 표현한다.
 */
@Composable
private fun Modifier.homeGlow(): Modifier {
    val glowColor = ChallaTheme.colors.primaryYellow
    return drawBehind {
        val center = Offset(x = size.width / 2f, y = size.height * 0.92f)
        val radius = size.width * 0.95f
        drawRect(
            brush =
                Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.20f), Color.Transparent),
                    center = center,
                    radius = radius,
                ),
        )
    }
}

@Composable
private fun HomeTopBar(
    onCreateRoomClick: () -> Unit,
    onEnterRoomClick: () -> Unit,
    onSettingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChallaTopNavigation(
        title = stringResource(id = R.string.home_title),
        modifier = modifier,
        variant = ChallaTopNavigationVariant.MAIN,
        trailingIcon = {
            HomeAddMenuAction(
                onCreateRoomClick = onCreateRoomClick,
                onEnterRoomClick = onEnterRoomClick,
            )
            HomeTopBarAction(
                icon = ChallaIcons.Setting,
                contentDescription = stringResource(id = R.string.home_setting_description),
                onClick = onSettingClick,
            )
        },
    )
}

/**
 * 상단바의 `+` 아이콘. 탭하면 방 만들기/방 입장하기 드롭다운 메뉴를 아이콘 아래로 띄운다.
 *
 * 메뉴 열림 상태는 화면 이동과 무관한 순수 UI 상태라 여기서 로컬로 관리한다.
 */
@Composable
private fun HomeAddMenuAction(
    onCreateRoomClick: () -> Unit,
    onEnterRoomClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        HomeTopBarAction(
            icon = ChallaIcons.Add,
            contentDescription = stringResource(id = R.string.home_add_description),
            onClick = { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(180.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = ChallaTheme.colors.staticBlack,
        ) {
            HomeAddMenuItem(
                text = stringResource(id = R.string.home_menu_create_room),
                onClick = {
                    expanded = false
                    onCreateRoomClick()
                },
            )
            HorizontalDivider(color = ChallaTheme.colors.lineNormal)
            HomeAddMenuItem(
                text = stringResource(id = R.string.home_menu_enter_room),
                onClick = {
                    expanded = false
                    onEnterRoomClick()
                },
            )
        }
    }
}

@Composable
private fun HomeAddMenuItem(
    text: String,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                color = ChallaTheme.colors.labelSubtle,
                style = ChallaTheme.typography.bodyXSmall.medium,
            )
        },
        onClick = onClick,
        contentPadding = PaddingValues(16.dp),
    )
}

@Composable
private fun HomeTopBarAction(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(40.dp)
                .noRippleClickOnce(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = ChallaTheme.colors.labelNeutral,
        )
    }
}

@Composable
private fun HomeEmptyMessage(
    nickname: String,
    profileImageUrl: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = nickname,
            color = ChallaTheme.colors.primaryYellow,
            textAlign = TextAlign.Center,
            style = ChallaTheme.typography.headingSmall.bold,
        )
        Text(
            text = stringResource(id = R.string.home_empty_subtitle),
            color = ChallaTheme.colors.labelNormal,
            textAlign = TextAlign.Center,
            style = ChallaTheme.typography.headingSmall.bold,
        )
        Spacer(modifier = Modifier.height(24.dp))
        HomeProfileImage(profileImageUrl = profileImageUrl)
    }
}

@Composable
private fun HomeProfileImage(
    profileImageUrl: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(ChallaTheme.colors.backgroundLevel3),
        contentAlignment = Alignment.Center,
    ) {
        if (profileImageUrl == null) {
            Icon(
                painter = painterResource(id = ChallaIcons.Profile),
                contentDescription = stringResource(id = R.string.home_profile_description),
                modifier = Modifier.size(80.dp),
                tint = ChallaTheme.colors.labelNeutral,
            )
        } else {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(profileImageUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = stringResource(id = R.string.home_profile_description),
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(ChallaTheme.colors.backgroundLevel3),
                error = ColorPainter(ChallaTheme.colors.backgroundLevel3),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun HomeActionButtons(
    onCreateRoomClick: () -> Unit,
    onInviteCodeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HomeActionButton(
            text = stringResource(id = R.string.home_create_room),
            containerColor = ChallaTheme.colors.primaryYellow,
            onClick = onCreateRoomClick,
        )
        HomeActionButton(
            text = stringResource(id = R.string.home_enter_invite_code),
            containerColor = ChallaTheme.colors.labelNormal,
            onClick = onInviteCodeClick,
        )
    }
}

@Composable
private fun HomeActionButton(
    text: String,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(containerColor)
                .noRippleClickOnce(role = Role.Button, onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = ChallaTheme.colors.staticBlack,
            style = ChallaTheme.typography.bodyLarge.bold,
        )
    }
}

private fun previewRooms(): ImmutableList<RoomUiModel> =
    persistentListOf(
        RoomUiModel.Shooting(
            id = "1",
            name = "친구들과 강릉 여행",
            participantCount = 1,
            takenCount = 24,
            coverImageUrl = null,
        ),
        RoomUiModel.Shooting(
            id = "2",
            name = "제주도 우정여행",
            participantCount = 4,
            takenCount = 12,
            coverImageUrl = null,
        ),
        RoomUiModel.Completed(
            id = "3",
            name = "친구들과 강릉 여행",
            participantCount = 11,
            printState = PrintState.WAITING,
            photoImageUrls = persistentListOf("", "", "", ""),
            totalPhotoCount = 24,
        ),
        RoomUiModel.Completed(
            id = "4",
            name = "인화 완료 된 방이에요",
            participantCount = 7,
            printState = PrintState.COMPLETED,
            photoImageUrls = persistentListOf("", "", ""),
            totalPhotoCount = 3,
        ),
    )

@Preview(showBackground = true, name = "Home - Rooms")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun HomeRoomsPreview() {
    ChallaTheme {
        HomeScreen(
            state =
                HomeState(
                    isLoading = false,
                    nickname = "나는야멋쟁이토마토",
                    profileImageUrl = null,
                    rooms = previewRooms(),
                ),
            onCreateRoomClick = {},
            onInviteCodeClick = {},
            onSettingClick = {},
            onRoomClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Home - Empty")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun HomeEmptyPreview() {
    ChallaTheme {
        HomeScreen(
            state =
                HomeState(
                    isLoading = false,
                    nickname = "나는야멋쟁이토마토",
                    profileImageUrl = null,
                ),
            onCreateRoomClick = {},
            onInviteCodeClick = {},
            onSettingClick = {},
            onRoomClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Home - Loading")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun HomeLoadingPreview() {
    ChallaTheme {
        HomeScreen(
            state = HomeState(isLoading = true),
            onCreateRoomClick = {},
            onInviteCodeClick = {},
            onSettingClick = {},
            onRoomClick = {},
        )
    }
}
