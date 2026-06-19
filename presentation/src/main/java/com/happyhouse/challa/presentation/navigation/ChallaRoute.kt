package com.happyhouse.challa.presentation.navigation

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
sealed interface ChallaRoute : NavKey {
    @Serializable
    data object Sample : ChallaRoute

    @Serializable
    data class Camera(
        val roomId: Long,
    ) : ChallaRoute

    @Serializable
    data object Onboarding : ChallaRoute

    @Serializable
    data object Login : ChallaRoute
}
