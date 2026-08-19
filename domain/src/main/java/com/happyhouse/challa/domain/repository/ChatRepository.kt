package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.result.ChallaResult

interface ChatRepository {
    /** 사진 한 장에 메시지를 남긴다. */
    suspend fun sendPhotoMessage(
        roomId: Long,
        photoId: Long,
        message: String,
    ): ChallaResult<Unit>
}
