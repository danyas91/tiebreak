package com.djasik.tiebreak.domain.engine

import com.djasik.tiebreak.domain.model.DecisionReason
import com.djasik.tiebreak.domain.model.GameState

interface GameRules {
    fun isFinished(state: GameState): Boolean

    fun getDecisionReason(state: GameState): DecisionReason?
}