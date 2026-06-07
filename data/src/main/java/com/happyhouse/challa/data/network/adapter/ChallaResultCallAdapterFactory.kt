package com.happyhouse.challa.data.network.adapter

import com.happyhouse.challa.domain.result.ChallaResult
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ChallaResultCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit,
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) {
            return null
        }

        check(returnType is ParameterizedType) {
            "API 반환 타입은 suspend fun ...(): ChallaResult<Foo> 형태로 지정해야 합니다."
        }

        val callType = getParameterUpperBound(0, returnType)
        if (getRawType(callType) != ChallaResult::class.java) {
            return null
        }

        check(callType is ParameterizedType) {
            "ChallaResult는 ChallaResult<Foo> 형태로 성공 타입을 지정해야 합니다."
        }

        val successType = getParameterUpperBound(0, callType)
        return ChallaResultCallAdapter<Any>(successType)
    }
}
