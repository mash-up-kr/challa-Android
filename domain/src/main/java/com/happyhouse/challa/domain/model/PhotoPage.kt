package com.happyhouse.challa.domain.model

/**
 * 촬영 순서대로 조회한 사진 목록 한 페이지.
 *
 * @property photos 오래된 사진부터 촬영 순서대로 정렬된 사진 목록
 * @property hasNext 다음 페이지가 있는지 여부
 */
data class PhotoPage(
    val photos: List<Photo>,
    val hasNext: Boolean,
)
