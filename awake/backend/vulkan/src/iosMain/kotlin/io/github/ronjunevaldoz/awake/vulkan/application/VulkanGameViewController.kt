// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.ronjunevaldoz.awake.vulkan.application

import io.github.ronjunevaldoz.awake.vulkan.VulkanMetalView
import platform.UIKit.UIScreen
import platform.UIKit.UIViewController

/**
 * Wraps a [VulkanGameApplication] in a plain UIKit view controller.
 */
fun makeVulkanGameViewController(
    application: VulkanGameApplication
): UIViewController {
    val controller = UIViewController()
    controller.view = VulkanMetalView(
        frame = UIScreen.mainScreen.bounds,
        input = application.input,
        onCreate = { metalLayer -> application.create(metalLayer) },
        onUpdate = { delta -> application.update(delta) },
        onResize = { width, height -> application.resize(0, 0, width, height) },
        onPause = { application.pause() },
        onResume = { application.resume() }
    )
    return controller
}
