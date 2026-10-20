package com.djasik.tiebreak.domain.engine

import com.djasik.tiebreak.domain.model.GameState
import com.djasik.tiebreak.domain.model.Strategy
import com.djasik.tiebreak.domain.model.Team
import kotlinx.coroutines.flow.StateFlow

interface GameEngine {
    val gameState: StateFlow<GameState>

    suspend fun start(playerTeam: Team, opponentTeam: Team)
    suspend fun stop()
    suspend fun restart()
    suspend fun pause()
    suspend fun resume()
    suspend fun setInitialStrategy(strategy: Strategy)
    suspend fun changeStrategy(strategy: Strategy)
    suspend fun submitStrategyDecision(strategy: Strategy)
}