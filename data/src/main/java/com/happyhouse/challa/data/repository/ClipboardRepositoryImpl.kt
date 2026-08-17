package com.happyhouse.challa.data.repository

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.happyhouse.challa.domain.repository.ClipboardRepository
import com.happyhouse.challa.domain.result.ChallaResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ClipboardRepository {
    override suspend fun copyText(text: String): ChallaResult<Unit> =
        try {
            val clipboardManager =
                requireNotNull(context.getSystemService(ClipboardManager::class.java)) {
                    "클립보드를 사용할 수 없습니다."
                }
            clipboardManager.setPrimaryClip(ClipData.newPlainText(CLIP_LABEL, text))
            ChallaResult.Success(Unit)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            ChallaResult.Failure.Unknown(throwable)
        }

    private companion object {
        const val CLIP_LABEL = "challa"
    }
}
