package com.happyhouse.challa.domain.result

sealed interface ChallaResult<out T> {
    data class Success<T>(
        val data: T,
    ) : ChallaResult<T>

    sealed interface Failure : ChallaResult<Nothing> {
        data class Network(
            val cause: Throwable?,
        ) : Failure

        data class Http(
            val code: Int,
            val message: String?,
        ) : Failure

        data class Unknown(
            val cause: Throwable?,
        ) : Failure
    }
}
