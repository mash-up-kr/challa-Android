import com.android.build.api.variant.BuildConfigField
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)
}

val localProperties =
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use(::load)
        }
    }

fun localProperty(key: String) =
    providers.provider {
        localProperties.getProperty(key)
            ?: error("Missing local.properties key: $key")
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
        val baseUrlKey = "$buildType.base.url"

        buildConfigFields.put(
            "BASE_URL",
            localProperty(baseUrlKey).map { value ->
                BuildConfigField("String", "\"$value\"", null)
            },
        )
    }
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.network)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
}
