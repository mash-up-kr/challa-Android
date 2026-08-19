package com.happyhouse.challa.data.repository

import com.happyhouse.challa.data.local.RoomVisitDataStore
import com.happyhouse.challa.domain.repository.RoomVisitRepository
import com.happyhouse.challa.domain.result.ChallaResult
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomVisitRepositoryImpl @Inject constructor(
    private val roomVisitDataStore: RoomVisitDataStore,
) : RoomVisitRepository {
    override suspend fun markVisited(roomId: Long): ChallaResult<Boolean> =
        try {
            ChallaResult.Success(roomVisitDataStore.markVisited(roomId))
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            ChallaResult.Failure.Unknown(throwable)
        }
}
