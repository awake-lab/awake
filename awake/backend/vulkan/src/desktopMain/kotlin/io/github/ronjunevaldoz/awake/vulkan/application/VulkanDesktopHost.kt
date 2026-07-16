// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.application

import io.github.ronjunevaldoz.awake.core.application.DesktopGameLoop
import io.github.ronjunevaldoz.awake.engine.application.AwakeGame
import io.github.ronjunevaldoz.awake.engine.application.GameWindowBackend
import io.github.ronjunevaldoz.awake.ui.UiDensity
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanWindow

private const val GLFW_CLIENT_API = 0x00022001
private const val GLFW_NO_API = 0

/**
 * Reusable desktop GLFW host for a Vulkan-backed [AwakeGame].
 *
 * Consumers still own authored concerns such as input polling, debug channels, and which
 * [VulkanGameApplication] instance to run. This helper only centralizes the window +
 * frame-loop boilerplate every Vulkan desktop sample would otherwise copy.
 */
fun runVulkanDesktopGame(
    game: AwakeGame,
    applicationFactory: (AwakeGame) -> VulkanGameApplication,
    pollInput: (window: Long) -> Unit = ::pollGlfwInput,
    beforeFrame: () -> Unit = {},
    afterLoop: () -> Unit = {}
) {
    runVulkanDesktopGame(
        game = game,
        application = applicationFactory(game),
        pollInput = pollInput,
        beforeFrame = beforeFrame,
        afterLoop = afterLoop
    )
}

fun runVulkanDesktopGame(
    game: AwakeGame,
    application: VulkanGameApplication,
    pollInput: (window: Long) -> Unit = ::pollGlfwInput,
    beforeFrame: () -> Unit = {},
    afterLoop: () -> Unit = {}
) {
    check(game.windowConfig.backend == GameWindowBackend.VULKAN) {
        "Desktop Vulkan host requires a Vulkan backend, found ${game.windowConfig.backend}."
    }
    check(VulkanWindow.glfwInit()) { "glfwInit failed" }
    VulkanWindow.glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API)
    val window = VulkanWindow.glfwCreateWindow(
        game.windowConfig.width,
        game.windowConfig.height,
        game.windowConfig.title
    )
    check(window != 0L) { "glfwCreateWindow returned null" }
    VulkanWindow.glfwSetScrollCallback(window)

    try {
        syncUiDensity(window)
        application.create(window)
        while (!VulkanWindow.glfwWindowShouldClose(window)) {
            VulkanWindow.glfwPollEvents()
            pollInput(window)
            syncUiDensity(window)
            beforeFrame()
            DesktopGameLoop.startLoop { deltaTime ->
                application.update(deltaTime.toFloat())
            }
        }
    } finally {
        afterLoop()
        application.dispose()
    }
}

private fun syncUiDensity(window: Long) {
    val windowWidth = VulkanWindow.glfwGetWindowWidth(window)
    val framebufferWidth = VulkanWindow.glfwGetFramebufferWidth(window)
    UiDensity.scale = if (windowWidth > 0 && framebufferWidth > 0) {
        framebufferWidth.toFloat() / windowWidth.toFloat()
    } else {
        1f
    }
}
