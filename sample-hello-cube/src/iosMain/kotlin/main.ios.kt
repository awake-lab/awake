// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

import io.github.ronjunevaldoz.awake.vulkan.VulkanMetalView
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanGameApplication
import platform.UIKit.UIScreen
import platform.UIKit.UIViewController

/**
 * First plain-`UIViewController`-hosting-`VulkanMetalView` pattern in this repo -- awake-demo
 * always goes through Compose (`ComposeUIViewController` -> `AwakeCanvas.kt`'s `UIKitView`
 * wrapping this same [VulkanMetalView]). This sample has no UI chrome, so it wires the same
 * five lifecycle lambdas directly against a plain [VulkanGameApplication] instead.
 */
fun makeSampleViewController(): UIViewController {
    val app = VulkanGameApplication(
        vertexShaderResourcePath = "assets/shader/vulkan/triangle.vert.spv",
        fragmentShaderResourcePath = "assets/shader/vulkan/triangle.frag.spv",
        vertexStride = sampleVertexStride,
        game = DemoCatalog()
    )
    val controller = UIViewController()
    controller.view = VulkanMetalView(
        frame = UIScreen.mainScreen.bounds,
        onCreate = { metalLayer -> app.create(metalLayer) },
        onUpdate = { delta -> app.update(delta) },
        onResize = { width, height -> app.resize(0, 0, width, height) },
        onPause = { app.pause() },
        onResume = { app.resume() }
    )
    return controller
}
