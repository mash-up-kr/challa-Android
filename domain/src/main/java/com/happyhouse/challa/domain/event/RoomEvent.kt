package com.happyhouse.challa.domain.event

import com.happyhouse.challa.domain.model.RoomCover

/**
 * 방에 생긴 변화를 알리는 이벤트.
 *
 * 같은 방을 그리고 있는 화면이 여럿이라, 한 화면에서 바꾼 내용을 나머지 화면이 다시 조회하지 않고
 * 반영할 수 있도록 이 타입으로 흘려보낸다. 어떤 방인지 가려내야 하므로 모든 이벤트가 [roomId]를 갖는다.
 */
sealed interface RoomEvent {
    val roomId: Long

    /** 방 이름이 [title]로 바뀌었다. */
    data class TitleUpdate(
        override val roomId: Long,
        val title: String,
    ) : RoomEvent

    /** 방 커버가 [cover]로 바뀌었다. */
    data class CoverUpdate(
        override val roomId: Long,
        val cover: RoomCover,
    ) : RoomEvent

    /** 인화 완료를 확인했다고 서버에 기록됐다. */
    data class PhotoPrintCompletionCheck(
        override val roomId: Long,
    ) : RoomEvent
}
