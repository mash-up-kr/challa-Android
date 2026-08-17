package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.result.ChallaResult

/**
 * 방에 처음 들어왔는지를 기기에 기록한다.
 *
 * 방을 만든 직후 초대 메뉴를 열어주는 안내처럼, 방마다 한 번만 보여줘야 하는 화면에 쓴다.
 */
interface RoomVisitRepository {
    /**
     * 방 진입을 기록하고, 이번 진입이 그 방의 첫 진입이었는지 알려준다.
     *
     * 확인과 기록을 한 번에 하므로, 같은 방으로 두 번 들어오면 두 번째부터는 false다.
     *
     * @return 이번 진입이 첫 진입이면 true
     */
    suspend fun markVisited(roomId: Long): ChallaResult<Boolean>
}
