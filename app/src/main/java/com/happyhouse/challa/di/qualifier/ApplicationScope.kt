package com.happyhouse.challa.di.qualifier

import javax.inject.Qualifier

/** 앱 프로세스와 동일한 수명으로 유지되는 coroutine scope를 구분합니다. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
