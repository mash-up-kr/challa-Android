package com.happyhouse.challa.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.happyhouse.challa.presentation.navigation.ChallaNavigator
import com.happyhouse.challa.presentation.navigation.ChallaRoute
import com.happyhouse.challa.presentation.navigation.rememberChallaNavigator

@Composable
fun rememberChallaAppState(
    startRoute: ChallaRoute,
    navigator: ChallaNavigator = rememberChallaNavigator(startRoute),
): ChallaAppState =
    remember(navigator) {
        ChallaAppState(navigator)
    }

@Stable
class ChallaAppState(
    val navigator: ChallaNavigator,
)
