package com.djasik.tiebreak.domain.model

sealed interface DecisionReason {
    data class OpponentStreak(val points: Int) : DecisionReason
    data object MatchPoint : DecisionReason
    data object UserRequested : DecisionReason
}