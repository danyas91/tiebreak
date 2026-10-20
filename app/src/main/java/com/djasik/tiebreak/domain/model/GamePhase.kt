package com.djasik.tiebreak.domain.model

sealed interface GamePhase {
    data object NotStarted : GamePhase
    data object Playing : GamePhase
    data class AwaitingDecision(val reason: DecisionReason) : GamePhase
    data class Finished(val winner: Team) : GamePhase
    data object Error : GamePhase
}