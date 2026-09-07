package com.happyhouse.challa.domain.repository

import com.happyhouse.challa.domain.event.RoomEvent
import com.happyhouse.challa.domain.model.CreatedRoom
import com.happyhouse.challa.domain.model.Room
import com.happyhouse.challa.domain.model.RoomCover
import com.happyhouse.challa.domain.model.RoomCoverOptions
import com.happyhouse.challa.domain.model.RoomDetail
import com.happyhouse.challa.domain.model.RoomMemberJoinedEvent
import com.happyhouse.challa.domain.model.RoomStatus
import com.happyhouse.challa.domain.model.RoomUser
import com.happyhouse.challa.domain.model.ShootableRoom
import com.happyhouse.challa.domain.result.ChallaResult
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    /**
     * [roomIds]에 새 사용자가 참여할 때마다 이벤트를 전달한다.
     *
     * 반환하는 [Flow]는 cold stream이다. 수집이 시작되면 WebSocket 연결과 방별 구독을 시작하고,
     * 수집이 취소되면 연결을 정리한다. 구독할 방 목록을 변경하려면 새 [roomIds]로 다시 수집한다.
     * 자동으로 복구할 수 없는 연결 오류가 발생하면 수집자에게 예외를 전달한다.
     */
    fun observeMemberJoined(roomIds: Set<Long>): Flow<RoomMemberJoinedEvent>

    /**
     * 방에 생긴 변화를 알리는 이벤트 흐름.
     *
     * 한 화면에서 바꾼 내용을 같은 방을 그리는 다른 화면들이 다시 조회하지 않고 반영하기 위해 쓴다.
     * 구독하는 쪽은 필요한 [RoomEvent] 하위 타입과 [RoomEvent.roomId]만 걸러 쓰면 되고,
     * 지금 구독 중인 화면에만 전달되며 지난 이벤트는 다시 주지 않는다.
     */
    val roomEventFlow: Flow<RoomEvent>

    suspend fun getRoom(roomId: Long): ChallaResult<RoomDetail>

    suspend fun getRoomUsers(roomId: Long): ChallaResult<List<RoomUser>>

    suspend fun getRoomList(statuses: List<RoomStatus>): ChallaResult<List<Room>>

    suspend fun postRoom(
        title: String,
        totalPhotoCount: Int,
    ): ChallaResult<CreatedRoom>

    /**
     * 방 이름을 바꾼다.
     *
     * @param title 새 방 이름.
     */
    suspend fun updateRoomTitle(
        roomId: Long,
        title: String,
    ): ChallaResult<Unit>

    /**
     * 입장 코드로 방에 참여한다. 성공 시 참여한 방의 식별자를 반환한다.
     *
     * @param code 방 입장 코드.
     */
    suspend fun enterRoom(code: String): ChallaResult<CreatedRoom>

    suspend fun getShootableRooms(): ChallaResult<List<ShootableRoom>>

    /**
     * 방 커버를 바꾼다. 부분 갱신이 아니라 [cover] 전체로 교체한다.
     *
     * @param cover 새 커버. 배경 이미지나 스티커를 지우려면 해당 값을 null로 둔다.
     */
    suspend fun updateRoomCover(
        roomId: Long,
        cover: RoomCover,
    ): ChallaResult<Unit>

    /** 커버 수정 화면에서 고를 수 있는 스티커·색상 목록을 가져온다. */
    suspend fun getRoomCoverOptions(): ChallaResult<RoomCoverOptions>

    /** 인화 완료를 확인했다고 기록한다. 이후 방 목록의 `photoPrintCompletionCheckedAt`가 채워진다. */
    suspend fun checkPhotoPrintCompletion(roomId: Long): ChallaResult<Unit>
}
