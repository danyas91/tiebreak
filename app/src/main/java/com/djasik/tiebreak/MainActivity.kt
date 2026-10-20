package com.djasik.tiebreak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.djasik.tiebreak.feature.home.Home
import com.djasik.tiebreak.feature.match.Match
import com.djasik.tiebreak.ui.theme.TieBreakTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TieBreakTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Home.route,
                ) {
                    composable(
                        route = Home.route,
                    ) { navBackStackEntry ->
                        Home.Screen(
                            navController = navController,
                            navBackStackEntry = navBackStackEntry,
                        )
                    }

                    composable(
                        route = Match.route,
                        arguments = listOf(
                            navArgument(Match.TEAM_ARG) {
                                type = NavType.StringType
                            }
                        ),
                    ) { navBackStackEntry ->
                        Match.Screen(
                            navController = navController,
                            navBackStackEntry = navBackStackEntry,
                        )
                    }
                }
            }
        }
    }
}
