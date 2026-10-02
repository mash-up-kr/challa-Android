import com.android.build.api.variant.BuildConfigField

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
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
