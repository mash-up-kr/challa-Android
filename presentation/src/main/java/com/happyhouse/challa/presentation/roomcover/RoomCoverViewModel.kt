package com.happyhouse.challa.presentation.roomcover

import androidx.lifecycle.viewModelScope
import com.happyhouse.challa.domain.model.RoomCover
import com.happyhouse.challa.domain.model.RoomCoverOptions
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
    @Assisted roomName: String,
    private val roomRepository: RoomRepository,
    private val imageUploadRepository: ImageUploadRepository,
) : BaseViewModel<RoomCoverState, RoomCoverIntent, RoomCoverSideEffect>(
        initialState = RoomCoverState(roomName = roomName),
    ) {
    /** 화면이 고른 id를 서버에 보낼 값으로 되돌리는 데 쓴다. */
    private var coverOptions: RoomCoverOptions? = null

    /** 마지막으로 저장에 성공한 커버. 저장이 실패하면 화면을 여기로 되돌린다. */
    private var savedCover: RoomCover = RoomCover()

    private var saveJob: Job? = null
    private var uploadJob: Job? = null

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

            coverOptions = options
            savedCover = room.cover

            val colors = options.colors.mapNotNull { it.toUiModelOrNull() }
            updateState {
                copy(
                    roomName = room.title,
                    content =
                        RoomCoverState.Content.Ready(
                            memberCount = users.size,
                            colors = colors.toImmutableList(),
                            stickers = options.stickers.map { it.toUiModel() }.toImmutableList(),
                            // 커버가 없는 방은 첫 색을 골라둔 채로 시작해, 스티커만 누르면 바로 색이 입혀진다.
                            selectedColorId = room.cover.sticker?.color?.id ?: colors.firstOrNull()?.id,
                            selectedStickerId = room.cover.sticker?.id,
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
        if (ready.selectedColorId == color.id) return

        val next = ready.copy(selectedColorId = color.id)
        updateState { copy(content = next) }
        if (next.selectedStickerId != null) saveCover(next)
    }

    private fun handleStickerClick(sticker: RoomCoverStickerUiModel) {
        val ready = readyContent() ?: return

        val next = ready.copy(selectedStickerId = sticker.id.takeIf { it != ready.selectedStickerId })
        updateState { copy(content = next) }
        saveCover(next)
    }

    private fun handleBackgroundImageSelect(imageUri: String) {
        val ready = readyContent() ?: return

        // 업로드가 끝나기 전에도 고른 사진이 보이도록 로컬 URI로 먼저 그린다.
        updateState { copy(content = ready.copy(backgroundImageUrl = imageUri)) }

        uploadJob?.cancel()
        uploadJob =
            viewModelScope.launch {
                imageUploadRepository
                    .uploadRoomCoverImage(imageUri)
                    .onSuccess { uploadedUrl ->
                        val uploaded = readyContent()?.copy(backgroundImageUrl = uploadedUrl) ?: return@onSuccess
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
        if (ready.backgroundImageUrl == null) return

        // 올리던 사진이 뒤늦게 도착해 지운 배경을 되살리지 않도록 업로드부터 끊는다.
        uploadJob?.cancel()
        val next = ready.copy(backgroundImageUrl = null)
        updateState { copy(content = next) }
        saveCover(next)
    }

    /** 연달아 고르면 앞선 요청을 버리고 마지막 선택만 남긴다. */
    private fun saveCover(ready: RoomCoverState.Content.Ready) {
        val cover = ready.toRoomCover() ?: return

        saveJob?.cancel()
        saveJob =
            viewModelScope.launch {
                roomRepository
                    .updateRoomCover(roomId = roomId, cover = cover)
                    .onSuccess { savedCover = cover }
                    .onFailure { failure ->
                        Timber.e(failure.causeOrNull(), "방 커버를 저장하지 못했습니다. roomId=$roomId")
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
                        selectedStickerId = savedCover.sticker?.id,
                        // 스티커가 없는 커버에는 색이 없다. 팔레트 선택은 그대로 둔다.
                        selectedColorId = savedCover.sticker?.color?.id ?: ready.selectedColorId,
                        backgroundImageUrl = savedCover.imageUrl,
                    ),
            )
        }
    }

    /** 화면의 선택을 서버에 보낼 커버로 되돌린다. 옵션 목록에 없는 선택이면 null이다. */
    private fun RoomCoverState.Content.Ready.toRoomCover(): RoomCover? {
        val stickerId = selectedStickerId ?: return RoomCover(imageUrl = backgroundImageUrl)

        val sticker = coverOptions?.stickers?.find { it.id == stickerId }
        val color = coverOptions?.colors?.find { it.id == selectedColorId }
        if (sticker == null || color == null) {
            Timber.w("커버 옵션에 없는 선택입니다. stickerId=$stickerId, colorId=$selectedColorId")
            return null
        }
        return RoomCover(
            imageUrl = backgroundImageUrl,
            sticker = RoomCoverSticker(id = sticker.id, imageUrl = sticker.imageUrl, color = color),
        )
    }

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
        fun create(
            roomId: Long,
            roomName: String,
        ): RoomCoverViewModel
    }
}
