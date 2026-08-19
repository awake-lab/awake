// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.application

import io.github.ronjunevaldoz.awake.core.application.DesktopGameLoop
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.engine.app.dsl.AppWindowBackend
import io.github.ronjunevaldoz.awake.engine.app.lifecycle.AwakeAppLifecycle
import io.github.ronjunevaldoz.awake.ui.UiDensity
import io.github.ronjunevaldoz.awake.ui.context.UiCursor
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanWindow

private const val GLFW_CLIENT_API = 0x00022001
private const val GLFW_NO_API = 0

/**
 * Reusable desktop GLFW host for a Vulkan-backed [AwakeAppLifecycle].
 *
 * Consumers still own authored concerns such as input polling, debug channels, and which
 * [VulkanEngine] instance to run. This helper only centralizes the window +
 * frame-loop boilerplate every Vulkan desktop sample would otherwise copy.
 */
fun runVulkanDesktopGame(
    game: AwakeAppLifecycle,
    applicationFactory: (AwakeAppLifecycle) -> VulkanEngine,
    pollInput: (window: Long, input: Input) -> Unit = ::pollGlfwInput,
    beforeFrame: () -> Unit = {},
    afterLoop: () -> Unit = {},
    cursor: (() -> UiCursor)? = null,
) {
    runVulkanDesktopGame(
        game = game,
        application = applicationFactory(game),
        pollInput = pollInput,
        beforeFrame = beforeFrame,
        afterLoop = afterLoop,
        cursor = cursor,
    )
}

fun runVulkanDesktopGame(
    game: AwakeAppLifecycle,
    application: VulkanEngine,
    pollInput: (window: Long, input: Input) -> Unit = ::pollGlfwInput,
    beforeFrame: () -> Unit = {},
    afterLoop: () -> Unit = {},
    // The desktop cursor application point: the UI owns the request (see UiCursor's doc
    // comment), this loop owns the platform call. `null` (default) skips it entirely -- every
    // existing caller keeps its current zero-cursor-management behavior; a caller opts in by
    // returning its own UiContext's `finishFrame().effects.cursor` each frame.
    cursor: (() -> UiCursor)? = null,
) {
    check(game.windowConfig.backend == AppWindowBackend.VULKAN) {
        "Desktop Vulkan host requires a Vulkan backend, found ${game.windowConfig.backend}."
    }
    check(VulkanWindow.glfwInit()) { "glfwInit failed" }
    VulkanWindow.glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API)
    val window = VulkanWindow.glfwCreateWindow(
        game.windowConfig.width,
        game.windowConfig.height,
        game.windowConfig.title,
    )
    check(window != 0L) { "glfwCreateWindow returned null" }
    VulkanWindow.glfwFocusWindow(window)
    VulkanWindow.glfwSetScrollCallback(window)

    try {
        syncUiDensity(window)
        application.create(window)
        while (!VulkanWindow.glfwWindowShouldClose(window)) {
            VulkanWindow.glfwPollEvents()
            pollInput(window, game.input)
            pollGlfwTextInput(window, game.input)
            syncUiDensity(window)
            beforeFrame()
            DesktopGameLoop.startLoop { deltaTime ->
                application.update(deltaTime.toFloat())
            }
            cursor?.let { applyUiCursor(window, it()) }
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
