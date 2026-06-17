package com.happyhouse.challa.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface ChallaRoute : NavKey {
    @Serializable
    data object Sample : ChallaRoute

    @Serializable
    data object Onboarding : ChallaRoute

    @Serializable
    data object Login : ChallaRoute

    @Serializable
    data object Home : ChallaRoute

    @Serializable
    data object CreateRoom : ChallaRoute
}
