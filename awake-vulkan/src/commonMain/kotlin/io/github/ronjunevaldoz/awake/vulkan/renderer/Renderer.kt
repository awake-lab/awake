/*
 * Awake
 * Awake.awake-vulkan.commonMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ronjunevaldoz.awake.vulkan.renderer

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.Renderer as RenderRenderer
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager

/**
 * Phase 2.5 (Web/WebGPU, decision D7) milestone 1: `expect class` -- see
 * [io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice]'s doc comment for why. Real
 * Vulkan body (and its `DEPTH_FORMAT`/`clearColorValue`/`clearDepthValue` companion
 * constants, private implementation detail not part of this public seam) in
 * `vulkanMain/.../renderer/Renderer.kt`. WebGPU's synchronization model
 * (`GPUQueue.submit`/canvas-context, no explicit `VkFence`/`VkSemaphore`/`VkRenderPass`) has
 * no 1:1 Vulkan equivalent, so a real WebGPU actual will need its own full `draw()`
 * implementation, not a shared body -- see docs/MVP_PLAN.md's Phase 2.5 section.
 *
 * Module restructuring slice 1 (see docs/MVP_PLAN.md): implements
 * [io.github.ronjunevaldoz.awake.render.renderer.Renderer] -- see
 * [io.github.ronjunevaldoz.awake.vulkan.mesh.Mesh]'s doc comment for why this doesn't
 * change `VulkanApplication.kt`'s construction pattern.
 */
expect class Renderer(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    renderPipeline: RenderPipeline,
    commandPool: Long,
    maxFramesInFlight: Int
) : RenderRenderer {
    override fun draw(camera: Camera, drawCalls: List<DrawCall>)
    override fun destroy()
}
