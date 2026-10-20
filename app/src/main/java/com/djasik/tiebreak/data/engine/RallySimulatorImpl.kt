package com.djasik.tiebreak.data.engine

import com.djasik.tiebreak.domain.engine.RallySimulator
import com.djasik.tiebreak.domain.model.GameState
import com.djasik.tiebreak.domain.model.MatchEvent
import com.djasik.tiebreak.domain.model.RallyResult
import com.djasik.tiebreak.domain.model.Strategy
import com.djasik.tiebreak.domain.model.TeamSide
import javax.inject.Inject
import kotlin.random.Random

class RallySimulatorImpl @Inject constructor() : RallySimulator {
    override fun simulate(state: GameState): RallyResult {
        val playerProbability = calculatePlayerWinProbability(state)
        val playerWon = Random.nextFloat() < playerProbability

        val winner = if (playerWon) {
            state.playerTeam
        } else {
            state.opponentTeam
        }

        return RallyResult(winner = winner)
    }

    private fun calculatePlayerWinProbability(state: GameState): Float {
        val playerPower = calculatePlayerPower(state)
        val opponentPower = calculateOpponentPower(state)

        val totalPower = playerPower + opponentPower

        if (totalPower <= 0f) {
            return DEFAULT_WIN_PROBABILITY
        }

        return (playerPower / totalPower)
            .coerceIn(
                minimumValue = MIN_WIN_PROBABILITY,
                maximumValue = MAX_WIN_PROBABILITY,
            )
    }

    private fun calculatePlayerPower(state: GameState): Float {
        val attack = state.playerTeam.stats.attack + getAttackModifier(state.strategy)

        val defense = state.playerTeam.stats.defense + getDefenseModifier(state.strategy)

        val servingModifier = if (state.servingTeam == TeamSide.PLAYER) {
            SERVING_BONUS
        } else {
            0f
        }

        val fatiguePenalty = calculateFatiguePenalty(fatigue = state.playerFatigue)

        return attack + defense + servingModifier - fatiguePenalty
    }

    private fun calculateOpponentPower(state: GameState): Float {
        val servingModifier = if (state.servingTeam == TeamSide.OPPONENT) {
            SERVING_BONUS
        } else {
            0f
        }

        val fatiguePenalty = calculateFatiguePenalty(fatigue = state.opponentFatigue)

        return state.opponentTeam.stats.attack + state.opponentTeam.stats.defense + servingModifier - fatiguePenalty
    }

    private fun getAttackModifier(strategy: Strategy): Float =
        when (strategy) {
            Strategy.AGGRESSIVE -> 2f
            Strategy.BALANCED -> 0f
            Strategy.DEFENSIVE -> -1f
        }

    private fun getDefenseModifier(strategy: Strategy): Float =
        when (strategy) {
            Strategy.AGGRESSIVE -> -0.5f
            Strategy.BALANCED -> 0f
            Strategy.DEFENSIVE -> 2f
        }

    private fun calculateFatiguePenalty(fatigue: Float): Float = fatigue * FATIGUE_POWER_PENALTY

    private companion object {
        const val FATIGUE_POWER_PENALTY = 0.3f
        const val SERVING_BONUS = 0.5f

        const val DEFAULT_WIN_PROBABILITY = 0.5f
        const val MIN_WIN_PROBABILITY = 0.2f
        const val MAX_WIN_PROBABILITY = 0.8f
    }
}