package com.djasik.tiebreak.feature.match

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SportsVolleyball
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.djasik.tiebreak.R
import com.djasik.tiebreak.core.view.AppBackground
import com.djasik.tiebreak.domain.model.DecisionReason
import com.djasik.tiebreak.domain.model.GamePhase
import com.djasik.tiebreak.domain.model.GameState
import com.djasik.tiebreak.domain.model.MatchEvent
import com.djasik.tiebreak.domain.model.Strategy
import com.djasik.tiebreak.domain.model.Team
import com.djasik.tiebreak.domain.model.TeamSide
import com.djasik.tiebreak.feature.home.StartMatchButton
import com.djasik.tiebreak.feature.match.view.CurrentStrategySection
import com.djasik.tiebreak.feature.match.view.FatigueBar
import com.djasik.tiebreak.feature.match.view.InitialStrategySection
import com.djasik.tiebreak.feature.match.view.MatchEvents
import com.djasik.tiebreak.feature.match.view.MatchFinishedDialog
import com.djasik.tiebreak.feature.match.view.StrategyDecisionDialog
import com.djasik.tiebreak.feature.match.view.StrategySelector
import com.djasik.tiebreak.feature.util.getDescription
import com.djasik.tiebreak.feature.util.getDisplayName
import com.djasik.tiebreak.feature.util.getIcon
import com.djasik.tiebreak.feature.util.getLogoRes
import kotlin.math.roundToInt

@Composable
fun MatchScreen(
    modifier: Modifier = Modifier,
    uiState: Match.UiState,
    onUserEvent: (Match.UserEvent) -> Unit,
) {
    AppBackground(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 20.dp,
                    vertical = 16.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            MatchHeader()

            MatchScore(
                playerTeam = uiState.playerTeam!!,
                opponentTeam = uiState.opponentTeam!!,
                playerScore = uiState.playerScore,
                opponentScore = uiState.opponentScore,
                servingTeam = uiState.servingTeam
            )

            FatigueBar(
                playerTeam = uiState.playerTeam,
                opponentTeam = uiState.opponentTeam,
                playerFatigue = uiState.playerFatigue,
                opponentFatigue = uiState.opponentFatigue,
            )

            when (uiState.gamePhase) {
                GamePhase.NotStarted -> {
                    InitialStrategySection(
                        selectedStrategy = uiState.strategy,
                        onStrategySelected = { strategy ->
                            onUserEvent(
                                Match.UserEvent.SelectStrategy(
                                    strategy = strategy,
                                )
                            )
                        },
                    )

                    StartMatchButton(
                        enabled = true,
                        onClick = {
                            onUserEvent(
                                Match.UserEvent.StartGame
                            )
                        },
                    )
                }

                GamePhase.Playing -> {
                    CurrentStrategySection(
                        selectedStrategy = uiState.strategy,
                        onStrategySelected = { strategy ->
                            onUserEvent(
                                Match.UserEvent.SelectStrategy(
                                    strategy = strategy,
                                )
                            )
                        },
                    )

                    MatchEvents(
                        events = uiState.events,
                    )
                }

                is GamePhase.AwaitingDecision -> {
                    CurrentStrategySection(
                        selectedStrategy = uiState.strategy,
                        onStrategySelected = {},
                        enabled = false,
                    )

                    MatchEvents(
                        events = uiState.events,
                    )
                }

                is GamePhase.Finished -> {
                    MatchEvents(
                        events = uiState.events,
                    )
                }

                else -> Unit
            }
        }

        if (uiState.gamePhase is GamePhase.AwaitingDecision) {
            StrategyDecisionDialog(
                reason = uiState.gamePhase.reason,
                selectedStrategy = uiState.strategy,
                onStrategySelected = { strategy ->
                    onUserEvent(
                        Match.UserEvent.SelectStrategy(
                            strategy = strategy,
                        )
                    )
                },
            )
        }

        if (uiState.gamePhase is GamePhase.Finished) {
            val winner = uiState.gamePhase.winner

            MatchFinishedDialog(
                winner = winner,
                onPlayAgain = {
                    onUserEvent(
                        Match.UserEvent.PlayAgain
                    )
                },
                onBackToHome = {
                    onUserEvent(
                        Match.UserEvent.NavigateToHome
                    )
                },
            )
        }

        BackHandler {
            onUserEvent(
                Match.UserEvent.NavigateBack
            )
        }
    }
}

@Composable
private fun MatchHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.match_screen_header),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        Text(
            text = stringResource(R.string.match_screen_title),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFC2AEDF),
        )
    }
}

@Composable
private fun MatchScore(
    playerTeam: Team,
    opponentTeam: Team,
    playerScore: Int,
    opponentScore: Int,
    servingTeam: TeamSide?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TeamScore(
            team = playerTeam,
            score = playerScore,
            isServing = servingTeam == TeamSide.PLAYER,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = ":",
            color = Color(0xFFC2AEDF),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
        )

        TeamScore(
            team = opponentTeam,
            score = opponentScore,
            isServing = servingTeam == TeamSide.OPPONENT,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TeamScore(
    team: Team,
    score: Int,
    isServing: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(
                id = team.id.getLogoRes(),
            ),
            contentDescription = team.id.displayName,
            modifier = Modifier.size(88.dp),
        )

        Spacer(
            modifier = Modifier.height(6.dp),
        )

        if (isServing) {
            ServingIndicator()
        } else {
            Spacer(
                modifier = Modifier.height(28.dp),
            )
        }

        Text(
            text = score.toString(),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        Text(
            text = team.id.displayName,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
        )
    }
}

@Composable
private fun ServingIndicator() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFFF8A25))
            .padding(
                horizontal = 10.dp,
                vertical = 4.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.SportsVolleyball,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = Color(0xFF24104F),
        )

        Text(
            text = stringResource(R.string.match_screen_serving_label),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF24104F),
        )
    }
}
