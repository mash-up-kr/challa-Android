package com.happyhouse.challa.presentation.navigation

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
sealed interface ChallaRoute : NavKey {
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
        val photoId: Long,
    ) : RoomScoped

    @Serializable
    data class Gallery(
        override val roomId: Long,
    ) : RoomScoped

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
