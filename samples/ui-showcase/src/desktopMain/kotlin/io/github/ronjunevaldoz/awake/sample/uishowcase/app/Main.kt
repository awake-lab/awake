// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.app

import io.github.ronjunevaldoz.awake.engine.app.dsl.requireService
import io.github.ronjunevaldoz.awake.engine.platformauthoring.ui.AppUiRuntime
import io.github.ronjunevaldoz.awake.vulkan.application.runVulkanDesktopGame

fun main() {
    val game = uiShowcase()
    // Without this, every hover-driven cursor request (resize handles, text fields) is recorded
    // by ui-core and then dropped on the floor: runVulkanDesktopGame's `cursor` defaults to
    // null, which skips the platform call entirely. See GameUiRuntime.cursor.
    val uiRuntime = game.requireService<AppUiRuntime>()
    runVulkanDesktopGame(
        game = game,
        applicationFactory = ::createUiShowcaseVulkanApplication,
        cursor = { uiRuntime.cursor },
    )
}
