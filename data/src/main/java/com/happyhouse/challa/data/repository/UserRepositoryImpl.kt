package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.local.ThemeDataStore
import com.happyhouse.challa.data.local.TokenDataStore
import com.happyhouse.challa.data.local.UserProfileCache
import com.happyhouse.challa.data.network.api.UserApi
import com.happyhouse.challa.data.network.dto.request.UpdateProfileRequest
import com.happyhouse.challa.domain.model.UserProfile
import com.happyhouse.challa.domain.repository.UserRepository
import com.happyhouse.challa.domain.result.ChallaResult
import com.happyhouse.challa.domain.result.mapCatching
import com.happyhouse.challa.domain.result.onSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl
    @Inject
    constructor(
        private val userApi: UserApi,
        private val tokenDataStore: TokenDataStore,
        private val themeDataStore: ThemeDataStore,
        private val userProfileCache: UserProfileCache,
    ) : UserRepository {
        override val profile: StateFlow<UserProfile?> = userProfileCache.profile

        override suspend fun prefetchMyProfile() {
            if (profile.value != null) return

            getMyProfile()
        }

        override suspend fun withdraw(): ChallaResult<Unit> =
            try {
                userApi
                    .withdraw()
                    .mapCatching { response ->
                        check(response.success) { response.message }
                    }.onSuccess {
                        themeDataStore.clearPrimaryTheme()
                        tokenDataStore.clear()
                        userProfileCache.clear()
                    }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                ChallaResult.Failure.Unknown(throwable)
            }

        override suspend fun getMyProfile(): ChallaResult<UserProfile> =
            userApi
                .getMyProfile()
                .mapCatching { response ->
                    check(response.success) { response.message }
                    val user = requireNotNull(response.data) { "프로필 응답 데이터가 비어 있습니다." }.user
                    UserProfile(
                        id = user.id,
                        nickname = user.nickname,
                        profileImageUrl = user.profileImageUrl,
                    )
                }.onSuccess { profile ->
                    userProfileCache.update(profile)
                }

        override suspend fun updateProfile(
            nickname: String,
            profileImageUrl: String?,
        ): ChallaResult<UserProfile> =
            userApi
                .updateProfile(
                    UpdateProfileRequest(
                        user =
                            UpdateProfileRequest.User(
                                nickname = nickname,
                                profileImageUrl = profileImageUrl,
                            ),
                    ),
                ).mapCatching { response ->
                    check(response.success) { response.message }
                    val user = requireNotNull(response.data) { "프로필 응답 데이터가 비어 있습니다." }.user
                    val updatedNickname = requireNotNull(user.nickname) { "수정된 프로필 닉네임이 비어 있습니다." }
                    UserProfile(
                        id = user.id,
                        nickname = updatedNickname,
                        profileImageUrl = user.profileImageUrl,
                    )
                }.onSuccess { profile ->
                    userProfileCache.update(profile)
                }
    }
