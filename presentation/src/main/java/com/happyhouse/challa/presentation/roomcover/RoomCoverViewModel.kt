package com.happyhouse.challa.presentation.roomcover

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.model.RoomCover
import com.happyhouse.challa.domain.model.RoomCoverColor
import com.happyhouse.challa.domain.model.RoomCoverSticker
import com.happyhouse.challa.domain.repository.ImageUploadRepository
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.causeOrNull
import com.happyhouse.challa.domain.result.onFailure
import com.happyhouse.challa.domain.result.onSuccess
import com.happyhouse.challa.presentation.base.BaseViewModel
import com.happyhouse.challa.presentation.roomcover.contract.RoomCoverColorUiModel
import com.happyhouse.challa.presentation.roomcover.contract.RoomCoverIntent
import com.happyhouse.challa.presentation.roomcover.contract.RoomCoverSideEffect
import com.happyhouse.challa.presentation.roomcover.contract.RoomCoverState
import com.happyhouse.challa.presentation.roomcover.contract.RoomCoverStickerUiModel
import com.happyhouse.challa.presentation.roomcover.contract.toUiModel
import com.happyhouse.challa.presentation.roomcover.contract.toUiModelOrNull
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = RoomCoverViewModel.Factory::class)
class RoomCoverViewModel @AssistedInject constructor(
    @Assisted private val roomId: Long,
    private val roomRepository: RoomRepository,
    private val imageUploadRepository: ImageUploadRepository,
) : BaseViewModel<RoomCoverState, RoomCoverIntent, RoomCoverSideEffect>(
        initialState = RoomCoverState(),
    ) {
    /** 마지막으로 저장에 성공한 커버. 저장이 실패하면 화면을 여기로 되돌린다. */
    private var savedCover: RoomCover = RoomCover()

    private var saveJob: Job? = null
    private var uploadJob: Job? = null

    /** 마지막으로 보내야 할 커버. 저장을 기다리는 사이 선택이 또 바뀌었는지 이 값으로 가린다. */
    private var latestCover: RoomCover? = null

    init {
        loadCover()
    }

    override fun onIntent(intent: RoomCoverIntent) {
        when (intent) {
            is RoomCoverIntent.ColorClick -> handleColorClick(intent.color)
            is RoomCoverIntent.StickerClick -> handleStickerClick(intent.sticker)
            is RoomCoverIntent.BackgroundImageSelect -> handleBackgroundImageSelect(intent.imageUri)
            RoomCoverIntent.BackgroundImageRemoveClick -> handleBackgroundImageRemove()
            RoomCoverIntent.RetryClick -> loadCover()
        }
    }

    private fun loadCover() {
        // 다시 불러오는 동안 앞선 저장이 끝나면 savedCover와 새 화면 상태가 어긋난다.
        saveJob?.cancel()
        uploadJob?.cancel()
        latestCover = null

        updateState { copy(content = RoomCoverState.Content.Loading) }
        viewModelScope.launch {
            val roomDeferred = async { roomRepository.getRoom(roomId) }
            val usersDeferred = async { roomRepository.getRoomUsers(roomId) }
            val optionsDeferred = async { roomRepository.getRoomCoverOptions() }

            val room = roomDeferred.await().valueOrLog("방 상세")
            val users = usersDeferred.await().valueOrLog("방 참여자")
            val options = optionsDeferred.await().valueOrLog("커버 옵션")
            if (room == null || users == null || options == null) {
                updateState { copy(content = RoomCoverState.Content.Error) }
                return@launch
            }

            savedCover = room.cover

            val colors = options.colors.mapNotNull { it.toUiModelOrNull() }
            val stickers = options.stickers.map { it.toUiModel() }
            updateState {
                copy(
                    roomName = room.title,
                    content =
                        RoomCoverState.Content.Ready(
                            memberCount = users.size,
                            colors = colors.toImmutableList(),
                            stickers = stickers.toImmutableList(),
                            // 커버가 없는 방은 첫 색을 골라둔 채로 시작해, 스티커만 누르면 바로 색이 입혀진다.
                            selectedColor =
                                colors.find { it.id == room.cover.sticker?.color?.id } ?: colors.firstOrNull(),
                            selectedSticker = stickers.find { it.id == room.cover.sticker?.id },
                            backgroundImageUrl = room.cover.imageUrl,
                        ),
                )
            }
        }
    }

    /**
     * 스티커가 없으면 색이 드러날 곳이 없어 저장하지 않는다.
     * 고른 색은 화면에 남아, 이어서 스티커를 고르면 그 색과 함께 저장된다.
     */
    private fun handleColorClick(color: RoomCoverColorUiModel) {
        val ready = readyContent() ?: return
        if (ready.selectedColor == color) return

        val next = ready.copy(selectedColor = color)
        updateState { copy(content = next) }
        if (next.selectedSticker != null) saveCover(next)
    }

    private fun handleStickerClick(sticker: RoomCoverStickerUiModel) {
        val ready = readyContent() ?: return

        val next = ready.copy(selectedSticker = sticker.takeIf { it != ready.selectedSticker })
        updateState { copy(content = next) }
        saveCover(next)
    }

    /**
     * 고른 사진은 먼저 미리보기에만 반영하고, 업로드로 공개 URL을 받은 뒤에 저장한다.
     * 로컬 URI를 [RoomCoverState.Content.Ready.backgroundImageUrl]에 넣으면
     * 그 사이에 스티커나 색을 바꿀 때 서버가 읽을 수 없는 주소가 함께 저장된다.
     */
    private fun handleBackgroundImageSelect(imageUri: String) {
        val ready = readyContent() ?: return

        updateState { copy(content = ready.copy(pendingImageUri = imageUri)) }

        uploadJob?.cancel()
        uploadJob =
            viewModelScope.launch {
                imageUploadRepository
                    .uploadRoomCoverImage(imageUri)
                    .onSuccess { uploadedUrl ->
                        val uploaded =
                            readyContent()?.copy(
                                backgroundImageUrl = uploadedUrl,
                                pendingImageUri = null,
                            ) ?: return@onSuccess
                        updateState { copy(content = uploaded) }
                        saveCover(uploaded)
                    }.onFailure { failure ->
                        Timber.e(failure.causeOrNull(), "커버 배경 이미지를 올리지 못했습니다. roomId=$roomId")
                        restoreSavedCover()
                        sendEffect(RoomCoverSideEffect.BackgroundImageUploadFailed)
                    }
            }
    }

    private fun handleBackgroundImageRemove() {
        val ready = readyContent() ?: return
        if (ready.backgroundImageUrl == null && ready.pendingImageUri == null) return

        // 올리던 사진이 뒤늦게 도착해 지운 배경을 되살리지 않도록 업로드부터 끊는다.
        uploadJob?.cancel()
        val next = ready.copy(backgroundImageUrl = null, pendingImageUri = null)
        updateState { copy(content = next) }
        saveCover(next)
    }

    /**
     * 앞선 저장이 끝난 뒤에 보낸다. 이미 나간 요청은 취소해도 서버가 처리하므로,
     * 순서를 맞추지 않으면 앞선 선택이 마지막 선택보다 늦게 도착해 서버에 남을 수 있다.
     * 기다리는 사이 선택이 또 바뀌면 중간 요청은 보내지 않고 마지막 것만 보낸다.
     */
    private fun saveCover(ready: RoomCoverState.Content.Ready) {
        val cover = ready.toRoomCover()
        latestCover = cover

        val previousSave = saveJob
        saveJob =
            viewModelScope.launch {
                previousSave?.join()
                if (latestCover != cover) return@launch

                roomRepository
                    .updateRoomCover(roomId = roomId, cover = cover)
                    .onSuccess { savedCover = cover }
                    .onFailure { failure ->
                        Timber.e(failure.causeOrNull(), "방 커버를 저장하지 못했습니다. roomId=$roomId")
                        // 이어서 보낼 선택이 있으면 그 결과를 따른다. 여기서 되돌리면 최신 선택이 화면에서 사라진다.
                        if (latestCover != cover) return@onFailure
                        restoreSavedCover()
                        sendEffect(RoomCoverSideEffect.CoverUpdateFailed)
                    }
            }
    }

    private fun restoreSavedCover() {
        val ready = readyContent() ?: return
        updateState {
            copy(
                content =
                    ready.copy(
                        selectedSticker = ready.stickers.find { it.id == savedCover.sticker?.id },
                        // 스티커가 없는 커버에는 색이 없다. 팔레트 선택은 그대로 둔다.
                        selectedColor =
                            ready.colors.find { it.id == savedCover.sticker?.color?.id } ?: ready.selectedColor,
                        backgroundImageUrl = savedCover.imageUrl,
                        pendingImageUri = null,
                    ),
            )
        }
    }

    /** 화면의 선택을 서버에 보낼 커버로 되돌린다. 색을 고르지 않았으면 스티커도 그릴 수 없어 함께 비운다. */
    private fun RoomCoverState.Content.Ready.toRoomCover(): RoomCover =
        RoomCover(
            imageUrl = backgroundImageUrl,
            sticker =
                selectedSticker?.let { sticker ->
                    selectedColor?.let { color ->
                        RoomCoverSticker(
                            id = sticker.id,
                            imageUrl = sticker.imageUrl,
                            color = RoomCoverColor(id = color.id, hex = color.hex),
                        )
                    }
                },
        )

    /** 목록을 그린 뒤에만 올라오는 인텐트라 늘 Ready다. 아니면 상태가 어긋난 것이므로 남긴다. */
    private fun readyContent(): RoomCoverState.Content.Ready? {
        val content = currentState.content
        if (content !is RoomCoverState.Content.Ready) {
            Timber.w("커버를 고를 수 없는 상태에서 인텐트가 올라왔습니다. content=$content")
            return null
        }
        return content
    }

    /** 세 응답을 함께 봐야 해서 값으로 꺼낸다. 실패면 원인을 남기고 null. */
    private fun <T> ChallaResult<T>.valueOrLog(what: String): T? {
        onFailure { Timber.e(it.causeOrNull(), "${what}을(를) 불러오지 못했습니다. roomId=$roomId") }
        return (this as? ChallaResult.Success)?.data
    }

    @AssistedFactory
    interface Factory {
        fun create(roomId: Long): RoomCoverViewModel
    }
}
