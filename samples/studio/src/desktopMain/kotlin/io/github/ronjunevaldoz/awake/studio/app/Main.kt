// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.app

import io.github.ronjunevaldoz.awake.engine.app.dsl.requireService
import io.github.ronjunevaldoz.awake.scene.runtime.SceneAppLifecycleRuntime
import io.github.ronjunevaldoz.awake.vulkan.application.runVulkanDesktopGame

fun main() {
    val game = studioGame()
    // Without this, every hover-driven cursor request (the workspace resize handles) is
    // recorded by ui-core and then dropped: runVulkanDesktopGame's `cursor` defaults to null,
    // which skips the platform call entirely. See SceneGameRuntime.cursor.
    val runtime = game.requireService<SceneAppLifecycleRuntime>()
    runVulkanDesktopGame(
        game = game,
        applicationFactory = ::createStudioVulkanApplication,
        cursor = { runtime.cursor },
    )
}
