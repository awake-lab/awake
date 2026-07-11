// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import demo.VulkanApplication
import io.github.ronjunevaldoz.awake.core.AwakeContext
import io.github.ronjunevaldoz.awake.core.application.DesktopGameLoop
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanWindow

// GLFW key/mouse codes this demo cares about -- see glfw3.h. A small, hand-picked subset
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

/** Polls GLFW key/mouse state into [Input] once per frame -- called right after
 * `glfwPollEvents()`, on the same render thread (see this project's `.claude/AGENTS.md`
 * "Threading model" section), so no callback/synchronization machinery is needed. */
private fun pollDesktopInput(window: Long) {
    polledKeys.forEach { (glfwKey, key) ->
        Input.setKeyDown(key, VulkanWindow.glfwGetKey(window, glfwKey) == GLFW_PRESS)
    }
    val cursor = VulkanWindow.glfwGetCursorPos(window)
    val leftButtonDown = VulkanWindow.glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS
    Input.setPointer(down = leftButtonDown, x = cursor[0].toFloat(), y = cursor[1].toFloat())
}

/**
 * Real, visible desktop Vulkan render -- opens an actual GLFW window and runs
 * [VulkanApplication] (the same textured-cube demo verified on Android) against it via
 * MoltenVK. Run via `./gradlew :awake-demo:desktopApp:runVulkanDesktop`.
 *
 * A separate entry point from `main.kt` (the OpenGL/AWT demo) rather than replacing it --
 * Phase 1c (platform-neutral surface creation) hasn't unified the two yet, so this is the
 * GLFW-specific path proven in awake-vulkan's Round 11 verification, now wired into an
 * actual running app instead of a throwaway smoke test.
 */
fun main() {
    // DesktopGameLoop reads AwakeContext.config.fps -- this entry point never touched
    // AwakeContext before (it bypassed DesktopGameLoop entirely with a hardcoded delta),
    // so nothing had initialized it yet.
    AwakeContext.init()
    check(VulkanWindow.glfwInit()) { "glfwInit failed" }
    VulkanWindow.glfwWindowHint(0x00022001, 0) // GLFW_CLIENT_API, GLFW_NO_API
    val window = VulkanWindow.glfwCreateWindow(800, 600, "Awake Vulkan - Desktop")
    check(window != 0L) { "glfwCreateWindow returned null" }

    val app = VulkanApplication()
    app.create(window)

    // DesktopGameLoop.startLoop measures the real elapsed time and throttles to
    // AwakeContext.config.fps -- this used to call app.update(0.016f) with a hardcoded
    // delta instead, bypassing Time/GameLoop entirely (unlike the Android path, which
    // already drove AndroidGameLoop from its own render thread).
    while (!VulkanWindow.glfwWindowShouldClose(window)) {
        VulkanWindow.glfwPollEvents()
        pollDesktopInput(window)
        DesktopGameLoop.startLoop { deltaTime ->
            app.update(deltaTime.toFloat())
        }
    }

    app.dispose()
}
