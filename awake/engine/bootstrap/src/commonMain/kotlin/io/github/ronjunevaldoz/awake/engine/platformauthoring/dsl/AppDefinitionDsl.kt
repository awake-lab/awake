// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.platformauthoring.dsl

import io.github.ronjunevaldoz.awake.engine.app.core.AppModule
import io.github.ronjunevaldoz.awake.engine.app.core.AppSpec
import io.github.ronjunevaldoz.awake.engine.app.lifecycle.AwakeAppLifecycle

fun <State> appDefinition(
    createState: () -> State,
    block: AppDefinitionDsl<State>.() -> Unit,
): AppDefinition<State> = AppDefinitionDsl(createState).apply(block).build()

class AppDefinition<State> internal constructor(
    private val createStateBlock: () -> State,
    private val windowBlock: WindowDsl.() -> Unit,
    private val moduleFactory: (State) -> AppModule,
) {
    fun createState(): State = createStateBlock()

    fun createModule(state: State): AppModule = moduleFactory(state)

    fun createApp(): AwakeAppLifecycle = createApp(createState())

    fun createApp(state: State): AwakeAppLifecycle = createAppSpec(state).createLifecycle()

    fun createAppSpec(): AppSpec = createAppSpec(createState())

    fun createAppSpec(state: State): AppSpec = createModule(state).createAppSpec(windowBlock)
}

@AwakeGameDsl
class AppDefinitionDsl<State> internal constructor(
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

    internal fun build(): AppDefinition<State> {
        val builtModule = checkNotNull(moduleFactory) {
            "gameDefinition requires a module { ... } factory or module(instance)."
        }
        return AppDefinition(
            createStateBlock = createStateBlock,
            windowBlock = windowBlock,
            moduleFactory = builtModule,
        )
    }
}
