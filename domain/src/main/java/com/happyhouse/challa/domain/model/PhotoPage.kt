package com.happyhouse.challa.domain.model

/** 사진 목록 한 페이지 */
data class PhotoPage(
    val photos: List<Photo>,
    val hasNext: Boolean,
)
