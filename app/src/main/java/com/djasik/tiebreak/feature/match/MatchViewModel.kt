
package com.djasik.tiebreak.feature.match

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djasik.tiebreak.domain.engine.GameEngine
import com.djasik.tiebreak.domain.model.GamePhase
import com.djasik.tiebreak.domain.model.GameState
import com.djasik.tiebreak.domain.model.Strategy
import com.djasik.tiebreak.domain.model.Team
import com.djasik.tiebreak.domain.model.TeamId
import com.djasik.tiebreak.domain.model.opponent
import com.djasik.tiebreak.domain.model.toTeam
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MatchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gameEngine: GameEngine,
) : ViewModel() {

    private val playerTeamId: TeamId =
        TeamId.valueOf(checkNotNull(savedStateHandle.get<String>(Match.TEAM_ARG)))

    private val playerTeam = playerTeamId.toTeam()

    private val opponentTeam = playerTeamId.opponent().toTeam()

    val uiState: StateFlow<Match.UiState> =
        gameEngine.gameState
            .map { state ->
                state.toUiState(
                    selectedPlayerTeam = playerTeam,
                    selectedOpponentTeam = opponentTeam,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = Match.UiState(
                    playerTeam = playerTeam,
                    opponentTeam = opponentTeam,
                ),
            )

    private val _uiEvent = MutableSharedFlow<Match.UiEvent>()

    val uiEvent = _uiEvent.asSharedFlow()

    init {
        Timber.d(
            "MatchViewModel created for %s vs %s",
            playerTeam.id,
            opponentTeam.id,
        )
    }

    fun onUserEvent(event: Match.UserEvent) {
        Timber.d("User event: %s", event)

        viewModelScope.launch {
            when (event) {
                is Match.UserEvent.SelectStrategy -> selectStrategy(strategy = event.strategy)

                Match.UserEvent.StartGame -> startMatch()
                Match.UserEvent.StopGame -> stopMatch()
                Match.UserEvent.PlayAgain -> gameEngine.restart()
                Match.UserEvent.NavigateBack -> onNavigateBack()
                Match.UserEvent.ConfirmStopGame -> confirmStopGame()
                Match.UserEvent.NavigateToHome -> navigateToHome()
                Match.UserEvent.ResumeGame -> gameEngine.resume()
            }
        }
    }

    override fun onCleared() {
        Timber.d("MatchViewModel cleared")
        super.onCleared()
    }

    private suspend fun startMatch() {
        gameEngine.start(
            playerTeam = playerTeam,
            opponentTeam = opponentTeam,
        )
    }

    private suspend fun stopMatch() {
        gameEngine.stop()
    }

    private suspend fun selectStrategy(
        strategy: Strategy,
    ) {
        val phase = gameEngine.gameState.value.phase

        Timber.d(
            "Strategy selected: %s in phase: %s",
            strategy,
            phase,
        )

        when (phase) {
            is GamePhase.AwaitingDecision -> gameEngine.submitStrategyDecision(strategy = strategy)
            is GamePhase.Finished, GamePhase.NotStarted -> gameEngine.setInitialStrategy(strategy = strategy)

            GamePhase.Playing -> gameEngine.changeStrategy(strategy = strategy)
            GamePhase.Error -> Unit
        }
    }

    private suspend fun onNavigateBack() {
        when (gameEngine.gameState.value.phase) {
            GamePhase.Playing,
            is GamePhase.AwaitingDecision -> {
                gameEngine.pause()
                emitUiEvent(Match.UiEvent.ShowStopGameDialog)
            }

            else -> navigateToHome()
        }
    }

    private suspend fun confirmStopGame() {
        Timber.i("User confirmed leaving the match")
        gameEngine.stop()
        navigateToHome()
    }

    private suspend fun navigateToHome() {
        emitUiEvent(Match.UiEvent.NavigateToHome)
    }

    private suspend fun emitUiEvent(event: Match.UiEvent) {
        _uiEvent.emit(event)
    }

}

internal fun GameState.toUiState(
    selectedPlayerTeam: Team,
    selectedOpponentTeam: Team,
): Match.UiState {
    val displayedPlayerTeam = if (phase == GamePhase.NotStarted) {
        selectedPlayerTeam
    } else {
        playerTeam
    }

    val displayedOpponentTeam = if (phase == GamePhase.NotStarted) {
        selectedOpponentTeam
    } else {
        opponentTeam
    }

    return Match.UiState(
        playerTeam = displayedPlayerTeam,
        opponentTeam = displayedOpponentTeam,
        playerScore = playerScore,
        opponentScore = opponentScore,
        playerFatigue = playerFatigue,
        opponentFatigue = opponentFatigue,
        strategy = strategy,
        gamePhase = phase,
        events = events,
        servingTeam = servingTeam,
    )
}
