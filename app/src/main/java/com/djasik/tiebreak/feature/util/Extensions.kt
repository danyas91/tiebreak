package com.djasik.tiebreak.feature.util

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.ui.graphics.vector.ImageVector
import com.djasik.tiebreak.R
import com.djasik.tiebreak.domain.model.DecisionReason
import com.djasik.tiebreak.domain.model.Strategy
import com.djasik.tiebreak.domain.model.TeamId

fun DecisionReason.getDescription(): String =
    when (this) {
        DecisionReason.UserRequested -> "Select your strategy."

        DecisionReason.MatchPoint -> "Opponent has match point. Choose your strategy."

        is DecisionReason.OpponentStreak -> "Opponent scored $points points in a row. Choose your strategy."
    }

fun Strategy.getDisplayName(): String =
    when (this) {
        Strategy.BALANCED -> "Balanced"
        Strategy.AGGRESSIVE -> "Aggressive"
        Strategy.DEFENSIVE -> "Defensive"
    }

fun Strategy.getIcon(): ImageVector =
    when (this) {
        Strategy.BALANCED -> Icons.Outlined.Balance
        Strategy.AGGRESSIVE -> Icons.Outlined.Bolt
        Strategy.DEFENSIVE -> Icons.Outlined.Shield
    }

@DrawableRes
fun TeamId.getLogoRes(): Int =
    when (this) {
        TeamId.DRAGONS -> R.drawable.logo_dragons
        TeamId.TIGERS -> R.drawable.logo_tigers
    }