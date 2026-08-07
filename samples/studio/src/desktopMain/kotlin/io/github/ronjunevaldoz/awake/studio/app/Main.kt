// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.app

import io.github.ronjunevaldoz.awake.vulkan.application.runVulkanDesktopGame

fun main() {
    val game = studioGame()
    runVulkanDesktopGame(
        game = game,
        applicationFactory = ::createStudioVulkanApplication,
    )
}
