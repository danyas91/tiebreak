package com.djasik.tiebreak.feature.match.view

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.djasik.tiebreak.R
import com.djasik.tiebreak.domain.model.Team
import com.djasik.tiebreak.feature.util.getLogoRes

@Composable
fun MatchFinishedDialog(
    winner: Team,
    onPlayAgain: () -> Unit,
    onBackToHome: () -> Unit,
) {
    var visible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
        ),
        label = "winner_scale",
    )

    Dialog(
        onDismissRequest = {},
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF24104F),
            ),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.match_finished_dialog_header),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                )

                Image(
                    painter = painterResource(
                        id = winner.id.getLogoRes(),
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                )

                Text(
                    text = stringResource(
                        R.string.match_finished_dialog_winner_label,
                        winner.id.displayName
                    ),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )

                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF8A25),
                    ),
                ) {
                    Text(stringResource(R.string.match_finished_dialog_play_again_button_label))
                }

                OutlinedButton(
                    onClick = onBackToHome,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.match_finished_dialog_back_button_label))
                }
            }
        }
    }
}