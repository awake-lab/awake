// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanWindow
import io.github.ronjunevaldoz.awake.vulkan.application.runVulkanDesktopGame
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.github.ronjunevaldoz.awake.sample.server.DebugControlServer
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.DebugCommand
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.DebugSnapshot
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.HelloCubeDebugController
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.parseDebugCommand
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.helloCubeDebugConfig
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.helloCubeDebugController

// GLFW key/mouse codes this sample cares about -- see glfw3.h. A small, hand-picked subset
// (matching io.github.ronjunevaldoz.awake.core.input.Key), not a full GLFW key table;
// extend as real gameplay needs more keys, rather than pre-mapping GLFW's entire key space.
private const val GLFW_KEY_SPACE = 32
private const val GLFW_KEY_ESCAPE = 256
private const val GLFW_KEY_RIGHT = 262
private const val GLFW_KEY_LEFT = 263
private const val GLFW_KEY_DOWN = 264
private const val GLFW_KEY_UP = 265
private const val GLFW_KEY_A = 65
private const val GLFW_KEY_D = 68
private const val GLFW_KEY_S = 83
private const val GLFW_KEY_W = 87
private const val GLFW_MOUSE_BUTTON_LEFT = 0
private const val GLFW_PRESS = 1

private val polledKeys = mapOf(
    GLFW_KEY_W to Key.W,
    GLFW_KEY_A to Key.A,
    GLFW_KEY_S to Key.S,
    GLFW_KEY_D to Key.D,
    GLFW_KEY_UP to Key.ArrowUp,
    GLFW_KEY_DOWN to Key.ArrowDown,
    GLFW_KEY_LEFT to Key.ArrowLeft,
    GLFW_KEY_RIGHT to Key.ArrowRight,
    GLFW_KEY_SPACE to Key.Space,
    GLFW_KEY_ESCAPE to Key.Escape
)

/**
 * Polls GLFW key/mouse state into [Input] once per frame -- called right after
 * `glfwPollEvents()`, on the same render thread (see `docs/architecture.md`'s
 * threading-model rules), so no callback/synchronization machinery is needed.
 *
 * `glfwGetCursorPos` returns screen/logical coordinates (GLFW's own "screen coordinates",
 * NOT framebuffer pixels), but [Input.pointerX]/[Input.pointerY] must be in the same
 * FRAMEBUFFER-PIXEL space `UiContext.hitTest` and the `screenSize` uniform
 * (`swapchainManager.extent.width/height`, see `VulkanGameApplication.onRender()`) use --
 * on a Retina/HiDPI display the framebuffer is a device-pixel-ratio multiple (e.g. 2x) of
 * the logical window size, so the raw cursor position must be scaled up by
 * framebufferSize/windowSize before hit-testing, or every click lands at half its real
 * position (see the investigation this restores from git history for the original bug).
 */
private fun pollDesktopInput(window: Long) {
    polledKeys.forEach { (glfwKey, key) ->
        Input.setKeyDown(key, VulkanWindow.glfwGetKey(window, glfwKey) == GLFW_PRESS)
    }

    val cursor = VulkanWindow.glfwGetCursorPos(window)
    val windowWidth = VulkanWindow.glfwGetWindowWidth(window)
    val windowHeight = VulkanWindow.glfwGetWindowHeight(window)
    val framebufferWidth = VulkanWindow.glfwGetFramebufferWidth(window)
    val framebufferHeight = VulkanWindow.glfwGetFramebufferHeight(window)
    val scaleX = if (windowWidth != 0) framebufferWidth.toFloat() / windowWidth else 1f
    val scaleY = if (windowHeight != 0) framebufferHeight.toFloat() / windowHeight else 1f

    val leftButtonDown = VulkanWindow.glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS
    Input.setPointer(
        down = leftButtonDown,
        x = cursor[0].toFloat() * scaleX,
        y = cursor[1].toFloat() * scaleY
    )

    // Drains the native scroll accumulator glfwSetScrollCallback (registered once in main(),
    // right after glfwCreateWindow) feeds -- see Input.scrollDeltaY's doc comment for why
    // this is a plain assignment (the native getter itself resets to 0) rather than a `+=`:
    // exactly one poll happens per frame here, so there's nothing to accumulate on top of.
    Input.scrollDeltaY = VulkanWindow.glfwConsumeScrollDeltaY(window).toFloat()
}

/** Applies one drained [DebugCommand] to [demoCatalog] -- called from `main()`'s per-frame
 * loop, on the render thread (see [DebugControlServer]'s own doc comment for why this must
 * never happen from the WebSocket handler coroutine directly). [DebugCommand.GetState] needs
 * no mutation -- the caller always reads a fresh debug snapshot afterward regardless of which
 * command ran. */
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
    val debugServer = if (debugConfig.websocketControlsEnabled) {
        DebugControlServer<DebugCommand, DebugSnapshot>(
            parseCommand = ::parseDebugCommand,
            encodeResponse = { Json.encodeToString(it) }
        ).also(DebugControlServer<DebugCommand, DebugSnapshot>::start)
    } else {
        null
    }

    runVulkanDesktopGame(
        game = game,
        application = createHelloCubeVulkanApplication(game),
        pollInput = ::pollDesktopInput,
        beforeFrame = {
            debugServer?.drainCommands()?.forEach { (command, deferred) ->
                applyDebugCommand(debugController, command)
                deferred.complete(debugController.snapshot())
            }
        },
        afterLoop = {
            debugServer?.stop()
        }
    )
}
