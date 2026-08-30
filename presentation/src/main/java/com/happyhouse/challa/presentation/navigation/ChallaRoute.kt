package com.happyhouse.challa.presentation.navigation

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
sealed interface ChallaRoute : NavKey {
    /**
     * 특정 방을 컨텍스트로 가지며 해당 방의 [roomId]를 제공하는 Route다.
     *
     * [ChallaNavHost]는 현재 Route가 [RoomScoped]이면 홈 방 목록 반영 여부와 관계없이 해당 방을
     * 실시간 이벤트 구독 대상에 추가한다.
     */
    sealed interface RoomScoped : ChallaRoute {
        val roomId: Long
    }

    @Serializable
    data class Camera(
        override val roomId: Long,
    ) : RoomScoped

    @Serializable
    data class PhotoDetail(
        override val roomId: Long,
        val args: PhotoDetailArgs,
    ) : RoomScoped

    /** @param playsPrintAnimation 방 상세 응답에는 확인 여부가 없어 방 목록을 가진 홈이 판단해 넘긴다. */
    @Serializable
    data class Gallery(
        override val roomId: Long,
        val playsPrintAnimation: Boolean,
    ) : RoomScoped

    @Serializable
    data class RoomSetting(
        val roomId: Long,
        val roomName: String,
    ) : ChallaRoute

    @Serializable
    data object Login : ChallaRoute

    @Serializable
    data object SettingProfile : ChallaRoute

    @Serializable
    data class EditProfile(
        val nickname: String,
        val profileImageUrl: String?,
    ) : ChallaRoute

    @Serializable
    data class Home(
        /** 프로필 설정을 막 마치고 들어왔는지. 홈 진입 애니메이션 재생 여부를 가른다. */
        val fromProfileSetup: Boolean = false,
    ) : ChallaRoute

    @Serializable
    data object Setting : ChallaRoute

    @Serializable
    data object ThemeSetting : ChallaRoute

    @Serializable
    data object Notification : ChallaRoute

    @Serializable
    data object Account : ChallaRoute

    @Serializable
    data object OpenSourceLicense : ChallaRoute

    @Serializable
    data class ShareInvite(
        val roomId: String,
        val roomName: String,
    ) : ChallaRoute
}
