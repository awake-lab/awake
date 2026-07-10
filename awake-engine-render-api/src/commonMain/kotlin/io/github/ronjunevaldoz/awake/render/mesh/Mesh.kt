/*
 * Awake
 * Awake.awake-engine-render-api.commonMain
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

package io.github.ronjunevaldoz.awake.render.mesh

/**
 * Module restructuring slice 1 (see docs/MVP_PLAN.md): the backend-neutral surface
 * `RenderSystem`/`DrawCall`/`Renderer.draw()` actually need across the module boundary --
 * deliberately narrow, not a 1:1 port of every Vulkan-backend `Mesh` member. Usage analysis
 * this session confirmed no caller outside `awake-vulkan` ever reads `vertexBuffer`/
 * `indexBuffer`/`indexCount`/etc. directly; only `bind()`/`draw()` are invoked generically
 * (by a `Renderer` implementation iterating `List<DrawCall>`). `awake-vulkan`'s real
 * `expect class Mesh` implements this interface (`expect class Mesh(...) :
 * io.github.ronjunevaldoz.awake.render.mesh.Mesh`) -- an `expect` class can implement an
 * interface declared in a different module, so `VulkanApplication.kt`'s existing
 * `Mesh(graphicsDevice, ...)` construction pattern needs no changes.
 */
interface Mesh {
    fun bind(commandBuffer: Long)
    fun draw(commandBuffer: Long)
    fun destroy()
}
