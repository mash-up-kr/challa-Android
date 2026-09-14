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
val kakaoNativeAppKey =
    localProperties.getProperty("kakao.native.app.key").orEmpty()

// 릴리즈 서명 설정
val releaseStoreFilePath =
    localProperties.getProperty("release.store.file").orEmpty()
val releaseStorePassword =
    localProperties.getProperty("release.store.password").orEmpty()
val releaseKeyAlias =
    localProperties.getProperty("release.key.alias").orEmpty()
val releaseKeyPassword =
    localProperties.getProperty("release.key.password").orEmpty()

// app의 릴리즈 산출물을 생성하는 요청 태스크에서만 카카오 키와 서명 정보를 검증한다.
// ktlintCheck처럼 릴리즈 SourceSet만 검사하는 태스크는 검증 대상에서 제외한다.
gradle.taskGraph.whenReady {
    val releaseBuildTasks =
        setOf(
            "assemble",
            "build",
            "assembleRelease",
            "bundleRelease",
            "installRelease",
            "packageRelease",
            "publishReleaseBundle",
        )
    val buildsRelease =
        gradle.startParameter.taskNames.any { taskPath ->
            val taskName = taskPath.substringAfterLast(':')
            val targetProject =
                taskPath.removePrefix(":").substringBefore(':', missingDelimiterValue = "app")
            targetProject == "app" && taskName in releaseBuildTasks
        }
    if (buildsRelease && kakaoNativeAppKey.isBlank()) {
        throw GradleException(
            "릴리즈 빌드에 필요한 카카오 네이티브 앱 키가 없습니다. " +
                "local.properties에 'kakao.native.app.key'를 설정하세요. " +
                "CI에서는 'KAKAO_NATIVE_APP_KEY' Secret을 확인하세요.",
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
                "릴리즈 빌드에 필요한 서명 설정이 없습니다: " +
                    missingSigningProperties.joinToString { (key) -> key },
            )
        }

        val releaseStoreFile = rootProject.file(releaseStoreFilePath)
        if (!releaseStoreFile.isFile) {
            throw GradleException("release keystore 파일을 찾을 수 없습니다: $releaseStoreFilePath")
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

            buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoNativeAppKey\"")
            manifestPlaceholders["kakaoNativeAppKey"] = kakaoNativeAppKey
        }

        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoNativeAppKey\"")
            manifestPlaceholders["kakaoNativeAppKey"] = kakaoNativeAppKey
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
