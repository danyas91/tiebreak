package com.djasik.tiebreak.domain.engine

import com.djasik.tiebreak.domain.model.Strategy

interface FatigueCalculator {
    fun calculatePlayerFatigue(
        current: Float,
        strategy: Strategy,
        stamina: Int,
    ): Float

    fun calculateOpponentFatigue(
        current: Float,
        stamina: Int,
    ): Float
}