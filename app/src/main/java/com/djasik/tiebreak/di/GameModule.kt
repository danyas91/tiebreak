package com.djasik.tiebreak.di

import com.djasik.tiebreak.data.engine.FatigueCalculatorImpl
import com.djasik.tiebreak.data.engine.GameEngineImpl
import com.djasik.tiebreak.data.engine.GameRulesImpl
import com.djasik.tiebreak.data.engine.RallySimulatorImpl
import com.djasik.tiebreak.domain.engine.FatigueCalculator
import com.djasik.tiebreak.domain.engine.GameEngine
import com.djasik.tiebreak.domain.engine.GameRules
import com.djasik.tiebreak.domain.engine.RallySimulator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GameModule {

    @Binds
    @Singleton
    abstract fun bindGameEngine(
        implementation: GameEngineImpl,
    ): GameEngine

    @Binds
    @Singleton
    abstract fun bindRallySimulator(
        implementation: RallySimulatorImpl,
    ): RallySimulator

    @Binds
    @Singleton
    abstract fun bindFatigueCalculator(
        implementation: FatigueCalculatorImpl,
    ): FatigueCalculator

    @Binds
    @Singleton
    abstract fun bindGameRules(
        implementation: GameRulesImpl,
    ): GameRules
}