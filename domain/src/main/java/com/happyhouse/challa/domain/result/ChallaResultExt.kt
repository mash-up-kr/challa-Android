package com.happyhouse.challa.domain.result

import kotlinx.coroutines.CancellationException

inline fun <T, R> ChallaResult<T>.map(transform: (T) -> R): ChallaResult<R> =
    when (this) {
        is ChallaResult.Success -> ChallaResult.Success(transform(data))
        is ChallaResult.Failure -> this
    }

inline fun <T, R> ChallaResult<T>.mapCatching(transform: (T) -> R): ChallaResult<R> =
    when (this) {
        is ChallaResult.Success ->
            runCatching {
                transform(data)
            }.fold(
                onSuccess = { ChallaResult.Success(it) },
                onFailure = { throwable ->
                    if (throwable is CancellationException) throw throwable
                    ChallaResult.Failure.Unknown(throwable)
                },
            )

        is ChallaResult.Failure -> this
    }

inline fun <T> ChallaResult<T>.onSuccess(action: (T) -> Unit): ChallaResult<T> =
    apply {
        if (this is ChallaResult.Success) {
            action(data)
        }
    }

inline fun <T> ChallaResult<T>.onFailure(action: (ChallaResult.Failure) -> Unit): ChallaResult<T> =
    apply {
        if (this is ChallaResult.Failure) {
            action(this)
        }
    }

inline fun <T> ChallaResult<T>.mapFailure(transform: (ChallaResult.Failure) -> ChallaResult.Failure): ChallaResult<T> =
    when (this) {
        is ChallaResult.Success -> this
        is ChallaResult.Failure -> transform(this)
    }
