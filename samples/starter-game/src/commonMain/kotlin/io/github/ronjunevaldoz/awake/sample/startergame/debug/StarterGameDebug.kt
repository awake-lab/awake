// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.debug

import io.github.ronjunevaldoz.awake.engine.application.AwakeGame
import io.github.ronjunevaldoz.awake.engine.application.requireService
import io.github.ronjunevaldoz.awake.sample.server.DebugService
import io.github.ronjunevaldoz.awake.sample.startergame.state.StarterGameRuntimeState
import io.github.ronjunevaldoz.awake.scene.runtime.SceneRouteInfo
import io.github.ronjunevaldoz.awake.scene.runtime.SceneRouterRuntime

internal data class StarterGameDebugConfig(
    val websocketControlsEnabled: Boolean
)

internal class StarterGameDebugController(
    private val router: SceneRouterRuntime,
    private val state: StarterGameRuntimeState
) {
    fun switchScene(sceneId: String) {
        router.switchTo(sceneId)
    }

    fun setTipsVisible(enabled: Boolean) {
        state.tipsVisible = enabled
    }

    fun snapshot(): StarterDebugSnapshot = StarterDebugSnapshot(
        activeSceneId = router.activeSceneId,
        activeSceneLabel = router.activeSceneLabel,
        sceneLabels = router.scenes.map(SceneRouteInfo::label),
        tipsVisible = state.tipsVisible
    )
}

internal val AwakeGame.starterGameDebugConfig: StarterGameDebugConfig
    get() = requireService()

internal val AwakeGame.starterGameDebugController: StarterGameDebugController
    get() = requireService()

internal fun StarterGameDebugController.asDebugService(): DebugService<StarterDebugCommand, StarterDebugSnapshot> =
    object : DebugService<StarterDebugCommand, StarterDebugSnapshot> {
        override fun handle(command: StarterDebugCommand) {
            when (command) {
                is StarterDebugCommand.SwitchScene -> switchScene(command.sceneId)
                is StarterDebugCommand.SetTipsVisible -> setTipsVisible(command.enabled)
                StarterDebugCommand.GetState -> Unit
            }
        }

        override fun snapshot(): StarterDebugSnapshot = this@asDebugService.snapshot()
    }
