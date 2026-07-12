// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
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
