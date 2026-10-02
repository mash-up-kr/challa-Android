# Add R8/ProGuard rules that should be applied by apps consuming this module.

# ChallaResultCallAdapterFactory가 중첩 generic 반환 타입을 reflection으로 조회합니다.
-keep,allowoptimization,allowobfuscation interface com.happyhouse.challa.domain.result.ChallaResult
-keep,allowoptimization,allowobfuscation class com.happyhouse.challa.data.network.dto.BaseResponse
