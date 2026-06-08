package com.happyhouse.challa.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface ChallaRoute : NavKey {
    @Serializable
    data object Sample : ChallaRoute

    @Serializable
    data class Camera(
        val roomId: Long,
    ) : ChallaRoute
}
