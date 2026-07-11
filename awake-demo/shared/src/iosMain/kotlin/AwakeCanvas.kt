// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import io.github.ronjunevaldoz.awake.core.graphics.Application
import io.github.ronjunevaldoz.awake.core.graphics.opengl.GameView
import io.github.ronjunevaldoz.awake.vulkan.VulkanMetalView
import platform.UIKit.UIScreen

@Composable
actual fun AwakeCanvas(
    modifier: Modifier,
    renderer: Application,
    vulkan: Boolean
) {
    if (vulkan) {
        val vulkanView = remember {
            {
                VulkanMetalView(
                    frame = UIScreen.mainScreen.bounds,
                    onCreate = { metalLayer -> renderer.create(metalLayer) },
                    onUpdate = { delta -> renderer.update(delta) },
                    onResize = { width, height -> renderer.resize(0, 0, width, height) },
                    onPause = { renderer.pause() },
                    onResume = { renderer.resume() }
                )
            }
        }
        UIKitView(
            factory = vulkanView,
            modifier = modifier,
            onRelease = { renderer.dispose() },
            interactive = true
        )
        return
    }
    val gameView = remember {
        { GameView(UIScreen.mainScreen.bounds, renderer) }
    }
    UIKitView(
        factory = gameView,
        modifier = modifier,
//        onResize = { uiView, frame ->
//            val width = CGRectGetWidth(frame)
//            val height = CGRectGetHeight(frame)
//            renderer.resize(0,0, width.toInt(), height.toInt())
//            uiView.setFrame(frame)
//        },
        onRelease = { uiView ->
            uiView.renderer.dispose()
        },
        interactive = false
    )
}