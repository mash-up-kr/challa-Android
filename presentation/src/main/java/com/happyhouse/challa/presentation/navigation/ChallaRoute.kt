package com.happyhouse.challa.presentation.navigation

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
sealed interface ChallaRoute : NavKey {
    @Serializable
    data class Camera(
        val roomId: Long,
    ) : ChallaRoute

    @Serializable
    data class PhotoDetail(
        val roomId: Long,
        val photoId: Long,
    ) : ChallaRoute

    /**
     * @param playsPrintAnimation 인화 완료를 아직 확인하지 않은 방으로 들어갈 때 true.
     *   방 상세 응답에는 확인 여부가 없어 방 목록을 가진 홈이 판단해 넘긴다.
     */
    @Serializable
    data class Gallery(
        val roomId: Long,
        val playsPrintAnimation: Boolean = false,
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
    data object Home : ChallaRoute

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
