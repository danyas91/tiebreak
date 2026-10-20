package com.djasik.tiebreak.feature.match.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.djasik.tiebreak.domain.model.Strategy
import com.djasik.tiebreak.feature.util.getDisplayName
import com.djasik.tiebreak.feature.util.getIcon

@Composable
fun StrategySelector(
    selectedStrategy: Strategy,
    onStrategySelected: (Strategy) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Strategy.entries.forEach { strategy ->
            StrategyButton(
                strategy = strategy,
                selected = selectedStrategy == strategy,
                enabled = enabled,
                onClick = {
                    onStrategySelected(strategy)
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StrategyButton(
    strategy: Strategy,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) {
        Color(0xFFFF8A25)
    } else {
        Color(0xFF6840A5)
    }

    Card(
        modifier = modifier
            .clickable(
                enabled = enabled,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = borderColor,
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF321568),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 16.dp,
                    horizontal = 8.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = strategy.getIcon(),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = if (selected) {
                    Color(0xFFFF8A25)
                } else {
                    Color(0xFFC2AEDF)
                },
            )

            Text(
                text = strategy.getDisplayName(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
            )
        }
    }
}