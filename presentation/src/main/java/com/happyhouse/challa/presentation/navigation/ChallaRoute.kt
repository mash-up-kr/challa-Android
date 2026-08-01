package com.happyhouse.challa.presentation.navigation

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
sealed interface ChallaRoute : NavKey {
    @Serializable
    data object Sample : ChallaRoute

    @Serializable
    data class Camera(
        val roomId: Long,
    ) : ChallaRoute

    @Serializable
    data class PhotoDetail(
        val roomId: Long,
        val photoId: Long,
    ) : ChallaRoute

    @Serializable
    data class Gallery(
        val roomId: Long,
    ) : ChallaRoute

    @Serializable
    data object Login : ChallaRoute

    @Serializable
    data object CreateProfile : ChallaRoute

    @Serializable
    data object RoomMain : ChallaRoute

    @Serializable
    data object Home : ChallaRoute

    @Serializable
    data class ShareInvite(
        val roomId: String,
        val roomName: String,
    ) : ChallaRoute
}
