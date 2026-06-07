package com.happyhouse.challa.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

@Composable
fun rememberNavigator(startRoute: ChallaRoute): Navigator {
    val backStack = rememberNavBackStack(startRoute)

    return remember(backStack) {
        Navigator(backStack)
    }
}

@Stable
class Navigator internal constructor(
    internal val backStack: NavBackStack<NavKey>,
) {
    val currentRoute: ChallaRoute
        get() = backStack.last() as ChallaRoute

    fun navigate(route: ChallaRoute) {
        backStack.add(route)
    }

    fun navigateSingleTop(route: ChallaRoute) {
        if (currentRoute != route) {
            backStack.add(route)
        }
    }

    fun replace(route: ChallaRoute) {
        backStack.removeLastOrNull()
        backStack.add(route)
    }

    fun clearAndNavigate(route: ChallaRoute) {
        backStack.clear()
        backStack.add(route)
    }

    fun goBack(): Boolean {
        if (backStack.size <= 1) return false

        backStack.removeLastOrNull()
        return true
    }
}
