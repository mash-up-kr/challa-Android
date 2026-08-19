package com.happyhouse.challa.presentation.photodetail.util

import com.happyhouse.challa.domain.model.Photo
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// 로케일을 고정하지 않으면 기기 설정에 따라 숫자 표기 자체가 바뀐다.
private val CapturedDateFormatter = DateTimeFormatter.ofPattern("yyyy. M. d. HH:mm", Locale.KOREA)

internal fun List<Photo>.toPhotoDetailUiModels(): ImmutableList<PhotoDetailUiModel> =
    map { photo ->
        PhotoDetailUiModel(
            id = photo.id,
            imageUrl = photo.imageUrl,
            photographer = photo.photographerNickname,
            photographerProfileImageUrl = photo.photographerProfileImageUrl,
            capturedDate = photo.createdAt.toCapturedDate(),
        )
    }.toPersistentList()

/** 서버가 UTC로 내려주므로 보는 사람의 시간대로 바꿔 그린다. */
private fun Instant.toCapturedDate(): String = CapturedDateFormatter.format(atZone(ZoneId.systemDefault()))
