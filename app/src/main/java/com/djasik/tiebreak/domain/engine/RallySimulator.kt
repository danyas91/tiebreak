package com.djasik.tiebreak.domain.engine

import com.djasik.tiebreak.domain.model.GameState
import com.djasik.tiebreak.domain.model.RallyResult

interface RallySimulator {
    fun simulate(state: GameState): RallyResult
}