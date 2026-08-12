package com.happyhouse.challa.data.network

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * 서버가 내려주는 ISO-8601 시각을 파싱한다.
 *
 * 오프셋이 붙어 오면 그대로 쓰고, 없으면 UTC로 본다.
 * 둘 다 아닌 값은 서버가 약속과 다른 응답을 준 것이므로 예외를 그대로 띄워 조회 실패로 만든다.
 * 시각을 아예 안 내려준 경우(null)와 형식이 깨진 경우를 구분하기 위함이다.
 */
internal fun String.parseServerInstant(): Instant =
    runCatching { OffsetDateTime.parse(this).toInstant() }
        .getOrElse { LocalDateTime.parse(this).toInstant(ZoneOffset.UTC) }
