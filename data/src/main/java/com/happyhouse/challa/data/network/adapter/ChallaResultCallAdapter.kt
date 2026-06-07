package com.happyhouse.challa.data.network.adapter

import com.happyhouse.challa.domain.result.ChallaResult
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

internal class ChallaResultCallAdapter<T>(
    private val successType: Type,
) : CallAdapter<T, Call<ChallaResult<T>>> {
    override fun responseType(): Type = successType

    override fun adapt(call: Call<T>): Call<ChallaResult<T>> = ChallaResultCall(call, successType)
}
