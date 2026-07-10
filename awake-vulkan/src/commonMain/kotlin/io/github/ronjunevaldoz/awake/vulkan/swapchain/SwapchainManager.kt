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

package io.github.ronjunevaldoz.awake.vulkan.swapchain

import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D

/**
 * Phase 2.5 (Web/WebGPU, decision D7) milestone 1: `expect class` -- see
 * [io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice]'s doc comment for why. Real
 * Vulkan body in `vulkanMain/.../swapchain/SwapchainManager.kt`.
 */
expect class SwapchainManager(graphicsDevice: GraphicsDevice, maxFramesInFlight: Int) {
    val maxFramesInFlight: Int
    var swapChain: Long
    var extent: VkExtent2D
    var imageViews: List<Long>
    var imageFormat: VkFormat
    val imageAvailableSemaphores: LongArray
    val renderFinishedSemaphores: LongArray
    val inFlightFences: LongArray
    var currentFrame: Int

    fun create()
    fun destroy()
    fun createSyncObjects()
    fun destroySyncObjects()
}
