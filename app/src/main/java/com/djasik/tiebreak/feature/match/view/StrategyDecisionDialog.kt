package com.djasik.tiebreak.feature.match.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.djasik.tiebreak.R
import com.djasik.tiebreak.domain.model.DecisionReason
import com.djasik.tiebreak.domain.model.Strategy
import com.djasik.tiebreak.feature.util.getDescription

@Composable
fun StrategyDecisionDialog(
    reason: DecisionReason,
    selectedStrategy: Strategy,
    onStrategySelected: (Strategy) -> Unit,
) {
    Dialog(
        onDismissRequest = {

        },
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF321568),
            ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFFFF8A25),
                )

                Text(
                    text = stringResource(R.string.strategy_decision_dialog_header),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )

                Text(
                    text = reason.getDescription(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFC2AEDF),
                    textAlign = TextAlign.Center,
                )

                StrategySelector(
                    selectedStrategy = selectedStrategy,
                    onStrategySelected = onStrategySelected,
                )
            }
        }
    }
}