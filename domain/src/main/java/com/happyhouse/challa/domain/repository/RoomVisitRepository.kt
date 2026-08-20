package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.result.ChallaResult

/** 방마다 한 번만 보여줘야 하는 안내(초대 메뉴 자동 열림)를 위해 방 진입을 기기에 기록한다. */
interface RoomVisitRepository {
    /**
     * 방 진입을 기록한다. 확인과 기록을 한 번에 하므로 두 번째 진입부터는 false다.
     *
     * @return 이번 진입이 첫 진입이면 true
     */
    suspend fun markVisited(roomId: Long): ChallaResult<Boolean>
}
