package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.result.ChallaResult

interface ClipboardRepository {
    /** 문자열을 시스템 클립보드에 복사한다. */
    suspend fun copyText(text: String): ChallaResult<Unit>
}
