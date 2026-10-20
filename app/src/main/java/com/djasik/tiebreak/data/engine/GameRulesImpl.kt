package com.djasik.tiebreak.data.engine

import com.djasik.tiebreak.domain.engine.GameRules
import com.djasik.tiebreak.domain.model.DecisionReason
import com.djasik.tiebreak.domain.model.GameState
import com.djasik.tiebreak.domain.model.MatchEvent
import javax.inject.Inject
import kotlin.math.abs

class GameRulesImpl @Inject constructor() : GameRules {
    override fun isFinished(state: GameState): Boolean {
        val maxScore = maxOf(state.playerScore, state.opponentScore)

        val scoreDifference = abs(state.playerScore - state.opponentScore)

        return maxScore >= TIE_BREAK_TARGET && scoreDifference >= MIN_WIN_DIFFERENCE
    }

    override fun getDecisionReason(state: GameState): DecisionReason? {
        if (isOpponentMatchPoint(state)) {
            return DecisionReason.MatchPoint
        }

        if (hasOpponentScoredThreeInRow(state)) {
            return DecisionReason.OpponentStreak(points = OPPONENT_STREAK)
        }

        return null
    }

    private fun isOpponentMatchPoint(state: GameState) =
        state.opponentScore >= 14 && state.opponentScore > state.playerScore

    private fun hasOpponentScoredThreeInRow(state: GameState): Boolean {
        val events = state.events
            .takeLast(GameState.MAX_EVENT_HISTORY)
            .filterIsInstance<MatchEvent.PointScored>()
            .takeLast(OPPONENT_STREAK_WINDOW)
        val streak = events.takeLast(OPPONENT_STREAK)
        val previous = events.getOrNull(events.lastIndex - OPPONENT_STREAK)

        return streak.size == OPPONENT_STREAK && streak.all { it.team == state.opponentTeam } && previous?.team != state.opponentTeam
    }

    private companion object {
        const val TIE_BREAK_TARGET = 15
        const val MIN_WIN_DIFFERENCE = 2
        const val OPPONENT_STREAK = 3
        const val OPPONENT_STREAK_WINDOW = 4
    }
}
