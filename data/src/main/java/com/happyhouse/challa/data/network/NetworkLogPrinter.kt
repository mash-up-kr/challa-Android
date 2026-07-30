package com.happyhouse.challa.data.network

import okhttp3.logging.HttpLoggingInterceptor

internal class NetworkLogPrinter(
    private val logText: (String) -> Unit,
    private val logJson: (String) -> Unit,
) : HttpLoggingInterceptor.Logger {
    private val pendingLines =
        object : ThreadLocal<MutableList<String>>() {
            override fun initialValue(): MutableList<String> = mutableListOf()
        }

    override fun log(message: String) {
        val trimmedMessage = message.trimStart()

        when {
            message.isBlank() -> flushPendingLines()
            trimmedMessage.isExchangeStart() -> {
                flushPendingLines()
                logText(message)
            }

            trimmedMessage.isExchangeEnd() -> {
                flushPendingLines()
                logText(message)
                pendingLines.remove()
            }

            trimmedMessage.isJson() -> {
                flushPendingLines()
                logJson(message)
            }

            else -> currentLines().add(message)
        }
    }

    private fun flushPendingLines() {
        val lines = currentLines()
        if (lines.isEmpty()) return

        logText(lines.joinToString(separator = "\n"))
        lines.clear()
    }

    private fun currentLines(): MutableList<String> = checkNotNull(pendingLines.get())

    private fun String.isExchangeStart(): Boolean = (startsWith("-->") || startsWith("<--")) && !isExchangeEnd()

    private fun String.isExchangeEnd(): Boolean = startsWith("--> END") || startsWith("<-- END")

    private fun String.isJson(): Boolean = startsWith("{") || startsWith("[")
}
