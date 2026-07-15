// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.app

import io.github.ronjunevaldoz.awake.sample.server.debugControlLoop
import io.github.ronjunevaldoz.awake.sample.startergame.debug.StarterDebugCommand
import io.github.ronjunevaldoz.awake.sample.startergame.debug.StarterDebugSnapshot
import io.github.ronjunevaldoz.awake.sample.startergame.debug.parseStarterDebugCommand
import io.github.ronjunevaldoz.awake.sample.startergame.debug.starterGameDebugConfig
import io.github.ronjunevaldoz.awake.sample.startergame.debug.starterGameDebugController
import io.github.ronjunevaldoz.awake.vulkan.application.pollGlfwInput
import io.github.ronjunevaldoz.awake.vulkan.application.runVulkanDesktopGame
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private fun applyDebugCommand(
    controller: io.github.ronjunevaldoz.awake.sample.startergame.debug.StarterGameDebugController,
    command: StarterDebugCommand
) {
    when (command) {
        is StarterDebugCommand.SwitchScene -> controller.switchScene(command.sceneId)
        is StarterDebugCommand.SetTipsVisible -> controller.setTipsVisible(command.enabled)
        StarterDebugCommand.GetState -> Unit
    }
}

fun main() {
    val game = starterGame()
    val debugController = game.starterGameDebugController
    val debugLoop = if (game.starterGameDebugConfig.websocketControlsEnabled) {
        debugControlLoop<StarterDebugCommand, StarterDebugSnapshot>(
            parseCommand = ::parseStarterDebugCommand,
            encodeResponse = { Json.encodeToString(it) },
            applyCommand = { command -> applyDebugCommand(debugController, command) },
            snapshot = debugController::snapshot
        ).also { it.start() }
    } else {
        null
    }

    runVulkanDesktopGame(
        game = game,
        application = createStarterGameVulkanApplication(game),
        pollInput = ::pollGlfwInput,
        beforeFrame = {
            debugLoop?.beforeFrame()
        },
        afterLoop = {
            debugLoop?.stop()
        }
    )
}
