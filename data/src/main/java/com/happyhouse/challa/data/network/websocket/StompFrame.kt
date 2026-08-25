package com.happyhouse.challa.data.network.websocket

/**
 * 방 참여 알림에서 사용하는 STOMP frame의 최소 표현이다.
 *
 * 범용 STOMP client가 아니라 현재 서버 계약에 필요한 `CONNECTED`, `MESSAGE`, `RECEIPT`,
 * `ERROR` frame을 읽고 `CONNECT`, `SUBSCRIBE` frame을 만드는 용도로만 사용한다.
 */
internal data class StompFrame(
    val command: String,
    val headers: Map<String, String>,
    val body: String,
)

/** 하나의 WebSocket text message에 포함된 NUL 종료 STOMP frame들을 분리하고 해석한다. */
internal fun String.toStompFrames(): List<StompFrame> =
    split(STOMP_FRAME_TERMINATOR)
        .mapNotNull { rawFrame -> rawFrame.trimStart('\r', '\n').toStompFrameOrNull() }

private fun String.toStompFrameOrNull(): StompFrame? {
    if (isBlank()) return null

    val normalized = replace("\r\n", "\n")
    val headerEnd = normalized.indexOf("\n\n")
    val headerBlock = if (headerEnd >= 0) normalized.substring(0, headerEnd) else normalized
    val lines = headerBlock.lines()
    val command = lines.firstOrNull()?.takeIf { it.isNotBlank() } ?: return null
    val headers =
        lines
            .drop(1)
            .mapNotNull { line ->
                val separator = line.indexOf(':')
                if (separator <= 0) return@mapNotNull null
                line.substring(0, separator) to line.substring(separator + 1)
            }.toMap()
    val body = if (headerEnd >= 0) normalized.substring(headerEnd + 2) else ""
    return StompFrame(command = command, headers = headers, body = body)
}

/** [host]를 virtual host로 지정해 STOMP 1.2 세션을 시작하는 `CONNECT` frame을 만든다. */
internal fun stompConnectFrame(host: String): String =
    buildString {
        appendLine("CONNECT")
        appendLine("accept-version:1.2")
        appendLine("host:$host")
        appendLine("heart-beat:0,0")
        appendLine()
        append(STOMP_FRAME_TERMINATOR)
    }

/**
 * [roomId]의 참여 이벤트 destination을 구독하는 `SUBSCRIBE` frame을 만든다.
 *
 * 메시지는 자동 승인(`ack:auto`)하고, 서버가 구독 완료를 확인할 수 있도록 receipt를 요청한다.
 */
internal fun stompSubscribeFrame(roomId: Long): String =
    buildString {
        appendLine("SUBSCRIBE")
        appendLine("id:room-member-joined-$roomId")
        appendLine("destination:/topic/room/$roomId/member-joined")
        appendLine("ack:auto")
        appendLine("receipt:${stompSubscriptionReceiptId(roomId)}")
        appendLine()
        append(STOMP_FRAME_TERMINATOR)
    }

/** 방별 구독 요청과 서버 `RECEIPT`를 연결하는 안정적인 식별자를 반환한다. */
internal fun stompSubscriptionReceiptId(roomId: Long): String = "room-member-joined-$roomId"

private const val STOMP_FRAME_TERMINATOR = '\u0000'
