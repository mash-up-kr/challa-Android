package com.happyhouse.challa.presentation.room.main.util

import com.happyhouse.challa.presentation.model.RoomStatus

internal val RoomStatus.roomMainLabel: String
    get() =
        when (this) {
            is RoomStatus.Shooting -> "촬영중"
            is RoomStatus.Waiting -> "대기중"
            RoomStatus.Opened -> "공개됨"
            is RoomStatus.Expiring -> "삭제 임박"
        }

internal fun RoomStatus.roomMainDescription(requiredPhotoCount: Int): String =
    when (this) {
        is RoomStatus.Shooting -> "${requiredPhotoCount}장 완성 시 자동으로 3시간 카운트다운이 시작됩니다."
        is RoomStatus.Waiting -> "사진 ${requiredPhotoCount}장이 모두 모였어요. 공개까지 카운트다운이 진행 중입니다."
        RoomStatus.Opened -> "방이 공개되었어요. 갤러리에서 사진을 확인해보세요."
        is RoomStatus.Expiring -> "방이 곧 자동 삭제됩니다. 갤러리에서 사진을 확인해보세요."
    }

internal val RoomStatus.roomMainPrimaryButtonText: String
    get() =
        when (this) {
            is RoomStatus.Shooting,
            is RoomStatus.Waiting,
            -> "촬영하기"

            RoomStatus.Opened,
            is RoomStatus.Expiring,
            -> "갤러리 보기"
        }

internal val RoomStatus.isRoomMainPrimaryButtonEnabled: Boolean
    get() = this !is RoomStatus.Waiting
