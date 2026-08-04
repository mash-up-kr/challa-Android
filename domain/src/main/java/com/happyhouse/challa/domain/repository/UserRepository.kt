package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.model.UserProfile
import com.happyhouse.challa.domain.result.ChallaResult

interface UserRepository {
    suspend fun withdraw(): ChallaResult<Unit>

    suspend fun updateProfile(
        nickname: String,
        profileImageUrl: String?,
    ): ChallaResult<UserProfile>
}
