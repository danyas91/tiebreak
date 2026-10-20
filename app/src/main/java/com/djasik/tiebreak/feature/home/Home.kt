package com.djasik.tiebreak.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.djasik.tiebreak.core.navigation.NavigationNode
import com.djasik.tiebreak.domain.model.TeamId
import com.djasik.tiebreak.feature.match.Match
import kotlinx.coroutines.flow.SharedFlow

object Home : NavigationNode() {

    @Composable
    override fun Screen(
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
    ) {
        val viewModel: HomeViewModel = hiltViewModel()

        HomeScreen(
            uiState = viewModel.uiState.collectAsStateWithLifecycle().value,
            onUserEvent = viewModel::onUserEvent,
        )

        HandleUiEvents(
            navController = navController,
            uiEvent = viewModel.uiEvent,
        )
    }

    @Composable
    private fun HandleUiEvents(
        navController: NavHostController,
        uiEvent: SharedFlow<UiEvent>,
    ) {
        LaunchedEffect(Unit) {
            uiEvent.collect { event ->
                when (event) {
                    is UiEvent.NavigateToMatch -> {
                        navController.navigate(
                            Match.createRoute(
                                teamId = event.teamId
                            )
                        )
                    }
                }
            }
        }
    }

    data class UiState(
        val selectedTeamId: TeamId? = null,
    )

    sealed interface UserEvent {
        data class SelectTeam(
            val teamId: TeamId,
        ) : UserEvent

        data object StartMatch : UserEvent
    }

    sealed interface UiEvent {
        data class NavigateToMatch(
            val teamId: TeamId,
        ) : UiEvent
    }
}