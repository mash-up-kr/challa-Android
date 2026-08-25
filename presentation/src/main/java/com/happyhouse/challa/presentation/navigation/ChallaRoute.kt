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
        val args: PhotoDetailArgs,
    ) : ChallaRoute

    @Serializable
    data class Gallery(
        val roomId: Long,
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

/**
 * 갤러리가 이미 받아둔 사진과 페이징 위치.
 *
 * 상세가 같은 목록을 다시 조회하지 않도록 그대로 넘기고, 끝까지 넘겨 갤러리가 받아둔 범위를 벗어나면
 * [nextPhotoPage] 부터 이어 받는다.
 */
@Serializable
data class PhotoDetailArgs(
    val roomTitle: String,
    val photos: List<PhotoArg>,
    val nextPhotoPage: Int,
    val hasNextPhotoPage: Boolean,
) {
    @Serializable
    data class PhotoArg(
        val id: Long,
        val imageUrl: String,
        val photographerNickname: String,
        val photographerProfileImageUrl: String?,
        val createdAtEpochMillis: Long,
    )
}
