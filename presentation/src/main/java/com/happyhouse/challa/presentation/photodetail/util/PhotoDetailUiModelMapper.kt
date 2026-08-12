package com.happyhouse.challa.presentation.photodetail.util

import com.happyhouse.challa.domain.model.Photo
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** 사진 위에 그리는 촬영 시각 형식. (예: 2026. 7. 16. 14:34) */
private val CapturedDateFormatter = DateTimeFormatter.ofPattern("yyyy. M. d. HH:mm")

/**
 * 방 사진 목록을 사진 상세 페이지 모델로 옮긴다. 순서는 서버가 내려준 촬영 순서를 그대로 따른다.
 */
internal fun List<Photo>.toPhotoDetailUiModels(): ImmutableList<PhotoDetailUiModel> =
    map { photo ->
        PhotoDetailUiModel(
            id = photo.id,
            imageUrl = photo.imageUrl,
            photographer = photo.photographerNickname,
            capturedDate = photo.createdAt.toCapturedDate(),
        )
    }.toPersistentList()

/** 촬영 시각은 서버가 UTC 기준으로 내려주므로 보는 사람의 시간대로 바꿔 그린다. */
private fun Instant.toCapturedDate(): String = CapturedDateFormatter.format(atZone(ZoneId.systemDefault()))
