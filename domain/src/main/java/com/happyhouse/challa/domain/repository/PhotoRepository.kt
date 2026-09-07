package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.model.PhotoPage
import com.happyhouse.challa.domain.result.ChallaResult

interface PhotoRepository {
    /**
     * 방의 사진을 오래된 사진부터 촬영 순서대로 조회한다.
     * 다음 페이지는 이전 페이지의 마지막 사진보다 나중에 촬영된 사진으로 이어진다.
     *
     * @param page 0부터 시작하는 페이지 번호
     */
    suspend fun getPhotos(
        roomId: Long,
        page: Int,
    ): ChallaResult<PhotoPage>

    suspend fun savePhoto(imageUrl: String): Result<Unit>
}
