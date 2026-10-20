package com.djasik.tiebreak.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

abstract class NavigationNode {
    protected val baseRoute = this::class.qualifiedName.orEmpty()

    open val route: String
        get() = baseRoute

    @Composable
    abstract fun Screen(navController: NavHostController, navBackStackEntry: NavBackStackEntry)
}