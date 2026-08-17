package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.result.ChallaResult

interface ClipboardRepository {
    suspend fun copyText(text: String): ChallaResult<Unit>
}
