// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.engine.application.GameInstaller
import io.github.ronjunevaldoz.awake.engine.application.GameSpec
import io.github.ronjunevaldoz.awake.engine.application.GameUiDsl
import io.github.ronjunevaldoz.awake.engine.application.GameUiSpec
import io.github.ronjunevaldoz.awake.engine.application.WindowDsl
import io.github.ronjunevaldoz.awake.engine.application.gameModule
import io.github.ronjunevaldoz.awake.engine.application.gameSpec
import io.github.ronjunevaldoz.awake.engine.application.gameUi
import io.github.ronjunevaldoz.awake.engine.application.module
import io.github.ronjunevaldoz.awake.engine.application.ui

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
        val featureModule = gameModule module@{
            install(builtScene)
            // Explicit `this@module` receiver required: GameModuleDsl.ui(spec) is an extension,
            // but EcsGameDsl also has a member ui(spec) (this@build's implicit outer receiver)
            // with the same signature -- Kotlin always prefers a member over an extension
            // regardless of scope distance, so an implicit-receiver `ui(it)` call here silently
            // resolves to EcsGameDsl.ui(spec) (just uiSpec = uiSpec, a no-op) instead of
            // installing the spec into this GameModuleDsl builder.
            val spec = uiSpec
            if (spec != null) {
                this@module.ui(spec)
            }
            installers.forEach { install(it) }
        }
        return gameSpec {
            window(windowBlock)
            module(featureModule)
        }
    }
}
