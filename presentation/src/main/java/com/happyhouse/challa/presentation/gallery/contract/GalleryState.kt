package com.happyhouse.challa.presentation.gallery.contract

import androidx.compose.runtime.Immutable
import com.happyhouse.challa.presentation.base.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class GalleryState(
    val roomId: Long = 0L,
    val roomName: String = "",
    val invitationCode: String = "",
    val members: ImmutableList<GalleryMemberUiModel> = persistentListOf(),
    val inviteMenu: InviteMenu = InviteMenu.Closed,
    val photoInfo: PhotoInfo = PhotoInfo.Loading,
) : UiState {
    /** 프로필 바에 딸린 초대 코드·참여자 목록 메뉴의 상태 */
    @Immutable
    sealed interface InviteMenu {
        data object Closed : InviteMenu

        /** @param showsTooltip 방마다 첫 진입에서만 true. 메뉴가 닫히면 [Closed]가 되어 툴팁도 사라진다. */
        data class Opened(
            val showsTooltip: Boolean,
        ) : InviteMenu
    }

    @Immutable
    sealed interface PhotoInfo {
        data object Loading : PhotoInfo

        data object Error : PhotoInfo

        /**
         * 인화 전([Shooting] + [Waiting])을 묶은 타입.
         * 둘 다 필름 슬롯을 그리므로, 구분할 필요가 없는 곳은 이 타입으로 한 번에 분기한다.
         */
        @Immutable
        sealed interface Film : PhotoInfo {
            val slots: ImmutableList<GalleryFilmSlotUiModel>
        }

        /** 인화 전 - 촬영 중. 필름을 다 채우지 못해 아직 인화 시각이 잡히지 않았다. */
        data class Shooting(
            override val slots: ImmutableList<GalleryFilmSlotUiModel>,
        ) : Film

        /** 인화 전 - 인화 대기. 필름을 다 채워 인화 완료까지 남은 시간을 센다. */
        data class Waiting(
            override val slots: ImmutableList<GalleryFilmSlotUiModel>,
            val remainingSeconds: Long,
        ) : Film

        /**
         * 인화 완료 - 사진이 공개됐다.
         *
         * @param playsPrintAnimation 인화 연출을 재생해야 하는지. 끝까지 보면 false가 되어 그리드만 남는다.
         *   진행 단계(당김 대기 / 내려오는 중 / 사진 등장)는 화면 안에서만 쓰이므로 여기 두지 않는다.
         */
        data class Printed(
            val photos: ImmutableList<GalleryPhotoUiModel>,
            val playsPrintAnimation: Boolean,
        ) : PhotoInfo
    }
}

/**
 * 인화 전 필름 슬롯 UI 모델
 */
@Immutable
data class GalleryFilmSlotUiModel(
    val order: Int,
    val state: State,
) {
    /** 필름 한 칸의 상태 */
    @Immutable
    sealed interface State {
        /** 아직 촬영되지 않은 자리 */
        data object Empty : State

        /**
         * 촬영했지만 아직 인화되지 않은 자리. 원본을 앱에서 블러 처리해 보여준다.
         *
         * @param imageUrl 사진 목록이 촬영 수보다 짧아 이미지를 받지 못했으면 null. 이때도 빈 자리와는 구분해서 그린다.
         */
        data class Captured(
            val imageUrl: String?,
        ) : State
    }
}

/**
 * 방 참여자 UI 모델
 *
 * @param profileImageUrl 프로필 사진을 등록하지 않은 참여자는 null
 */
@Immutable
data class GalleryMemberUiModel(
    val id: Long,
    val nickname: String,
    val profileImageUrl: String?,
)

/**
 * 갤러리 썸네일 UI 모델
 */
@Immutable
data class GalleryPhotoUiModel(
    val id: Long,
    val order: Int,
    val imageUrl: String,
)
