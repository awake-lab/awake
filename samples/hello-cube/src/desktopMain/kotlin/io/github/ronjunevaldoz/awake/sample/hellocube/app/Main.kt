// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.sample.server.debugControlLoop
import io.github.ronjunevaldoz.awake.vulkan.application.runVulkanDesktopGame
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.DebugCommand
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.DebugSnapshot
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.HelloCubeDebugController
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.parseDebugCommand
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.helloCubeDebugConfig
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.helloCubeDebugController

private fun applyDebugCommand(debugController: HelloCubeDebugController, command: DebugCommand) {
    when (command) {
        is DebugCommand.SwitchDemo -> debugController.switchDemo(command.index)
        is DebugCommand.SetCameraEye -> debugController.setCameraEye(Vec3(command.x, command.y, command.z))
        is DebugCommand.SetCameraCenter -> debugController.setCameraCenter(Vec3(command.x, command.y, command.z))
        is DebugCommand.SetMinimap -> debugController.setMinimap(command.enabled)
        DebugCommand.GetState -> Unit
    }
}

fun main() {
    val game = helloCubeGame()
    val debugController = game.helloCubeDebugController
    val debugConfig = game.helloCubeDebugConfig
    val debugLoop = if (debugConfig.websocketControlsEnabled) {
        debugControlLoop<DebugCommand, DebugSnapshot>(
            parseCommand = ::parseDebugCommand,
            encodeResponse = { Json.encodeToString(it) },
            applyCommand = { command -> applyDebugCommand(debugController, command) },
            snapshot = debugController::snapshot
        ).also { it.start() }
    } else {
        null
    }

    runVulkanDesktopGame(
        game = game,
        applicationFactory = ::createHelloCubeVulkanApplication,
        beforeFrame = {
            debugLoop?.beforeFrame()
        },
        afterLoop = {
            debugLoop?.stop()
        }
    )
}
