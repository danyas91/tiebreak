package com.djasik.tiebreak.feature.match.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.djasik.tiebreak.R
import com.djasik.tiebreak.domain.model.Team
import kotlin.math.roundToInt

@Composable
fun FatigueBar(
    modifier: Modifier = Modifier,
    playerTeam: Team,
    opponentTeam: Team,
    playerFatigue: Float,
    opponentFatigue: Float,
) {
    val playerProgress = (playerFatigue / 10f).coerceIn(0f, 1f)
    val opponentProgress = (opponentFatigue / 10f).coerceIn(0f, 1f)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.match_screen_fatigue_bar_header),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            FatigueLabel(
                teamName = playerTeam.id.displayName,
                progress = playerProgress,
            )

            FatigueLabel(
                teamName = opponentTeam.id.displayName,
                progress = opponentProgress,
                alignment = Alignment.End,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FatigueProgress(
                progress = playerProgress,
                reversed = true,
                modifier = Modifier.weight(1f),
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .width(2.dp)
                    .height(24.dp)
                    .background(Color.White),
            )

            FatigueProgress(
                progress = opponentProgress,
                reversed = false,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FatigueProgress(
    modifier: Modifier = Modifier,
    progress: Float,
    reversed: Boolean,
) {
    val segmentCount = 10
    val activeSegments = (progress * segmentCount)
        .roundToInt()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(segmentCount) { index ->
            val logicalIndex = if (reversed) {
                segmentCount - 1 - index
            } else {
                index
            }

            val active = logicalIndex < activeSegments

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (active) {
                            fatigueColor(
                                progress = progress,
                            )
                        } else {
                            Color(0xFF6840A5)
                        }
                    ),
            )
        }
    }
}

@Composable
private fun FatigueLabel(
    teamName: String,
    progress: Float,
    alignment: Alignment.Horizontal = Alignment.Start,
) {
    Column(
        horizontalAlignment = alignment,
    ) {
        Text(
            text = teamName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White,
        )

        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = fatigueColor(progress),
        )
    }
}

private fun fatigueColor(
    progress: Float,
): Color =
    when {
        progress < 0.4f -> Color(0xFF4CAF50)
        progress < 0.6f -> Color(0xFFFFC107)
        progress < 0.8f -> Color(0xFFFF8A25)
        else -> Color(0xFFE53935)
    }