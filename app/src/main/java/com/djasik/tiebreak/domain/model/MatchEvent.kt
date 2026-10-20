package com.djasik.tiebreak.domain.model

sealed interface MatchEvent {
    val score: MatchScore

    data class PointScored(val team: Team, override val score: MatchScore) : MatchEvent
}