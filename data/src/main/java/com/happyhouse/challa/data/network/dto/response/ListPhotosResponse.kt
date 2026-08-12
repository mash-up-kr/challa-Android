package com.happyhouse.challa.data.network.dto.response

import kotlinx.serialization.Serializable

/**
 * 방 사진 목록 응답
 *
 * @param hasNext 다음 페이지가 남아 있는지 여부
 */
@Serializable
data class ListPhotosResponse(
    val photos: List<Photo>,
    val hasNext: Boolean = false,
) {
    /**
     * @param imageUrl 스펙상 nullable. 미공개 사진에도 주소를 내려주는지 서버 확인 전이라 열어둔다.
     * @param userNickname 촬영자 닉네임. 서버가 nullable로 열어둬 없을 수 있다.
     * @param userProfileImageUrl 촬영자 프로필 사진. 등록하지 않았으면 null
     * @param createdAt 촬영 시각 (ISO-8601)
     */
    @Serializable
    data class Photo(
        val id: Long,
        val imageUrl: String? = null,
        val userNickname: String? = null,
        val userProfileImageUrl: String? = null,
        val createdAt: String,
    )
}
