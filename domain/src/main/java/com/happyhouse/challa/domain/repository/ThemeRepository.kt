package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.model.PrimaryTheme
import com.happyhouse.challa.domain.result.ChallaResult
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    val primaryTheme: Flow<ChallaResult<PrimaryTheme>>

    suspend fun updatePrimaryTheme(theme: PrimaryTheme): ChallaResult<Unit>

    fun retryPrimaryThemeRead()
}
