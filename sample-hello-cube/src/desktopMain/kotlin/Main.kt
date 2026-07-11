// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0

import io.github.ronjunevaldoz.awake.core.application.DesktopGameLoop
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanWindow

/**
 * A bare GLFW window running [SampleApplication] -- same shape as `awake-demo:desktopApp`'s
 * `VulkanDesktopMain.kt`, since that's the minimal pattern any new consumer's own desktop
 * entry point would follow too. No input polling here (the sample's cube doesn't move); see
 * `VulkanDesktopMain.kt` for the input-polling version once a game needs it.
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
        DesktopGameLoop.startLoop { deltaTime ->
            app.update(deltaTime.toFloat())
        }
    }

    app.dispose()
}
