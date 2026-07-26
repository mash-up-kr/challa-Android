package com.happyhouse.challa.data.repository

import com.happyhouse.challa.domain.model.RoomSummary
import com.happyhouse.challa.domain.repository.RoomRepository
import com.happyhouse.challa.domain.result.ChallaResult
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor() : RoomRepository {
    override suspend fun getRooms(): ChallaResult<List<RoomSummary>> =
        ChallaResult.Success(
            listOf(
                RoomSummary(
                    id = 1L,
                    name = "방이름1",
                    remainingCount = 24,
                    totalCount = 24,
                ),
                RoomSummary(
                    id = 2L,
                    name = "방이름방이름방이름2",
                    remainingCount = 6,
                    totalCount = 24,
                ),
                RoomSummary(
                    id = 3L,
                    name = "방이름방이름방이름3방이름",
                    remainingCount = 5,
                    totalCount = 48,
                ),
                RoomSummary(
                    id = 4L,
                    name = "방이름방이름방이름4",
                    remainingCount = 0,
                    totalCount = 48,
                ),
                RoomSummary(
                    id = 5L,
                    name = "방이름5",
                    remainingCount = 12,
                    totalCount = 24,
                ),
            ),
        )
}
