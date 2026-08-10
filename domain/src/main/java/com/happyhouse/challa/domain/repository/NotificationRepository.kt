package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.result.ChallaResult
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    val isEnabled: Flow<ChallaResult<Boolean>>

    suspend fun setEnabled(enabled: Boolean): ChallaResult<Unit>
}
