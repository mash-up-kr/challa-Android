import com.android.build.api.variant.BuildConfigField
import org.gradle.api.provider.Provider

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)
}

fun requiredGradleProperty(name: String): Provider<String> =
    providers
        .gradleProperty(name)
        .orElse("")
        .map { value ->
            value.ifBlank {
                throw GradleException("필수 Gradle Property '$name'가 없거나 비어 있습니다.")
            }
        }

android {
    namespace = "com.happyhouse.challa.data"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            proguardFiles("proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }
}

androidComponents {
    onVariants { variant ->
        val buildType = variant.buildType ?: return@onVariants
        val buildConfigFields = variant.buildConfigFields ?: return@onVariants
        val baseUrlPropertyName =
            when (buildType) {
                "debug" -> "challaDebugBaseUrl"
                "release" -> "challaReleaseBaseUrl"
                else -> return@onVariants
            }

        buildConfigFields.put(
            "BASE_URL",
            requiredGradleProperty(baseUrlPropertyName)
                .map { value ->
                    BuildConfigField("String", "\"$value\"", null)
                },
        )
    }
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logger)
    implementation(libs.bundles.network)
    implementation(libs.coil.singleton)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    debugImplementation(libs.bundles.flipper)
    releaseImplementation(libs.flipper.noop)
}
