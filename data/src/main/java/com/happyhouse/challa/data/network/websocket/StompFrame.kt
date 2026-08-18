package com.happyhouse.challa.data.network.websocket

internal data class StompFrame(
    val command: String,
    val headers: Map<String, String>,
    val body: String,
)

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

internal fun stompConnectFrame(): String =
    buildString {
        appendLine("CONNECT")
        appendLine("accept-version:1.2")
        appendLine("heart-beat:0,0")
        appendLine()
        append(STOMP_FRAME_TERMINATOR)
    }

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

internal fun stompSubscriptionReceiptId(roomId: Long): String = "room-member-joined-$roomId"

private const val STOMP_FRAME_TERMINATOR = '\u0000'
