package com.happyhouse.challa.data.network.adapter

import com.happyhouse.challa.domain.result.ChallaResult
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.lang.reflect.Type

internal class ChallaResultCall<T>(
    private val delegate: Call<T>,
    private val successType: Type,
) : Call<ChallaResult<T>> {
    override fun enqueue(callback: Callback<ChallaResult<T>>) {
        delegate.enqueue(
            object : Callback<T> {
                override fun onResponse(
                    call: Call<T>,
                    response: Response<T>,
                ) {
                    callback.onResponse(
                        this@ChallaResultCall,
                        Response.success(response.toChallaResult()),
                    )
                }

                override fun onFailure(
                    call: Call<T>,
                    throwable: Throwable,
                ) {
                    callback.onResponse(
                        this@ChallaResultCall,
                        Response.success(throwable.toChallaFailure()),
                    )
                }
            },
        )
    }

    override fun execute(): Response<ChallaResult<T>> =
        try {
            Response.success(delegate.execute().toChallaResult())
        } catch (throwable: Throwable) {
            Response.success(throwable.toChallaFailure())
        }

    override fun clone(): Call<ChallaResult<T>> = ChallaResultCall(delegate.clone(), successType)

    override fun request(): Request = delegate.request()

    override fun timeout(): Timeout = delegate.timeout()

    override fun isExecuted(): Boolean = delegate.isExecuted

    override fun isCanceled(): Boolean = delegate.isCanceled

    override fun cancel() = delegate.cancel()

    @Suppress("UNCHECKED_CAST")
    private fun Response<T>.toChallaResult(): ChallaResult<T> =
        if (isSuccessful) {
            val body = body()
            if (body != null) {
                ChallaResult.Success(body)
            } else if (successType == Unit::class.java) {
                ChallaResult.Success(Unit as T)
            } else {
                ChallaResult.Failure.Unknown(null)
            }
        } else {
            ChallaResult.Failure.Http(
                code = code(),
                message = errorBody()?.string().takeUnless { it.isNullOrBlank() } ?: message(),
            )
        }

    private fun Throwable.toChallaFailure(): ChallaResult.Failure =
        when (this) {
            is IOException -> ChallaResult.Failure.Network(this)
            else -> ChallaResult.Failure.Unknown(this)
        }
}
