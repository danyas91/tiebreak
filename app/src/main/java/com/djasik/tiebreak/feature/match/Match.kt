package com.djasik.tiebreak.feature.match

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.djasik.tiebreak.core.navigation.NavigationNode
import com.djasik.tiebreak.core.view.StopGameDialog
import com.djasik.tiebreak.domain.model.GamePhase
import com.djasik.tiebreak.domain.model.MatchEvent
import com.djasik.tiebreak.domain.model.Strategy
import com.djasik.tiebreak.domain.model.Team
import com.djasik.tiebreak.domain.model.TeamId
import com.djasik.tiebreak.domain.model.TeamSide
import kotlinx.coroutines.flow.SharedFlow

object Match : NavigationNode() {

    const val TEAM_ARG = "team"

    override val route: String
        get() = "$baseRoute/{$TEAM_ARG}"

    fun createRoute(teamId: TeamId): String = "$baseRoute/${teamId.name}"

    @Composable
    override fun Screen(
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
    ) {
        val viewModel: MatchViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        var showStopGameDialog by rememberSaveable {
            mutableStateOf(false)
        }

        HandleUiEvents(
            navController = navController,
            uiEvent = viewModel.uiEvent,
            onShowStopGameDialog = {
                showStopGameDialog = true
            },
        )

        MatchScreen(
            uiState = uiState,
            onUserEvent = viewModel::onUserEvent,
        )

        if (showStopGameDialog) {
            StopGameDialog(
                onConfirm = {
                    viewModel.onUserEvent(
                        UserEvent.ConfirmStopGame
                    )
                },
                onDismiss = {
                    viewModel.onUserEvent(
                        UserEvent.ResumeGame
                    )
                    showStopGameDialog = false
                },
            )
        }
    }

    @Composable
    private fun HandleUiEvents(
        navController: NavHostController,
        uiEvent: SharedFlow<UiEvent>,
        onShowStopGameDialog: () -> Unit,
    ) {
        LaunchedEffect(Unit) {
            uiEvent.collect { uiEvent ->
                when (uiEvent) {
                    UiEvent.NavigateToHome -> {
                        navController.popBackStack()
                    }

                    UiEvent.ShowStopGameDialog -> {
                        onShowStopGameDialog()
                    }
                }
            }
        }
    }

    data class UiState(
        val playerTeam: Team? = null,
        val opponentTeam: Team? = null,
        val playerScore: Int = 0,
        val opponentScore: Int = 0,
        val playerFatigue: Float = 0f,
        val opponentFatigue: Float = 0f,
        val strategy: Strategy = Strategy.BALANCED,
        val isDecisionRequired: Boolean = false,
        val gamePhase: GamePhase = GamePhase.NotStarted,
        val servingTeam: TeamSide? = null,
        val events: List<MatchEvent> = emptyList(),
    )

    sealed interface UserEvent {
        data class SelectStrategy(val strategy: Strategy) : UserEvent
        data object StartGame : UserEvent
        data object StopGame : UserEvent
        data object PlayAgain : UserEvent
        data object NavigateBack : UserEvent
        data object ConfirmStopGame : UserEvent
        data object NavigateToHome : UserEvent
        data object ResumeGame : UserEvent
    }

    sealed interface UiEvent {
        data object NavigateToHome : UiEvent
        data object ShowStopGameDialog : UiEvent
    }

}