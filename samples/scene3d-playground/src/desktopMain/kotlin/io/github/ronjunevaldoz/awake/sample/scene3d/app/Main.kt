// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.app

import io.github.ronjunevaldoz.awake.vulkan.application.runVulkanDesktopGame

fun main() {
    val game = scene3DPlayground()
    runVulkanDesktopGame(
        game = game,
        applicationFactory = ::createScene3DPlaygroundVulkanApplication
    )
}
