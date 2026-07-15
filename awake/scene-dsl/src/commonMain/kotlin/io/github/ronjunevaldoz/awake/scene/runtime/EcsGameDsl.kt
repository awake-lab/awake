// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.engine.application.AwakeGame
import io.github.ronjunevaldoz.awake.engine.application.GameInstaller
import io.github.ronjunevaldoz.awake.engine.application.GameSpec
import io.github.ronjunevaldoz.awake.engine.application.WindowDsl
import io.github.ronjunevaldoz.awake.engine.application.gameModule
import io.github.ronjunevaldoz.awake.engine.application.gameSpec
import io.github.ronjunevaldoz.awake.engine.application.module
import io.github.ronjunevaldoz.awake.ui.GameUiDsl
import io.github.ronjunevaldoz.awake.ui.GameUiSpec
import io.github.ronjunevaldoz.awake.ui.gameUi
import io.github.ronjunevaldoz.awake.ui.ui

fun ecsGame(
    block: EcsGameDsl.() -> Unit
): AwakeGame = ecsGameSpec(block).createGame()

fun ecsGameSpec(
    block: EcsGameDsl.() -> Unit
): GameSpec {
    return EcsGameDsl().apply(block).build()
}

class EcsGameDsl internal constructor() {
    private var windowBlock: WindowDsl.() -> Unit = {}
    private var sceneInstaller: GameInstaller? = null
    private var uiSpec: GameUiSpec? = null
    private val installers = mutableListOf<GameInstaller>()

    fun window(block: WindowDsl.() -> Unit) {
        windowBlock = block
    }

    fun ecs(
        name: String? = null,
        block: SceneGameDsl.() -> Unit
    ) {
        scene(name, block)
    }

    fun scene(
        name: String? = null,
        block: SceneGameDsl.() -> Unit
    ) {
        scene(
            sceneGame {
                if (name != null) {
                    this.name(name)
                }
                block()
            }
        )
    }

    fun scene(spec: SceneGameSpec) {
        sceneInstaller = spec
    }

    fun flow(spec: SceneRouterSpec) {
        sceneInstaller = spec
    }

    fun flow(block: SceneFlowDsl.() -> Unit) {
        flow(sceneFlow(block))
    }

    fun ui(block: GameUiDsl.() -> Unit) {
        ui(gameUi(block))
    }

    fun ui(spec: GameUiSpec) {
        uiSpec = spec
    }

    fun install(installer: GameInstaller) {
        installers += installer
    }

    internal fun build(): GameSpec {
        val builtScene = checkNotNull(sceneInstaller) {
            "ecsGameSpec requires a scene { ... }, ecs { ... }, or flow { ... } block."
        }
        val featureModule = gameModule {
            install(builtScene)
            uiSpec?.let { ui(it) }
            installers.forEach { install(it) }
        }
        return gameSpec {
            window(windowBlock)
            module(featureModule)
        }
    }
}
