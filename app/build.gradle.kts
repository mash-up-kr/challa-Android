import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

val localProperties =
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use(::load)
        }
    }

// 카카오 SDK 설정
val kakaoNativeAppKeyDebug =
    localProperties.getProperty("debug.kakao.native.app.key").orEmpty()
val kakaoNativeAppKeyRelease =
    localProperties.getProperty("release.kakao.native.app.key").orEmpty()

// 릴리스 서명 설정
val releaseStoreFilePath =
    localProperties.getProperty("release.store.file").orEmpty()
val releaseStorePassword =
    localProperties.getProperty("release.store.password").orEmpty()
val releaseKeyAlias =
    localProperties.getProperty("release.key.alias").orEmpty()
val releaseKeyPassword =
    localProperties.getProperty("release.key.password").orEmpty()

// 릴리스 변형을 빌드할 때 카카오 키가 없으면(local.properties/CI 시크릿 누락) 빌드를 실패시킨다.
// 키 없이 릴리스 APK가 생성되면 배포 후 카카오 SDK 초기화·로그인이 실패하기 때문이다.
gradle.taskGraph.whenReady {
    val buildsRelease = allTasks.any { it.project == project && it.name.contains("Release") }
    if (buildsRelease && kakaoNativeAppKeyRelease.isBlank()) {
        throw GradleException(
            "릴리스 카카오 네이티브 앱 키가 없습니다. local.properties(또는 CI 시크릿)에 " +
                "'release.kakao.native.app.key'를 설정하세요.",
        )
    }
    if (buildsRelease) {
        val missingSigningProperties =
            listOf(
                "release.store.file" to releaseStoreFilePath,
                "release.store.password" to releaseStorePassword,
                "release.key.alias" to releaseKeyAlias,
                "release.key.password" to releaseKeyPassword,
            ).filter { (_, value) -> value.isBlank() }

        if (missingSigningProperties.isNotEmpty()) {
            throw GradleException(
                "릴리스 서명 정보가 없습니다: " +
                    missingSigningProperties.joinToString { (key) -> key },
            )
        }

        val releaseStoreFile = rootProject.file(releaseStoreFilePath)
        if (!releaseStoreFile.isFile) {
            throw GradleException("릴리스 keystore 파일을 찾을 수 없습니다: $releaseStoreFilePath")
        }
    }
}

android {
    namespace = "com.happyhouse.challa"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.happyhouse.challa"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = releaseStoreFilePath.takeIf(String::isNotBlank)?.let(rootProject::file)
            storePassword = releaseStorePassword
            keyAlias = releaseKeyAlias
            keyPassword = releaseKeyPassword
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true

            buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoNativeAppKeyDebug\"")
            manifestPlaceholders["kakaoNativeAppKey"] = kakaoNativeAppKeyDebug
        }

        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoNativeAppKeyRelease\"")
            manifestPlaceholders["kakaoNativeAppKey"] = kakaoNativeAppKeyRelease
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":presentation"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.timber)
    implementation(libs.logger)
    implementation(libs.kakao.user)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.androidx.compose)
    ksp(libs.hilt.android.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.bundles.androidx.test)
    debugImplementation(libs.bundles.androidx.compose.debug)
}
