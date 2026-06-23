package com.happyhouse.challa.presentation.photodetail

import android.content.Context

/**
 * imageUrl의 사진을 기기 갤러리에 저장한다.
 *
 * TODO: 실제 저장 로직은 후속 이슈에서 구현
 */
suspend fun savePhotoToMediaStore(
    context: Context,
    imageUrl: String,
): Result<Unit> {
    return Result.failure(NotImplementedError("savePhotoToMediaStore 미구현"))
}
