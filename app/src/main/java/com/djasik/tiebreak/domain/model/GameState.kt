package com.djasik.tiebreak.domain.model

data class GameState(
    val playerTeam: Team = Teams.Dragons,
    val opponentTeam: Team = Teams.Tigers,
    val playerScore: Int = 0,
    val opponentScore: Int = 0,
    val playerFatigue: Float = 0f,
    val opponentFatigue: Float = 0f,
    val strategy: Strategy = Strategy.BALANCED,
    val servingTeam: TeamSide = TeamSide.PLAYER,
    val phase: GamePhase = GamePhase.NotStarted,
    val events: List<MatchEvent> = emptyList(),
) {
    companion object {
        const val MAX_EVENT_HISTORY = 6
    }
}
