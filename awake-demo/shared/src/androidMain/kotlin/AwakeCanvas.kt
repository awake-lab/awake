// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.ronjunevaldoz.awake.core.graphics.Application
import io.github.ronjunevaldoz.awake.core.graphics.VulkanView
import io.github.ronjunevaldoz.awake.core.graphics.opengl.AndroidRenderer
import io.github.ronjunevaldoz.awake.core.graphics.opengl.OpenGLView

@Composable
actual fun AwakeCanvas(
    modifier: Modifier,
    renderer: Application,
    vulkan: Boolean
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            if (vulkan) {
                VulkanView(context, renderer)
            } else {
                OpenGLView(context, AndroidRenderer(renderer))
            }
        }
    )
}