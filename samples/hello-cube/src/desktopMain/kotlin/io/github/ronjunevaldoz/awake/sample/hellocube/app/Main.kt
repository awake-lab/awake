// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.sample.server.webSocketDebugLoop
import io.github.ronjunevaldoz.awake.sample.server.withOptionalDebugLoop
import io.github.ronjunevaldoz.awake.vulkan.application.runVulkanDesktopGame
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.asDebugService
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.parseDebugCommand
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.helloCubeDebugConfig
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.helloCubeDebugController

fun main() {
    val game = helloCubeGame()
    val debugController = game.helloCubeDebugController
    val debugConfig = game.helloCubeDebugConfig

    withOptionalDebugLoop(
        enabled = debugConfig.websocketControlsEnabled,
        createLoop = {
            webSocketDebugLoop(
                parseCommand = ::parseDebugCommand,
                encodeResponse = { Json.encodeToString(it) },
                service = debugController.asDebugService()
            )
        }
    ) { beforeFrame, afterLoop ->
        runVulkanDesktopGame(
            game = game,
            applicationFactory = ::createHelloCubeVulkanApplication,
            beforeFrame = beforeFrame,
            afterLoop = afterLoop
        )
    }
}
