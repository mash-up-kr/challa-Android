package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.network.api.UserApi
import com.happyhouse.challa.data.network.dto.UpdateProfileRequest
import com.happyhouse.challa.domain.model.UserProfile
import com.happyhouse.challa.domain.repository.UserRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl
    @Inject
    constructor(
        private val userApi: UserApi,
    ) : UserRepository {
        override suspend fun updateProfile(
            nickname: String,
            profileImageUrl: String?,
        ): ChallaResult<UserProfile> =
            userApi
                .updateProfile(
                    UpdateProfileRequest(
                        user = UpdateProfileRequest.User(
                            nickname = nickname,
                            profileImageUrl = profileImageUrl,
                        ),
                    ),
                ).mapCatching { response ->
                    check(response.success) { response.message }
                    val user = requireNotNull(response.data) { "프로필 응답 데이터가 비어 있습니다." }.user
                    UserProfile(
                        id = user.id,
                        nickname = user.nickname,
                        profileImageUrl = user.profileImageUrl,
                    )
                }
    }
