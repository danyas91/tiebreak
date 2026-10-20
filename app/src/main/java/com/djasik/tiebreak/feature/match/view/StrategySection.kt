package com.djasik.tiebreak.feature.match.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.djasik.tiebreak.R
import com.djasik.tiebreak.domain.model.Strategy

@Composable
fun InitialStrategySection(
    selectedStrategy: Strategy,
    onStrategySelected: (Strategy) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.match_screen_initial_strategy_header),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        Text(
            text = stringResource(R.string.match_screen_initial_strategy_label),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFC2AEDF),
        )

        StrategySelector(
            selectedStrategy = selectedStrategy,
            onStrategySelected = onStrategySelected,
        )
    }
}

@Composable
fun CurrentStrategySection(
    selectedStrategy: Strategy,
    onStrategySelected: (Strategy) -> Unit,
    enabled: Boolean = true,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.match_screen_current_strategy_header),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        StrategySelector(
            selectedStrategy = selectedStrategy,
            onStrategySelected = onStrategySelected,
            enabled = enabled,
        )
    }
}