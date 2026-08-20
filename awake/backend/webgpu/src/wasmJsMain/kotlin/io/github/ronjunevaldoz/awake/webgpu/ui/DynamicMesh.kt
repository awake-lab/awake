// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.ui

import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.fastArrayBufferOf
import io.ygdrasil.webgpu.BufferDescriptor
import io.ygdrasil.webgpu.GPUBuffer
import io.ygdrasil.webgpu.GPUBufferUsage
import io.ygdrasil.webgpu.GPUIndexFormat

/**
 * A vertex+index buffer rewritten every frame -- mirrors Vulkan's `DynamicMesh` (see that
 * class's doc comment for the full rationale), but thinner: WebGPU's `queue.writeBuffer` is
 * already safe to call every frame with no staging/mapping ceremony. This class still exists
 * separately from `awake-backend-webgpu`'s own `Mesh` (not reused as-is) because `Mesh`'s
 * constructor sizes its buffer to the exact incoming array -- wrong for a buffer whose live
 * content size varies frame to frame while capacity ([maxQuads]) stays fixed.
 */
class DynamicMesh(
    private val graphicsDevice: GraphicsDevice,
    private val maxQuads: Int,
    private val floatsPerVertex: Int = FLOATS_PER_VERTEX,
) {
    private val maxVertices = maxQuads * VERTICES_PER_QUAD
    private val maxIndices = maxQuads * INDICES_PER_QUAD

    private val vertexBuffer: GPUBuffer
    private val indexBuffer: GPUBuffer

    /** How many indices this frame's [update] actually wrote -- [draw] only draws this many. */
    var drawIndexCount: Int = 0
        private set

    init {
        val device = graphicsDevice.wgpuContext.device
        vertexBuffer = device.createBuffer(
            BufferDescriptor(
                size = (maxVertices * floatsPerVertex * Float.SIZE_BYTES).toULong(),
                usage = GPUBufferUsage.Vertex or GPUBufferUsage.CopyDst,
            ),
        )
        indexBuffer = device.createBuffer(
            BufferDescriptor(
                size = (maxIndices * Int.SIZE_BYTES).toULong(),
                usage = GPUBufferUsage.Index or GPUBufferUsage.CopyDst,
            ),
        )
    }

    fun update(vertices: FloatArray, indices: IntArray) {
        require(vertices.size <= maxVertices * floatsPerVertex) {
            "UI quad count exceeds DynamicMesh capacity ($maxQuads quads) -- " +
                "raise maxQuads or reduce widgets drawn this frame."
        }
        require(indices.size <= maxIndices) {
            "UI index count exceeds DynamicMesh capacity ($maxQuads quads) -- " +
                "raise maxQuads or reduce widgets drawn this frame."
        }
        val device = graphicsDevice.wgpuContext.device
        device.queue.writeBuffer(vertexBuffer, 0uL, fastArrayBufferOf(vertices))
        device.queue.writeBuffer(indexBuffer, 0uL, fastArrayBufferOf(indices))
        drawIndexCount = indices.size
    }

    fun vertexBufferRef(): GPUBuffer = vertexBuffer
    fun indexBufferRef(): GPUBuffer = indexBuffer

    fun destroy() {
        vertexBuffer.close()
        indexBuffer.close()
    }

    companion object {
        /** pos (vec2) + color (vec4) + transform (vec4: scale.xy + pivot.xy, see
         * `UiPrimitiveTransform`) -- see `ui_quad.wgsl`'s input layout. */
        const val FLOATS_PER_VERTEX = io.github.ronjunevaldoz.awake.render.passes.ui.UiVertexLayout.FLOATS_PER_VERTEX

        /** pos (vec2) + uv (vec2) + color (vec4) + transform (vec4) -- see `ui_glyph.wgsl`'s
         * input layout. */
        const val GLYPH_FLOATS_PER_VERTEX = io.github.ronjunevaldoz.awake.render.passes.ui.UiVertexLayout.GLYPH_FLOATS_PER_VERTEX

        /** pos (vec2) + localPos (vec2) + halfSize (vec2) + radius (float) + color (vec4) +
         * transform (vec4) -- see `ui_rounded_quad.wgsl`'s input layout, mirrors Vulkan's
         * `DynamicMesh.ROUNDED_QUAD_FLOATS_PER_VERTEX`. */
        const val ROUNDED_QUAD_FLOATS_PER_VERTEX = 15
        const val VERTICES_PER_QUAD = io.github.ronjunevaldoz.awake.render.passes.ui.UiVertexLayout.VERTICES_PER_QUAD
        const val INDICES_PER_QUAD = io.github.ronjunevaldoz.awake.render.passes.ui.UiVertexLayout.INDICES_PER_QUAD
        val indexFormat = GPUIndexFormat.Uint32
    }
}
