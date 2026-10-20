# TieBreak

TieBreak is an Android application that simulates a volleyball tie-break match. It was created as part of a **Kotlin Coroutines Mastery course** to practice working with Coroutines, Flow, and asynchronous programming.

The user can choose a team strategy, follow the match simulation, and adjust the strategy during the game.

## Features

- Volleyball match simulation with automatic score updates
- Three available strategies: Balanced, Aggressive, and Defensive
- Strategy changes during the match
- Fatigue calculation for both teams
- Game state updates using StateFlow

## How to Play

1. Select your team and opponent
2. Choose an initial strategy: Balanced, Aggressive, or Defensive
3. Start the match and follow the score as rallies are simulated automatically
4. When the game asks for a strategy decision, choose how your team should continue playing

The goal is to win the tie-break by reaching 15 points with at least a 2-point lead.

## Architecture

The game logic is separated from the UI and divided into several components:

- **GameEngine** – controls the game flow and manages the current state
- **RallySimulator** – simulates rallies and determines their results
- **FatigueCalculator** – calculates fatigue during the match
- **GameRules** – checks match completion and strategy decision conditions
- **GameState** – holds the current score, strategy, fatigue, and match events

## Coroutines & Flow

The game uses Kotlin Coroutines and Flow to handle the simulation and state updates.

- **Coroutines** to run the game simulation
- **StateFlow** to observe game state changes
- **CoroutineDispatcher** to control coroutine execution
- **Job cancellation** to pause and stop the game
- **CompletableDeferred** to wait for the user's strategy decision
- **Mutex** to protect shared game engine state from concurrent modifications during game lifecycle operations

## Debugging & Logs

The application includes logs that can be viewed in Android Studio Logcat. They help track the game flow and observe how Kotlin Coroutines work during the simulation, including coroutine execution, cancellation, game state changes, and waiting for strategy decisions.
