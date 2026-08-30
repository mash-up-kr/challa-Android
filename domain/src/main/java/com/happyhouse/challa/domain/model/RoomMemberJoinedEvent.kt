package com.happyhouse.challa.domain.model

/**
 * 사용자가 방에 새로 참여했을 때 실시간으로 전달되는 domain 이벤트.
 *
 * @property roomId 참여가 발생한 방 식별자.
 * @property roomTitle 알림에 표시할 방 제목.
 * @property nickname 새로 참여한 사용자 닉네임.
 * @property userProfileImageUrl 새로 참여한 사용자 프로필 이미지 URL. 이미지가 없으면 `null`이다.
 */
data class RoomMemberJoinedEvent(
    val roomId: Long,
    val roomTitle: String,
    val nickname: String,
    val userProfileImageUrl: String?,
)
