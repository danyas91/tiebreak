package com.djasik.tiebreak.feature.match.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SportsVolleyball
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.djasik.tiebreak.R
import com.djasik.tiebreak.domain.model.GameState
import com.djasik.tiebreak.domain.model.MatchEvent

@Composable
fun MatchEvents(
    events: List<MatchEvent>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.match_screen_match_events_header),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        if (events.isEmpty()) {
            Text(
                text = stringResource(R.string.match_screen_no_events_label),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC2AEDF),
            )
        } else {
            events
                .takeLast(GameState.MAX_EVENT_HISTORY)
                .reversed()
                .forEach { event ->
                    MatchEventItem(
                        event = event,
                    )
                }
        }
    }
}

@Composable
private fun MatchEventItem(
    event: MatchEvent,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6840A5),
        ),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.SportsVolleyball,
                contentDescription = null,
                tint = Color(0xFFFF8A25),
            )

            Spacer(
                modifier = Modifier.width(12.dp),
            )

            when (event) {
                is MatchEvent.PointScored -> {
                    Text(
                        text = stringResource(
                            R.string.match_screen_point_for_label,
                            event.team.id.displayName
                        ),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                    )

                    Text(
                        text = "${event.score.player} : ${event.score.opponent}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}