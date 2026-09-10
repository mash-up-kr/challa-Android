package com.happyhouse.challa.presentation.chatting.component

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.happyhouse.challa.presentation.chatting.contract.ChatState.ChatInfo
import com.happyhouse.challa.presentation.chatting.contract.ChatState.ChatInfo.LoadMoreState

/**
 * 비어 있지 않고 오래된 메시지부터 정렬된 채팅 목록의 스크롤과 이전 페이지 조회를 처리한다.
 *
 * 최초 진입 시 마지막 메시지로 이동한다. 이후 새 메시지는 내가 보냈거나 기존 마지막 메시지
 * 근처를 보고 있었을 때만 따라간다. 키보드가 표시된 상태에서 목록 높이가 줄면 직전에 하단
 * 근처였고 스크롤 중이 아닌 경우에만 따라가며, 메시지 수 변경만으로는 이 이동을 실행하지 않는다.
 *
 * @param leadingStatusItemCount 목록 앞의 로딩·오류 항목 수. 채팅 인덱스와 목록 인덱스 변환에 사용한다.
 * @param onLoadMore 초기 스크롤 완료 후 목록 상단에 가까워지면 호출한다.
 * 다음 페이지가 있고 추가 조회 상태가 IDLE인 경우에만 호출한다.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun ChatListSideEffects(
    chatInfo: ChatInfo.Loaded,
    listState: LazyListState,
    leadingStatusItemCount: Int,
    onLoadMore: () -> Unit,
) {
    val chats = chatInfo.chats
    val isImeVisible = WindowInsets.isImeVisible
    var hasCompletedInitialScroll by rememberSaveable { mutableStateOf(false) }
    var previousLatestChatId by remember { mutableLongStateOf(chats.last().chatId) }
    val scrollTargetIndex = chats.lastIndex + leadingStatusItemCount
    val latestScrollTargetIndex by rememberUpdatedState(scrollTargetIndex)
    val currentIsImeVisible by rememberUpdatedState(isImeVisible)
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    val shouldLoadPreviousPage by
        remember(
            listState,
            chats.size,
            chatInfo.hasNext,
            chatInfo.loadMoreState,
        ) {
            derivedStateOf {
                val firstVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index
                hasCompletedInitialScroll &&
                    chatInfo.hasNext &&
                    chatInfo.loadMoreState == LoadMoreState.IDLE &&
                    firstVisibleItemIndex != null &&
                    firstVisibleItemIndex <= LOAD_MORE_TRIGGER_INDEX
            }
        }

    LaunchedEffect(chats.size) {
        if (!hasCompletedInitialScroll) {
            listState.scrollToItem(latestScrollTargetIndex)
            hasCompletedInitialScroll = true
        }
    }

    LaunchedEffect(chats.last().chatId) {
        val latestChat = chats.last()
        val latestChatId = latestChat.chatId
        if (hasCompletedInitialScroll && latestChatId != previousLatestChatId) {
            val previousLatestChatIndex =
                chats.indexOfFirst { chat -> chat.chatId == previousLatestChatId }
            val lastVisibleChatIndex =
                listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.minus(
                    leadingStatusItemCount,
                )
            if (
                shouldAutoScrollToLatestChat(
                    isMine = latestChat.isMine,
                    previousLatestChatIndex = previousLatestChatIndex,
                    lastVisibleChatIndex = lastVisibleChatIndex,
                )
            ) {
                listState.scrollToItem(latestScrollTargetIndex)
            }
        }
        previousLatestChatId = latestChatId
    }

    LaunchedEffect(listState) {
        var previousViewportHeight = 0
        var wasNearBottom = false

        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            layoutInfo.viewportSize.height to
                isNearLatestChat(
                    latestChatIndex = latestScrollTargetIndex,
                    lastVisibleChatIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index,
                )
        }.collect { (viewportHeight, isNearBottom) ->
            val shouldFollowResize =
                currentIsImeVisible &&
                    viewportHeight < previousViewportHeight &&
                    wasNearBottom &&
                    !listState.isScrollInProgress

            previousViewportHeight = viewportHeight
            wasNearBottom = isNearBottom || shouldFollowResize

            if (shouldFollowResize) {
                listState.scrollToItem(latestScrollTargetIndex)
            }
        }
    }

    LaunchedEffect(shouldLoadPreviousPage, chats.size) {
        if (shouldLoadPreviousPage) currentOnLoadMore()
    }
}

private fun shouldAutoScrollToLatestChat(
    isMine: Boolean,
    previousLatestChatIndex: Int,
    lastVisibleChatIndex: Int?,
): Boolean =
    isMine ||
        isNearLatestChat(
            latestChatIndex = previousLatestChatIndex,
            lastVisibleChatIndex = lastVisibleChatIndex,
        )

/** 두 인덱스는 상태 항목 포함 여부가 같은 기준이어야 한다. 표시 항목이 없으면 false를 반환한다. */
private fun isNearLatestChat(
    latestChatIndex: Int,
    lastVisibleChatIndex: Int?,
): Boolean =
    latestChatIndex >= 0 &&
        lastVisibleChatIndex != null &&
        lastVisibleChatIndex >= latestChatIndex - AUTO_SCROLL_ITEM_THRESHOLD

private const val LOAD_MORE_TRIGGER_INDEX = 3
private const val AUTO_SCROLL_ITEM_THRESHOLD = 1
