package com.happyhouse.challa.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.happyhouse.challa.presentation.navigation.ChallaNavigator
import com.happyhouse.challa.presentation.navigation.ChallaRoute
import com.happyhouse.challa.presentation.navigation.rememberChallaNavigator

private val StartRoute = ChallaRoute.Sample

@Composable
fun rememberChallaAppState(navigator: ChallaNavigator = rememberChallaNavigator(StartRoute)): ChallaAppState =
    remember(navigator) {
        ChallaAppState(navigator)
    }

@Stable
class ChallaAppState(
    val navigator: ChallaNavigator,
)
