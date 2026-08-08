package com.happyhouse.challa.data.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okhttp3.logging.HttpLoggingInterceptor

internal class NetworkLogPrinter(
    private val printLog: (String) -> Unit,
) : HttpLoggingInterceptor.Logger {
    private val pendingLines =
        object : ThreadLocal<MutableList<String>>() {
            override fun initialValue(): MutableList<String> = mutableListOf()
        }

    override fun log(message: String) {
        val trimmedMessage = message.trimStart()

        when {
            trimmedMessage.isExchangeStart() -> {
                flushPendingLines()
                currentLines().add(message)
            }

            trimmedMessage.isExchangeEnd() -> {
                currentLines().add(message)
                flushPendingLines()
                pendingLines.remove()
            }

            trimmedMessage.isJson() -> currentLines().add(message.formatJson())

            else -> currentLines().add(message)
        }
    }

    private fun flushPendingLines() {
        val lines = currentLines()
        if (lines.isEmpty()) return

        printLog(lines.joinToString(separator = "\n"))
        lines.clear()
    }

    private fun currentLines(): MutableList<String> = checkNotNull(pendingLines.get())

    private fun String.isExchangeStart(): Boolean = (startsWith("-->") || startsWith("<--")) && !isExchangeEnd()

    private fun String.isExchangeEnd(): Boolean =
        startsWith("--> END") ||
            startsWith("<-- END") ||
            startsWith("<-- HTTP FAILED")

    private fun String.isJson(): Boolean = startsWith("{") || startsWith("[")

    private fun String.formatJson(): String =
        runCatching {
            val json = PRETTY_JSON.parseToJsonElement(this)
            PRETTY_JSON.encodeToString(JsonElement.serializer(), json)
        }.getOrDefault(this)

    private companion object {
        val PRETTY_JSON = Json { prettyPrint = true }
    }
}
