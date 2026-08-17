// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.dagger.hilt) apply false
    alias(libs.plugins.google.services) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configurations.configureEach {
        if (name.contains("ksp", ignoreCase = true) ||
            name.contains("hiltAnnotationProcessor", ignoreCase = true)
        ) {
            resolutionStrategy.force(
                "org.jetbrains.kotlin:kotlin-metadata-jvm:${libs.versions.kotlin.get()}",
            )
        }
    }
}
