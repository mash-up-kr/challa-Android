import com.android.build.api.variant.BuildConfigField

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

// 카카오 SDK 설정
val kakaoNativeAppKeyProvider =
    providers
        .gradleProperty("challaKakaoNativeAppKey")
        .orElse("")
        .map { value ->
            value.ifBlank {
                throw GradleException(
                    "필수 Gradle Property 'challaKakaoNativeAppKey'가 없거나 비어 있습니다.",
                )
            }
        }

// 배포용 Debug 키를 지정하지 않은 로컬 빌드는 기존 Android Debug 키를 사용합니다.
val debugSigningProperties =
    listOf(
        "challaDebugStoreFile",
        "challaDebugStorePassword",
        "challaDebugKeyAlias",
        "challaDebugKeyPassword",
    ).associateWith { providers.gradleProperty(it).orNull }
val usesCustomDebugSigning = debugSigningProperties.values.any { it != null }
if (usesCustomDebugSigning) {
    val missingProperties = debugSigningProperties.filterValues { it.isNullOrBlank() }.keys
    if (missingProperties.isNotEmpty()) {
        throw GradleException("Debug 서명 설정이 없거나 비어 있습니다: ${missingProperties.joinToString()}")
    }
    val debugStoreFile = rootProject.file(debugSigningProperties.getValue("challaDebugStoreFile")!!)
    if (!debugStoreFile.isFile) {
        throw GradleException("Debug keystore 파일을 찾을 수 없습니다: $debugStoreFile")
    }
}

// 릴리즈 서명 설정
val releaseStoreFilePath =
    providers.gradleProperty("challaReleaseStoreFile").orNull.orEmpty()
val releaseStorePassword =
    providers.gradleProperty("challaReleaseStorePassword").orNull.orEmpty()
val releaseKeyAlias =
    providers.gradleProperty("challaReleaseKeyAlias").orNull.orEmpty()
val releaseKeyPassword =
    providers.gradleProperty("challaReleaseKeyPassword").orNull.orEmpty()

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
        getByName("debug") {
            if (usesCustomDebugSigning) {
                storeFile =
                    rootProject.file(debugSigningProperties.getValue("challaDebugStoreFile")!!)
                storePassword = debugSigningProperties.getValue("challaDebugStorePassword")
                keyAlias = debugSigningProperties.getValue("challaDebugKeyAlias")
                keyPassword = debugSigningProperties.getValue("challaDebugKeyPassword")
            }
        }
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
        }

        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
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

androidComponents {
    onVariants { variant ->
        val buildConfigFields = variant.buildConfigFields ?: return@onVariants

        buildConfigFields.put(
            "KAKAO_NATIVE_APP_KEY",
            kakaoNativeAppKeyProvider.map { value ->
                BuildConfigField("String", "\"$value\"", null)
            },
        )
        variant.manifestPlaceholders.put("kakaoNativeAppKey", kakaoNativeAppKeyProvider)
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
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
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
