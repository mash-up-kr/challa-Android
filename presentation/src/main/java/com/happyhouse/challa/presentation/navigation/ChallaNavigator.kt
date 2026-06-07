package com.happyhouse.challa.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

@Composable
fun rememberChallaNavigator(startRoute: ChallaRoute): ChallaNavigator {
    val backStack = rememberNavBackStack(startRoute)

    return remember(backStack) {
        ChallaNavigator(backStack)
    }
}

/**
 * Navigation3의 back stack을 화면 이동 API로 감싼 navigator.
 */
@Stable
class ChallaNavigator internal constructor(
    internal val backStack: NavBackStack<NavKey>,
) {
    val currentRoute: ChallaRoute
        get() = backStack.last() as ChallaRoute

    fun navigate(route: ChallaRoute) {
        backStack.add(route)
    }

    /**
     * 이동하려는 route가 현재 stack 최상단 route와 다를 때만 이동한다.
     */
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
