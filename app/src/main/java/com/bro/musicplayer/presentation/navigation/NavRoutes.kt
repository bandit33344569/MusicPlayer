package com.bro.musicplayer.presentation.navigation

sealed class NavRoutes(val route: String) {
    data object Home : NavRoutes("home")
    data object Deezer : NavRoutes("deezer")
    data object Player: NavRoutes("player")
}