package com.djasik.tiebreak.data.engine

import com.djasik.tiebreak.domain.engine.FatigueCalculator
import com.djasik.tiebreak.domain.engine.GameRules
import com.djasik.tiebreak.domain.engine.RallySimulator
import com.djasik.tiebreak.domain.model.DecisionReason
import com.djasik.tiebreak.domain.model.GamePhase
import com.djasik.tiebreak.domain.model.MatchEvent
import com.djasik.tiebreak.domain.model.MatchScore
import com.djasik.tiebreak.domain.model.RallyResult
import com.djasik.tiebreak.domain.model.Strategy
import com.djasik.tiebreak.domain.model.TeamSide
import com.djasik.tiebreak.domain.model.Teams
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameEngineImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private val rallySimulator: RallySimulator = mockk()
    private val fatigueCalculator: FatigueCalculator = mockk()
    private val gameRules: GameRules = mockk()

    private lateinit var gameEngine: GameEngineImpl

    private val playerTeam = Teams.Dragons
    private val opponentTeam = Teams.Tigers

    @BeforeEach
    fun setUp() {
        every {
            fatigueCalculator.calculatePlayerFatigue(
                current = any(),
                strategy = any(),
                stamina = any(),
            )
        } answers { firstArg() }

        every {
            fatigueCalculator.calculateOpponentFatigue(
                current = any(),
                stamina = any(),
            )
        } answers { firstArg() }

        every { gameRules.isFinished(any()) } returns false
        every { gameRules.getDecisionReason(any()) } returns null
        every { rallySimulator.simulate(any()) } returns RallyResult(winner = playerTeam)

        gameEngine = GameEngineImpl(
            rallySimulator = rallySimulator,
            fatigueCalculator = fatigueCalculator,
            gameRules = gameRules,
            dispatcher = testDispatcher,
        )
    }

    @AfterEach
    fun tearDown() = runTest {
        gameEngine.stop()
    }

    @Test
    fun `When game is started, then game state is initialized`() = runGameTest {
        startGame()

        val state = gameEngine.gameState.value

        assertEquals(GamePhase.Playing, state.phase)
        assertEquals(playerTeam, state.playerTeam)
        assertEquals(opponentTeam, state.opponentTeam)
        assertEquals(0, state.playerScore)
        assertEquals(0, state.opponentScore)
        assertTrue(state.events.isEmpty())
    }

    @Test
    fun `When initial strategy is selected, then game starts with selected strategy`() = runGameTest {
        gameEngine.setInitialStrategy(Strategy.AGGRESSIVE)
        startGame()

        assertEquals(Strategy.AGGRESSIVE, gameEngine.gameState.value.strategy)
    }

    @Test
    fun `When player wins rally, then player score and serving team are updated`() = runGameTest {
        startGame()
        advanceRally()

        val state = gameEngine.gameState.value

        assertEquals(1, state.playerScore)
        assertEquals(0, state.opponentScore)
        assertEquals(TeamSide.PLAYER, state.servingTeam)
    }

    @Test
    fun `When opponent wins rally, then opponent score and serving team are updated`() = runGameTest {
        every { rallySimulator.simulate(any()) } returns RallyResult(winner = opponentTeam)

        startGame()
        advanceRally()

        val state = gameEngine.gameState.value

        assertEquals(0, state.playerScore)
        assertEquals(1, state.opponentScore)
        assertEquals(TeamSide.OPPONENT, state.servingTeam)
    }

    @Test
    fun `When rally is completed, then point scored event is added`() = runGameTest {
        startGame()
        advanceRally()

        val event = assertInstanceOf(
            MatchEvent.PointScored::class.java,
            gameEngine.gameState.value.events.single(),
        )

        assertEquals(playerTeam, event.team)
        assertEquals(MatchScore(player = 1, opponent = 0), event.score)
    }

    @Test
    fun `When game is stopped, then pending rallies are cancelled`() = runGameTest {
        startGame()
        advanceRally()

        gameEngine.stop()
        val stateAfterStop = gameEngine.gameState.value

        advanceTimeBy(RALLY_DELAY_MS * 3)
        runCurrent()

        verify(exactly = 1) { rallySimulator.simulate(any()) }
        assertEquals(stateAfterStop, gameEngine.gameState.value)
    }

    @Test
    fun `When game is paused, then no additional rallies are simulated`() = runGameTest {
        startGame()
        advanceRally()

        gameEngine.pause()

        advanceTimeBy(RALLY_DELAY_MS * 3)
        runCurrent()

        verify(exactly = 1) { rallySimulator.simulate(any()) }
        assertEquals(1, gameEngine.gameState.value.playerScore)
    }

    @Test
    fun `When paused game is resumed, then rally simulation continues without resetting score`() =
        runGameTest {
            startGame()
            advanceRally()

            gameEngine.pause()

            advanceTimeBy(RALLY_DELAY_MS * 3)
            runCurrent()

            gameEngine.resume()
            advanceRally()

            verify(exactly = 2) { rallySimulator.simulate(any()) }
            assertEquals(2, gameEngine.gameState.value.playerScore)
        }

    @Test
    fun `When decision is required, then game suspends rally simulation`() = runGameTest {
        every {
            gameRules.getDecisionReason(any())
        } returns DecisionReason.OpponentStreak(points = 3)

        startGame()
        advanceRally()

        assertInstanceOf(
            GamePhase.AwaitingDecision::class.java,
            gameEngine.gameState.value.phase,
        )

        advanceTimeBy(RALLY_DELAY_MS * 5)
        runCurrent()

        verify(exactly = 1) { rallySimulator.simulate(any()) }
    }

    @Test
    fun `When strategy decision is submitted, then game resumes with selected strategy`() =
        runGameTest {
            every {
                gameRules.getDecisionReason(any())
            } returnsMany listOf(
                DecisionReason.OpponentStreak(points = 3),
                null,
            )

            startGame()
            advanceRally()

            assertInstanceOf(
                GamePhase.AwaitingDecision::class.java,
                gameEngine.gameState.value.phase,
            )

            gameEngine.submitStrategyDecision(Strategy.DEFENSIVE)
            runCurrent()

            assertEquals(GamePhase.Playing, gameEngine.gameState.value.phase)
            assertEquals(Strategy.DEFENSIVE, gameEngine.gameState.value.strategy)

            advanceRally()

            verify(exactly = 2) { rallySimulator.simulate(any()) }
        }

    @Test
    fun `When game is paused during decision, then pending decision is preserved`() = runGameTest {
        every {
            gameRules.getDecisionReason(any())
        } returns DecisionReason.OpponentStreak(points = 3)

        startGame()
        advanceRally()

        val phaseBefore = gameEngine.gameState.value.phase

        gameEngine.pause()

        advanceTimeBy(RALLY_DELAY_MS * 3)
        runCurrent()

        assertEquals(phaseBefore, gameEngine.gameState.value.phase)
        verify(exactly = 1) { rallySimulator.simulate(any()) }
    }

    @Test
    fun `When game is resumed during pending decision, then it continues waiting for strategy`() =
        runGameTest {
            every {
                gameRules.getDecisionReason(any())
            } returns DecisionReason.OpponentStreak(points = 3)

            startGame()
            advanceRally()

            gameEngine.pause()
            runCurrent()

            gameEngine.resume()
            runCurrent()

            advanceTimeBy(RALLY_DELAY_MS * 3)
            runCurrent()

            assertInstanceOf(
                GamePhase.AwaitingDecision::class.java,
                gameEngine.gameState.value.phase,
            )

            verify(exactly = 1) { rallySimulator.simulate(any()) }

            gameEngine.submitStrategyDecision(Strategy.AGGRESSIVE)
            runCurrent()

            assertEquals(GamePhase.Playing, gameEngine.gameState.value.phase)
            assertEquals(Strategy.AGGRESSIVE, gameEngine.gameState.value.strategy)
        }

    @Test
    fun `When game is stopped during decision, then pending decision is cancelled`() = runGameTest {
        every {
            gameRules.getDecisionReason(any())
        } returns DecisionReason.OpponentStreak(points = 3)

        startGame()
        advanceRally()

        gameEngine.stop()
        runCurrent()

        gameEngine.submitStrategyDecision(Strategy.AGGRESSIVE)

        assertEquals(GamePhase.NotStarted, gameEngine.gameState.value.phase)

        advanceTimeBy(RALLY_DELAY_MS * 3)
        runCurrent()

        verify(exactly = 1) { rallySimulator.simulate(any()) }
    }

    @Test
    fun `When game is restarted, then score fatigue events and strategy are reset`() = runGameTest {
        startGame()
        advanceRally()

        gameEngine.changeStrategy(Strategy.AGGRESSIVE)
        gameEngine.restart()

        val state = gameEngine.gameState.value

        assertEquals(GamePhase.NotStarted, state.phase)
        assertEquals(0, state.playerScore)
        assertEquals(0, state.opponentScore)
        assertEquals(0f, state.playerFatigue)
        assertEquals(0f, state.opponentFatigue)
        assertTrue(state.events.isEmpty())
        assertEquals(Strategy.BALANCED, state.strategy)
        assertEquals(playerTeam, state.playerTeam)
        assertEquals(opponentTeam, state.opponentTeam)
    }

    @Test
    fun `When game rules indicate finished match, then winner is stored in phase`() = runGameTest {
        every { gameRules.isFinished(any()) } returns true

        startGame()
        advanceRally()

        val phase = assertInstanceOf(
            GamePhase.Finished::class.java,
            gameEngine.gameState.value.phase,
        )

        assertEquals(playerTeam, phase.winner)

        advanceTimeBy(RALLY_DELAY_MS * 3)
        runCurrent()

        verify(exactly = 1) { rallySimulator.simulate(any()) }
    }

    @Test
    fun `When rally simulator throws exception, then game phase becomes Error`() = runGameTest {
        every {
            rallySimulator.simulate(any())
        } throws IllegalStateException("Simulation failed")

        startGame()
        advanceRally()

        assertEquals(GamePhase.Error, gameEngine.gameState.value.phase)
    }

    @Test
    fun `When game is already running, then another game cannot be started`() = runGameTest {
        startGame()

        gameEngine.start(
            playerTeam = opponentTeam,
            opponentTeam = playerTeam,
        )

        assertEquals(playerTeam, gameEngine.gameState.value.playerTeam)
        assertEquals(opponentTeam, gameEngine.gameState.value.opponentTeam)
    }

    @Test
    fun `When initial strategy is changed during game, then change is ignored`() = runGameTest {
        startGame()
        gameEngine.setInitialStrategy(Strategy.AGGRESSIVE)

        assertEquals(Strategy.BALANCED, gameEngine.gameState.value.strategy)
    }

    @Test
    fun `When game is not started, then resume does not start simulation`() = runGameTest {
        gameEngine.resume()

        advanceTimeBy(RALLY_DELAY_MS * 3)
        runCurrent()

        verify(exactly = 0) { rallySimulator.simulate(any()) }
        assertEquals(GamePhase.NotStarted, gameEngine.gameState.value.phase)
    }

    @Test
    fun `When strategy is submitted while paused, then it is applied after resume`() =
        runGameTest {
            every {
                gameRules.getDecisionReason(any())
            } returns DecisionReason.OpponentStreak(points = 3)

            startGame()
            advanceRally()

            assertInstanceOf(
                GamePhase.AwaitingDecision::class.java,
                gameEngine.gameState.value.phase,
            )

            gameEngine.pause()
            runCurrent()

            gameEngine.submitStrategyDecision(Strategy.DEFENSIVE)

            assertInstanceOf(
                GamePhase.AwaitingDecision::class.java,
                gameEngine.gameState.value.phase,
            )

            gameEngine.resume()
            runCurrent()

            assertEquals(GamePhase.Playing, gameEngine.gameState.value.phase)
            assertEquals(Strategy.DEFENSIVE, gameEngine.gameState.value.strategy)
        }

    private suspend fun startGame() {
        gameEngine.start(playerTeam, opponentTeam)
    }

    private fun TestScope.advanceRally() {
        advanceTimeBy(RALLY_DELAY_MS)
        runCurrent()
    }

    private fun runGameTest(
        block: suspend TestScope.() -> Unit,
    ) = runTest(testDispatcher) {
        try {
            block()
        } finally {
            gameEngine.stop()
        }
    }

    private companion object {
        const val RALLY_DELAY_MS = 2_000L
    }
}
