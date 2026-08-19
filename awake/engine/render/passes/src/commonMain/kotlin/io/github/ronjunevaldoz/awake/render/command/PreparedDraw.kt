// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.command

/**
 * One draw, with every GPU handle already resolved -- what shared render-feature code iterates.
 *
 * [CommandRecorder] alone can't express a draw: it says how to issue a bind, not what to bind.
 * This is the other half of that boundary, and the same "opaque, backend-defined" rule applies to
 * every member -- each is a handle a backend already produced, passed through without inspection.
 *
 * An interface, not a data class, so each backend's own already-per-frame prepared type
 * (Vulkan's `PreparedDrawCall`) implements it directly instead of being copied into a second
 * object per draw per frame. Member names deliberately avoid the ones those types already use.
 */
interface PreparedDraw {
    val pipeline: PipelineHandle

    /** Bound at set/group 0 -- a Vulkan descriptor set or a WebGPU bind group, already written
     * with this draw's uniforms by whoever prepared it. */
    val materialBinding: MaterialBinding

    /** Bound at vertex binding 0. Null for a draw whose vertices are generated in the shader. */
    val vertexBuffer: BufferHandle?

    /** Null for a non-indexed draw -- [elementCount] is then a vertex count. */
    val indexBuffer: BufferHandle?

    /** Index count when [indexBuffer] is set, vertex count otherwise. */
    val elementCount: Int

    /** Always >= 1: a plain draw is one instance, not zero. */
    val instances: Int get() = 1

    /** Per-instance model matrices, bound at vertex binding 1. */
    val instanceVertexBuffer: BufferHandle? get() = null

    /** Per-instance joint palettes, bound at set/group 1 -- skinned instancing only. */
    val jointPaletteBinding: MaterialBinding? get() = null

    /** Per-instance color/alpha, bound at vertex binding 2 -- particles only. */
    val instanceColorBuffer: BufferHandle? get() = null

    /** Per-instance sprite frame, bound at vertex binding 3 -- particles only. */
    val instanceFrameBuffer: BufferHandle? get() = null
}
