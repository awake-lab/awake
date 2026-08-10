// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.app

import io.github.ronjunevaldoz.awake.engine.game.requireService
import io.github.ronjunevaldoz.awake.engine.gameauthoring.GameUiRuntime
import io.github.ronjunevaldoz.awake.vulkan.application.runVulkanDesktopGame

fun main() {
    val game = studioGame()
    // Without this, every hover-driven cursor request -- the resize handles above all -- is
    // recorded by ui-core and then dropped: runVulkanDesktopGame's `cursor` defaults to null,
    // which skips the platform call entirely. ui-showcase has always passed it; studio never
    // did, which is why its dividers showed no resize cursor even once dragging worked.
    val uiRuntime = game.requireService<GameUiRuntime>()
    runVulkanDesktopGame(
        game = game,
        applicationFactory = ::createStudioVulkanApplication,
        cursor = { uiRuntime.cursor },
    )
}
