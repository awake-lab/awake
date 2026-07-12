// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.android

import SampleGame
import android.app.Activity
import android.os.Bundle
import io.github.ronjunevaldoz.awake.core.graphics.VulkanView
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanGameApplication
import sampleVertexStride

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = VulkanGameApplication(
            vertexShaderResourcePath = "assets/shader/vulkan/triangle.vert.spv",
            fragmentShaderResourcePath = "assets/shader/vulkan/triangle.frag.spv",
            vertexStride = sampleVertexStride,
            game = SampleGame()
        )
        setContentView(VulkanView(this, app))
    }
}
