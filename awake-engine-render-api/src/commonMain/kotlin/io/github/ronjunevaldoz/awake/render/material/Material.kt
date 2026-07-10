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

package io.github.ronjunevaldoz.awake.render.material

/**
 * Module restructuring slice 1 (see docs/MVP_PLAN.md): narrow interface, see
 * [io.github.ronjunevaldoz.awake.render.mesh.Mesh]'s doc comment for the rationale.
 * `createResources(texture)` is deliberately excluded -- it's only ever called from
 * backend-specific app-bootstrap code (`VulkanApplication.setupVulkan()`) on the concrete
 * type directly, never through this interface.
 */
interface Material {
    fun updateUniformBuffer(mvp: FloatArray)
    fun bind(commandBuffer: Long, pipelineLayout: Long)
    fun destroy()
}
