package com.happyhouse.challa.data.network.dto.response

import com.happyhouse.challa.data.network.parseServerInstant
import com.happyhouse.challa.domain.model.Photo
import kotlinx.serialization.Serializable

/**
 * 방 사진 목록 응답
 *
 * @param hasNext 다음 페이지가 남아 있는지 여부. 기본값을 두지 않아 누락을 조회 실패로 드러낸다.
 *  기본값 false를 두면 서버가 이 필드를 빼먹었을 때 첫 페이지만 받고 조용히 끝나 사진이 사라진다.
 */
@Serializable
data class ListPhotosResponse(
    val photos: List<Photo>,
    val hasNext: Boolean,
) {
    /**
     * @param imageUrl 사진 주소. 미공개 사진에도 항상 내려온다(서버 확인 완료). 앱이 블러 처리해 보여준다.
     * @param userNickname 촬영자 닉네임. 스펙상 nullable이라 열어둔다.
     * @param userProfileImageUrl 촬영자 프로필 사진. 항상 내려온다(서버 확인 완료).
     * @param createdAt 촬영 시각 (ISO-8601). 촬영 시각은 사진 상세에서만 쓰는 값이라,
     *  누락돼도 갤러리까지 함께 실패하지 않도록 열어둔다. 형식이 깨진 경우는 조회 실패로 남긴다.
     */
    @Serializable
    data class Photo(
        val id: Long,
        val imageUrl: String,
        val userNickname: String? = null,
        val userProfileImageUrl: String,
        val createdAt: String? = null,
    )
}

internal fun ListPhotosResponse.Photo.toPhoto(): Photo =
    Photo(
        id = id,
        imageUrl = imageUrl,
        photographerNickname = userNickname,
        photographerProfileImageUrl = userProfileImageUrl,
        createdAt = createdAt?.parseServerInstant(),
    )
