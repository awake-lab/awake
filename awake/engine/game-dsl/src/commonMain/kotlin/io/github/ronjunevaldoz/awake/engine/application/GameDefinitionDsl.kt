// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

fun <State> gameDefinition(
    createState: () -> State,
    block: GameDefinitionDsl<State>.() -> Unit
): GameDefinition<State> {
    return GameDefinitionDsl(createState).apply(block).build()
}

class GameDefinition<State> internal constructor(
    private val createStateBlock: () -> State,
    private val windowBlock: WindowDsl.() -> Unit,
    private val moduleFactory: (State) -> GameModule
) {
    fun createState(): State = createStateBlock()

    fun createModule(state: State): GameModule = moduleFactory(state)

    fun createGame(): AwakeGame = createGame(createState())

    fun createGame(state: State): AwakeGame = createGameSpec(state).createGame()

    fun createGameSpec(): GameSpec = createGameSpec(createState())

    fun createGameSpec(state: State): GameSpec = createModule(state).createGameSpec(windowBlock)
}

@AwakeGameDsl
class GameDefinitionDsl<State> internal constructor(
    private val createStateBlock: () -> State
) {
    private var windowBlock: WindowDsl.() -> Unit = {}
    private var moduleFactory: ((State) -> GameModule)? = null

    fun window(block: WindowDsl.() -> Unit) {
        windowBlock = block
    }

    fun module(factory: (State) -> GameModule) {
        moduleFactory = factory
    }

    fun module(module: GameModule) {
        moduleFactory = { module }
    }

    internal fun build(): GameDefinition<State> {
        val builtModule = checkNotNull(moduleFactory) {
            "gameDefinition requires a module { ... } factory or module(instance)."
        }
        return GameDefinition(
            createStateBlock = createStateBlock,
            windowBlock = windowBlock,
            moduleFactory = builtModule
        )
    }
}
