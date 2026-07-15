// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.app

import io.github.ronjunevaldoz.awake.sample.server.webSocketDebugLoop
import io.github.ronjunevaldoz.awake.sample.server.withOptionalDebugLoop
import io.github.ronjunevaldoz.awake.sample.startergame.debug.asDebugService
import io.github.ronjunevaldoz.awake.sample.startergame.debug.parseStarterDebugCommand
import io.github.ronjunevaldoz.awake.sample.startergame.debug.starterGameDebugConfig
import io.github.ronjunevaldoz.awake.sample.startergame.debug.starterGameDebugController
import io.github.ronjunevaldoz.awake.vulkan.application.runVulkanDesktopGame
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun main() {
    val game = starterGame()
    val debugController = game.starterGameDebugController
    withOptionalDebugLoop(
        enabled = game.starterGameDebugConfig.websocketControlsEnabled,
        createLoop = {
            webSocketDebugLoop(
                parseCommand = ::parseStarterDebugCommand,
                encodeResponse = { Json.encodeToString(it) },
                service = debugController.asDebugService()
            )
        }
    ) { beforeFrame, afterLoop ->
        runVulkanDesktopGame(
            game = game,
            applicationFactory = ::createStarterGameVulkanApplication,
            beforeFrame = beforeFrame,
            afterLoop = afterLoop
        )
    }
}
