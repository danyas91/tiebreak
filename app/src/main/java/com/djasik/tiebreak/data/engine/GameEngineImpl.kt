package com.djasik.tiebreak.data.engine

import com.djasik.tiebreak.di.DefaultDispatcher
import com.djasik.tiebreak.domain.engine.FatigueCalculator
import com.djasik.tiebreak.domain.engine.GameEngine
import com.djasik.tiebreak.domain.engine.GameRules
import com.djasik.tiebreak.domain.engine.RallySimulator
import com.djasik.tiebreak.domain.model.DecisionReason
import com.djasik.tiebreak.domain.model.GamePhase
import com.djasik.tiebreak.domain.model.GameState
import com.djasik.tiebreak.domain.model.MatchEvent
import com.djasik.tiebreak.domain.model.MatchScore
import com.djasik.tiebreak.domain.model.RallyResult
import com.djasik.tiebreak.domain.model.Strategy
import com.djasik.tiebreak.domain.model.Team
import com.djasik.tiebreak.domain.model.TeamSide
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random

class GameEngineImpl @Inject constructor(
    private val rallySimulator: RallySimulator,
    private val fatigueCalculator: FatigueCalculator,
    private val gameRules: GameRules,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) : GameEngine {

    private val engineScope = CoroutineScope(
        SupervisorJob() +
                dispatcher +
                CoroutineName("GameEngine")
    )

    private val stateMutex = Mutex()

    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var gameJob: Job? = null

    private var strategyDecision: CompletableDeferred<Strategy>? = null

    override suspend fun start(
        playerTeam: Team,
        opponentTeam: Team,
    ) = stateMutex.withLock {
        if (gameJob?.isActive == true) {
            Timber.w("Game is already running")
            return@withLock
        }

        if (_gameState.value.phase != GamePhase.NotStarted) {
            Timber.w("Cannot start game in phase: %s", _gameState.value.phase)
            return@withLock
        }

        val initialStrategy = _gameState.value.strategy

        clearStrategyDecision()

        _gameState.value = GameState(
            playerTeam = playerTeam,
            opponentTeam = opponentTeam,
            strategy = initialStrategy,
            servingTeam = if (Random.nextBoolean()) {
                TeamSide.PLAYER
            } else {
                TeamSide.OPPONENT
            },
            phase = GamePhase.Playing,
        )

        Timber.i(
            "Starting match: %s vs %s, strategy: %s",
            playerTeam.id,
            opponentTeam.id,
            initialStrategy,
        )

        launchGameLoop()
    }

    override suspend fun stop() = stateMutex.withLock {
        Timber.i("Stopping game")

        gameJob?.cancel()
        gameJob = null
        clearStrategyDecision()

        val current = _gameState.value

        _gameState.value = GameState(
            playerTeam = current.playerTeam,
            opponentTeam = current.opponentTeam,
            strategy = current.strategy,
        )
    }

    override suspend fun restart() = stateMutex.withLock {
        Timber.i("Restarting game")

        gameJob?.cancel()
        gameJob = null
        clearStrategyDecision()

        val current = _gameState.value

        _gameState.value = GameState(
            playerTeam = current.playerTeam,
            opponentTeam = current.opponentTeam,
            strategy = Strategy.BALANCED,
            servingTeam = TeamSide.PLAYER,
            phase = GamePhase.NotStarted,
        )
    }

    override suspend fun resume() = stateMutex.withLock {
        if (gameJob?.isActive == true) {
            return@withLock
        }

        when (_gameState.value.phase) {
            GamePhase.NotStarted,
            is GamePhase.Finished,
            GamePhase.Error -> {
                Timber.w("Cannot resume game in phase: %s", _gameState.value.phase)
                return@withLock
            }

            GamePhase.Playing,
            is GamePhase.AwaitingDecision -> Unit
        }

        Timber.d("Game resumed")

        launchGameLoop()
    }

    private fun launchGameLoop() {
        gameJob = engineScope.launch(
            CoroutineName("GameLoop")
        ) {
            try {
                runGameLoop()
            } catch (exception: CancellationException) {
                Timber.d("Game loop cancelled")
                throw exception
            } catch (exception: Exception) {
                Timber.e(exception, "Unexpected error in game loop")
                handleGameError()
            } finally {
                Timber.d("Game loop finished")
            }
        }
    }

    override suspend fun pause() = stateMutex.withLock {
        gameJob?.cancel()
        gameJob = null

        Timber.d("Game paused")
    }

    override suspend fun setInitialStrategy(
        strategy: Strategy,
    ) = stateMutex.withLock {
        if (_gameState.value.phase != GamePhase.NotStarted) {
            Timber.w("Cannot set initial strategy in phase: %s", _gameState.value.phase)
            return@withLock
        }

        Timber.d("Initial strategy selected: %s", strategy)

        _gameState.update {
            it.copy(
                strategy = strategy,
            )
        }
    }

    override suspend fun changeStrategy(
        strategy: Strategy,
    ) = stateMutex.withLock {
        if (_gameState.value.phase != GamePhase.Playing) {
            Timber.w("Cannot change strategy in phase: %s", _gameState.value.phase)
            return@withLock
        }

        Timber.d("Strategy changed to: %s", strategy)

        _gameState.update {
            it.copy(
                strategy = strategy,
            )
        }
    }

    override suspend fun submitStrategyDecision(
        strategy: Strategy,
    ) = stateMutex.withLock {
        if (_gameState.value.phase !is GamePhase.AwaitingDecision) {
            Timber.w("No strategy decision is currently required")
            return@withLock
        }

        val decision = strategyDecision ?: return@withLock

        if (decision.complete(strategy)) {
            Timber.d("Strategy decision submitted: %s", strategy)
        }
    }

    private suspend fun runGameLoop() {
        while (currentCoroutineContext().isActive) {
            val phase = _gameState.value.phase

            if (phase is GamePhase.AwaitingDecision) {
                waitForStrategyDecision(reason = phase.reason)
                continue
            }

            if (phase != GamePhase.Playing) {
                return
            }

            delay(RALLY_DELAY_MS)

            val decisionReason = stateMutex.withLock {
                currentCoroutineContext().ensureActive()

                if (_gameState.value.phase != GamePhase.Playing) {
                    return@withLock null
                }

                val currentState = _gameState.value
                val rallyResult = rallySimulator.simulate(state = currentState)

                currentCoroutineContext().ensureActive()

                applyRallyResult(result = rallyResult)

                val updatedState = _gameState.value

                if (gameRules.isFinished(updatedState)) {
                    finishGame(state = updatedState)
                    return@withLock null
                }

                gameRules.getDecisionReason(state = updatedState)
            }

            if (decisionReason != null) {
                waitForStrategyDecision(reason = decisionReason)
            }
        }
    }

    private fun applyRallyResult(
        result: RallyResult,
    ) {
        _gameState.update { current ->
            val playerWon = result.winner == current.playerTeam

            val playerScore = current.playerScore + if (playerWon) 1 else 0
            val opponentScore = current.opponentScore + if (playerWon) 0 else 1

            Timber.d(
                "Point for %s, score: %d:%d",
                result.winner.id,
                playerScore,
                opponentScore,
            )

            current.copy(
                playerScore = playerScore,
                opponentScore = opponentScore,
                playerFatigue = fatigueCalculator.calculatePlayerFatigue(
                    current = current.playerFatigue,
                    strategy = current.strategy,
                    stamina = current.playerTeam.stats.stamina,
                ),
                opponentFatigue = fatigueCalculator.calculateOpponentFatigue(
                    current = current.opponentFatigue,
                    stamina = current.opponentTeam.stats.stamina,
                ),
                servingTeam = if (playerWon) {
                    TeamSide.PLAYER
                } else {
                    TeamSide.OPPONENT
                },
                events = (
                        current.events + MatchEvent.PointScored(
                            team = result.winner,
                            score = MatchScore(
                                player = playerScore,
                                opponent = opponentScore,
                            ),
                        )
                        ).takeLast(GameState.MAX_EVENT_HISTORY),
            )
        }
    }

    private fun finishGame(
        state: GameState,
    ) {
        val winner = if (state.playerScore > state.opponentScore) {
            state.playerTeam
        } else {
            state.opponentTeam
        }

        Timber.i(
            "Game finished. Winner: %s, score: %d:%d",
            winner.id,
            state.playerScore,
            state.opponentScore,
        )

        _gameState.update {
            it.copy(
                phase = GamePhase.Finished(
                    winner = winner,
                )
            )
        }
    }

    private suspend fun waitForStrategyDecision(
        reason: DecisionReason,
    ) {
        val decision = stateMutex.withLock {
            currentCoroutineContext().ensureActive()

            if (_gameState.value.phase != GamePhase.Playing &&
                _gameState.value.phase !is GamePhase.AwaitingDecision
            ) {
                return@withLock null
            }

            val pendingDecision = strategyDecision
                ?.takeUnless { it.isCancelled }
                ?: CompletableDeferred<Strategy>().also {
                    strategyDecision = it
                }

            Timber.d("Waiting for strategy decision: %s", reason)

            _gameState.update {
                it.copy(
                    phase = GamePhase.AwaitingDecision(
                        reason = reason,
                    )
                )
            }

            pendingDecision
        } ?: return

        val strategy = decision.await()

        stateMutex.withLock {
            currentCoroutineContext().ensureActive()

            if (strategyDecision !== decision || _gameState.value.phase !is GamePhase.AwaitingDecision) {
                return@withLock
            }

            Timber.d("Strategy decision received: %s", strategy)

            _gameState.update {
                it.copy(
                    strategy = strategy,
                    phase = GamePhase.Playing,
                )
            }

            strategyDecision = null
        }
    }

    private suspend fun handleGameError() {
        stateMutex.withLock {
            currentCoroutineContext().ensureActive()

            clearStrategyDecision()

            _gameState.update {
                it.copy(
                    phase = GamePhase.Error,
                )
            }
        }
    }

    private fun clearStrategyDecision() {
        strategyDecision?.let { decision ->
            if (!decision.isCompleted) {
                decision.cancel()
            }
        }

        strategyDecision = null
    }

    private companion object {
        const val RALLY_DELAY_MS = 2_000L
    }
}
