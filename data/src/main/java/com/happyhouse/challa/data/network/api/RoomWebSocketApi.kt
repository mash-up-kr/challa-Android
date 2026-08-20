package com.happyhouse.challa.data.network.api

import com.happyhouse.challa.data.BuildConfig
import com.happyhouse.challa.data.network.dto.BaseResponse
import com.happyhouse.challa.data.network.dto.response.RoomMemberJoinedResponse
import com.happyhouse.challa.data.network.websocket.stompConnectFrame
import com.happyhouse.challa.data.network.websocket.stompSubscribeFrame
import com.happyhouse.challa.data.network.websocket.stompSubscriptionReceiptId
import com.happyhouse.challa.data.network.websocket.toStompFrames
import com.orhanobut.logger.Logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retryWhen
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class RoomWebSocketApi
    @Inject
    constructor(
        private val okHttpClient: OkHttpClient,
        private val json: Json,
    ) {
        internal fun observeMemberJoined(roomIds: Set<Long>): Flow<RoomMemberJoinedResponse.Room> =
            openMemberJoinedStream(roomIds)
                .retryWhen { cause, attempt ->
                    if (cause is PermanentWebSocketException) {
                        Logger
                            .t(WEB_SOCKET_LOG_TAG)
                            .w("방 참여 WebSocket 영구 오류로 재연결하지 않습니다. roomIds=$roomIds, cause=$cause")
                        return@retryWhen false
                    }
                    if (cause !is IOException) return@retryWhen false

                    val retryDelay = min(INITIAL_RETRY_DELAY_MS * (attempt + 1), MAX_RETRY_DELAY_MS)
                    Logger
                        .t(WEB_SOCKET_LOG_TAG)
                        .w("방 참여 WebSocket 연결이 끊겨 재연결합니다. roomIds=$roomIds, delay=${retryDelay}ms, cause=$cause")
                    delay(retryDelay)
                    true
                }
                .catch { cause ->
                    if (cause !is PermanentWebSocketException) throw cause
                }

        private fun openMemberJoinedStream(roomIds: Set<Long>): Flow<RoomMemberJoinedResponse.Room> =
            callbackFlow {
                val disposed = AtomicBoolean(false)
                val roomIdByReceiptId = roomIds.associateBy(::stompSubscriptionReceiptId)
                val confirmedRoomIds = mutableSetOf<Long>()
                val request =
                    Request
                        .Builder()
                        .url(webSocketUrl())
                        .header(STOMP_PROTOCOL_HEADER, STOMP_PROTOCOL_VERSION)
                        .build()

                val listener =
                    object : WebSocketListener() {
                        override fun onOpen(
                            webSocket: WebSocket,
                            response: Response,
                        ) {
                            if (!webSocket.send(stompConnectFrame())) {
                                close(IOException("STOMP CONNECT frame을 전송하지 못했습니다."))
                            }
                        }

                        override fun onMessage(
                            webSocket: WebSocket,
                            text: String,
                        ) {
                            text.toStompFrames().forEach { frame ->
                                when (frame.command) {
                                    STOMP_CONNECTED -> {
                                        for (roomId in roomIds) {
                                            val sent = webSocket.send(stompSubscribeFrame(roomId))
                                            Logger.t(WEB_SOCKET_LOG_TAG).d(
                                                "방 참여 이벤트 구독 요청을 전송했습니다. " +
                                                    "roomId=$roomId, receiptId=${stompSubscriptionReceiptId(roomId)}, sent=$sent",
                                            )
                                            if (!sent) {
                                                close(IOException("STOMP SUBSCRIBE frame을 전송하지 못했습니다. roomId=$roomId"))
                                                break
                                            }
                                        }
                                    }

                                    STOMP_MESSAGE -> handleMessage(frame.body)
                                    STOMP_RECEIPT -> handleReceipt(frame.headers[STOMP_RECEIPT_ID_HEADER])
                                    STOMP_ERROR ->
                                        close(
                                            PermanentWebSocketException(
                                                frame.body.ifBlank { "STOMP 오류가 발생했습니다." },
                                            ),
                                        )
                                }
                            }
                        }

                        override fun onClosing(
                            webSocket: WebSocket,
                            code: Int,
                            reason: String,
                        ) {
                            webSocket.close(code, reason)
                        }

                        override fun onClosed(
                            webSocket: WebSocket,
                            code: Int,
                            reason: String,
                        ) {
                            if (!disposed.get()) {
                                close(IOException("WebSocket 연결이 종료되었습니다. code=$code, reason=$reason"))
                            }
                        }

                        override fun onFailure(
                            webSocket: WebSocket,
                            t: Throwable,
                            response: Response?,
                        ) {
                            if (!disposed.get()) {
                                val failure =
                                    response
                                        ?.takeUnless { it.isRetryableHandshakeFailure() }
                                        ?.let {
                                            PermanentWebSocketException(
                                                message = "WebSocket handshake에 실패했습니다. code=${it.code}",
                                                cause = t,
                                            )
                                        } ?: t
                                close(failure)
                            }
                        }

                        private fun handleMessage(body: String) {
                            val response =
                                runCatching { json.decodeFromString<BaseResponse<RoomMemberJoinedResponse>>(body) }
                                    .onFailure {
                                        Logger.t(WEB_SOCKET_LOG_TAG).w("WebSocket 응답을 해석하지 못했습니다: $body, cause=$it")
                                    }
                                    .getOrNull()
                                    ?: return

                            if (!response.success) {
                                close(PermanentWebSocketException(response.message))
                                return
                            }

                            val room = response.data?.room
                            if (room == null) {
                                Logger.t(WEB_SOCKET_LOG_TAG).w("방 참여 WebSocket 응답에 room 데이터가 없습니다: $body")
                                return
                            }

                            val result = trySend(room)
                            if (result.isFailure) {
                                Logger.t(WEB_SOCKET_LOG_TAG).w(
                                    "방 참여 이벤트를 전달하지 못했습니다. roomId=${room.id}, cause=${result.exceptionOrNull()}",
                                )
                            }
                        }

                        private fun handleReceipt(receiptId: String?) {
                            val roomId = receiptId?.let(roomIdByReceiptId::get)
                            if (roomId == null) {
                                Logger.t(WEB_SOCKET_LOG_TAG).w("알 수 없는 STOMP RECEIPT를 수신했습니다. receiptId=$receiptId")
                                return
                            }

                            if (confirmedRoomIds.add(roomId)) {
                                Logger.t(WEB_SOCKET_LOG_TAG).d(
                                    "방 참여 이벤트 구독이 확인되었습니다. roomId=$roomId, receiptId=$receiptId",
                                )
                                if (confirmedRoomIds.size == roomIds.size) {
                                    Logger.t(WEB_SOCKET_LOG_TAG).d(
                                        "모든 방 참여 이벤트 구독이 확인되었습니다. roomIds=$confirmedRoomIds",
                                    )
                                }
                            }
                        }
                    }

                val webSocket = okHttpClient.newWebSocket(request, listener)
                awaitClose {
                    disposed.set(true)
                    webSocket.close(NORMAL_CLOSURE_CODE, NORMAL_CLOSURE_REASON)
                }
            }

        private fun webSocketUrl(): String =
            BuildConfig.BASE_URL
                .trimEnd('/')
                .replaceFirst("https://", "wss://")
                .replaceFirst("http://", "ws://") + WEB_SOCKET_PATH

        private fun Response.isRetryableHandshakeFailure(): Boolean =
            code == HTTP_REQUEST_TIMEOUT ||
                code == HTTP_TOO_MANY_REQUESTS ||
                code in HTTP_SERVER_ERROR_RANGE

        private class PermanentWebSocketException(
            message: String,
            cause: Throwable? = null,
        ) : IOException(message, cause)

        private companion object {
            const val WEB_SOCKET_PATH = "/api/v1/ws"
            const val STOMP_PROTOCOL_HEADER = "Sec-WebSocket-Protocol"
            const val STOMP_PROTOCOL_VERSION = "v12.stomp"
            const val STOMP_CONNECTED = "CONNECTED"
            const val STOMP_MESSAGE = "MESSAGE"
            const val STOMP_RECEIPT = "RECEIPT"
            const val STOMP_ERROR = "ERROR"
            const val STOMP_RECEIPT_ID_HEADER = "receipt-id"
            const val NORMAL_CLOSURE_CODE = 1000
            const val NORMAL_CLOSURE_REASON = "Room subscription disposed"
            const val INITIAL_RETRY_DELAY_MS = 1_000L
            const val MAX_RETRY_DELAY_MS = 10_000L
            const val WEB_SOCKET_LOG_TAG = "RoomWebSocket"
            const val HTTP_REQUEST_TIMEOUT = 408
            const val HTTP_TOO_MANY_REQUESTS = 429
            val HTTP_SERVER_ERROR_RANGE = 500..599
        }
    }
