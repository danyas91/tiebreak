package com.djasik.tiebreak.data.engine

import com.djasik.tiebreak.domain.engine.FatigueCalculator
import com.djasik.tiebreak.domain.model.Strategy
import javax.inject.Inject

class FatigueCalculatorImpl @Inject constructor() : FatigueCalculator {
    override fun calculatePlayerFatigue(
        current: Float,
        strategy: Strategy,
        stamina: Int,
    ): Float {
        val baseIncrease = when (strategy) {
            Strategy.AGGRESSIVE -> 0.8f
            Strategy.BALANCED -> 0.5f
            Strategy.DEFENSIVE -> 0.3f
        }

        return calculateFatigue(
            current = current,
            baseIncrease = baseIncrease,
            stamina = stamina,
        )
    }

    override fun calculateOpponentFatigue(
        current: Float,
        stamina: Int,
    ): Float =
        calculateFatigue(
            current = current,
            baseIncrease = OPPONENT_BASE_FATIGUE_INCREASE,
            stamina = stamina,
        )

    private fun calculateFatigue(
        current: Float,
        baseIncrease: Float,
        stamina: Int,
    ): Float {
        val staminaFactor = 1f - stamina / MAX_TEAM_STAT * STAMINA_FATIGUE_REDUCTION

        return (current + baseIncrease * staminaFactor).coerceAtMost(MAX_FATIGUE)
    }

    private companion object {
        const val MAX_FATIGUE = 10f
        const val MAX_TEAM_STAT = 10f
        const val STAMINA_FATIGUE_REDUCTION = 0.5f
        const val OPPONENT_BASE_FATIGUE_INCREASE = 0.5f
    }
}