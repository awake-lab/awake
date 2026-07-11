// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0

import io.github.ronjunevaldoz.awake.core.application.DesktopGameLoop
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanWindow

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
 * `glfwPollEvents()`, on the same render thread (see this project's `.claude/AGENTS.md`
 * "Threading model" section), so no callback/synchronization machinery is needed.
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
}

/**
 * A bare GLFW window running [SampleApplication] -- same shape as `awake-demo:desktopApp`'s
 * (now-retired) `VulkanDesktopMain.kt`, since that's the minimal pattern any new consumer's
 * own desktop entry point would follow too. Polls keyboard/pointer input into [Input] once
 * per frame (see [pollDesktopInput]) so `SampleApplication`'s UI widgets (e.g. the
 * debug-toggle) are actually clickable.
 *
 * Unlike `VulkanDesktopMain.kt`, this doesn't call `AwakeContext.init()` -- that class lives
 * in `awake-opengl` (legacy backend) purely to mirror fps/ups into `EngineConfigHolder` for
 * old call sites; [DesktopGameLoop] already reads sensible defaults from
 * `EngineConfigHolder.config` without it, and a Vulkan-only sample has no reason to depend
 * on the OpenGL module at all.
 */
fun main() {
    check(VulkanWindow.glfwInit()) { "glfwInit failed" }
    VulkanWindow.glfwWindowHint(0x00022001, 0) // GLFW_CLIENT_API, GLFW_NO_API
    val window = VulkanWindow.glfwCreateWindow(800, 600, "Awake Sample - Hello Cube")
    check(window != 0L) { "glfwCreateWindow returned null" }

    val app = SampleApplication()
    app.create(window)

    while (!VulkanWindow.glfwWindowShouldClose(window)) {
        VulkanWindow.glfwPollEvents()
        pollDesktopInput(window)
        DesktopGameLoop.startLoop { deltaTime ->
            app.update(deltaTime.toFloat())
        }
    }

    app.dispose()
}
