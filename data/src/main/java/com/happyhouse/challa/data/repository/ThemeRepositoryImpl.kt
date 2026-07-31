package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.local.ThemeDataStore
import com.happyhouse.challa.domain.model.PrimaryTheme
import com.happyhouse.challa.domain.repository.ThemeRepository
import com.happyhouse.challa.domain.result.ChallaResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * [ThemeDataStore]를 source of truth로 사용하는 [ThemeRepository] 구현체입니다.
 *
 * 테마 API가 추가되면 [updatePrimaryTheme]에서 로컬 값을 먼저 반영한 뒤 원격 저장을 수행해,
 * 사용자의 선택이 UI에 즉시 적용되도록 유지합니다.
 */
class ThemeRepositoryImpl @Inject constructor(
    private val themeDataStore: ThemeDataStore,
) : ThemeRepository {
    override val primaryTheme: Flow<PrimaryTheme> = themeDataStore.primaryTheme

    override suspend fun updatePrimaryTheme(theme: PrimaryTheme): ChallaResult<Unit> =
        try {
            themeDataStore.updatePrimaryTheme(theme)
            ChallaResult.Success(Unit)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            ChallaResult.Failure.Unknown(throwable)
        }
}
