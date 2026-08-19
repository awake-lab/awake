// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.gameauthoring

import io.github.ronjunevaldoz.awake.engine.app.core.AppModule
import io.github.ronjunevaldoz.awake.engine.app.core.AppSpec
import io.github.ronjunevaldoz.awake.engine.app.lifecycle.AwakeAppLifecycle

fun <State> gameDefinition(
    createState: () -> State,
    block: GameDefinitionDsl<State>.() -> Unit,
): GameDefinition<State> = GameDefinitionDsl(createState).apply(block).build()

class GameDefinition<State> internal constructor(
    private val createStateBlock: () -> State,
    private val windowBlock: WindowDsl.() -> Unit,
    private val moduleFactory: (State) -> AppModule,
) {
    fun createState(): State = createStateBlock()

    fun createModule(state: State): AppModule = moduleFactory(state)

    fun createGame(): AwakeAppLifecycle = createGame(createState())

    fun createGame(state: State): AwakeAppLifecycle = createGameSpec(state).createLifecycle()

    fun createGameSpec(): AppSpec = createGameSpec(createState())

    fun createGameSpec(state: State): AppSpec = createModule(state).createGameSpec(windowBlock)
}

@AwakeGameDsl
class GameDefinitionDsl<State> internal constructor(
    private val createStateBlock: () -> State,
) {
    private var windowBlock: WindowDsl.() -> Unit = {}
    private var moduleFactory: ((State) -> AppModule)? = null

    fun window(block: WindowDsl.() -> Unit) {
        windowBlock = block
    }

    fun module(factory: (State) -> AppModule) {
        moduleFactory = factory
    }

    fun module(module: AppModule) {
        moduleFactory = { module }
    }

    internal fun build(): GameDefinition<State> {
        val builtModule = checkNotNull(moduleFactory) {
            "gameDefinition requires a module { ... } factory or module(instance)."
        }
        return GameDefinition(
            createStateBlock = createStateBlock,
            windowBlock = windowBlock,
            moduleFactory = builtModule,
        )
    }
}
