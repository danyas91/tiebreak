package com.djasik.tiebreak.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.djasik.tiebreak.R
import com.djasik.tiebreak.core.view.AppBackground
import com.djasik.tiebreak.domain.model.Team
import com.djasik.tiebreak.domain.model.TeamId
import com.djasik.tiebreak.domain.model.Teams
import com.djasik.tiebreak.feature.util.getLogoRes

@Composable
fun HomeScreen(
    uiState: Home.UiState,
    onUserEvent: (Home.UserEvent) -> Unit,
) {
    AppBackground() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.home_screen_header),
                color = Color(0xFFBBA7E8),
            )

            Text(
                text = stringResource(R.string.home_screen_title),
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
            )

            TeamSelection(
                selectedTeamId = uiState.selectedTeamId,
                onTeamSelected = { teamId ->
                    onUserEvent(Home.UserEvent.SelectTeam(teamId))
                },
                modifier = Modifier.padding(top = 24.dp),
            )

            Spacer(
                modifier = Modifier.height(24.dp),
            )

            StartMatchButton(
                enabled = uiState.selectedTeamId != null,
                onClick = {
                    onUserEvent(Home.UserEvent.StartMatch)
                },
            )
        }
    }
}

@Composable
fun TeamSelection(
    modifier: Modifier = Modifier,
    selectedTeamId: TeamId?,
    onTeamSelected: (TeamId) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TeamSelectionCard(
            team = Teams.Dragons,
            isSelected = selectedTeamId == TeamId.DRAGONS,
            onClick = {
                onTeamSelected(TeamId.DRAGONS)
            },
            modifier = Modifier.weight(1f),
        )

        TeamSelectionCard(
            team = Teams.Tigers,
            isSelected = selectedTeamId == TeamId.TIGERS,
            onClick = {
                onTeamSelected(TeamId.TIGERS)
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TeamSelectionCard(
    modifier: Modifier = Modifier,
    team: Team,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) {
        Color(0xFFFF8A25)
    } else {
        Color(0xFF6840A5)
    }

    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF321568),
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor,
        ),
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column {
                    Image(
                        painter = painterResource(
                            id = team.id.getLogoRes(),
                        ),
                        contentDescription = team.id.displayName,
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.CenterHorizontally),
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp),
                    )

                    Text(
                        text = team.id.displayName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp),
                    )

                    TeamStatistics(
                        label = stringResource(R.string.home_screen_attack_label),
                        value = team.stats.attack,
                        isSelected = isSelected,
                    )

                    TeamStatistics(
                        label = stringResource(R.string.home_screen_defense_label),
                        value = team.stats.defense,
                        isSelected = isSelected,
                    )

                    TeamStatistics(
                        label = stringResource(R.string.home_screen_stamina_label),
                        value = team.stats.stamina,
                        isSelected = isSelected,
                    )
                }
            }

            SelectionIndicator(
                isSelected = isSelected,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }
}

@Composable
private fun TeamStatistics(
    label: String,
    value: Int,
    isSelected: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = if (isSelected) {
                Color(0xFFFFA347)
            } else {
                Color(0xFFC2AEDF)
            },
        )

        Text(
            text = value.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SelectionIndicator(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .border(
                width = 2.dp,
                color = if (isSelected) {
                    Color(0xFFFF8A25)
                } else {
                    Color(0xFF8E6BC4)
                },
                shape = CircleShape,
            )
            .background(
                color = if (isSelected) {
                    Color(0xFFFF8A25)
                } else {
                    Color.Transparent
                },
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF24104F),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
fun StartMatchButton(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFF8A25),
            contentColor = Color(0xFF24104F),
            disabledContainerColor = Color(0xFF6B4B83),
            disabledContentColor = Color(0xFF9B86B2),
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp,
        ),
        contentPadding = PaddingValues(
            horizontal = 24.dp,
        ),
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
        )

        Spacer(
            modifier = Modifier.width(12.dp),
        )

        Text(
            text = stringResource(R.string.common_start_match),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}