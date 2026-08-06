// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.ui

import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkMemoryPropertyFlagBits
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.handles.BufferHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.DeviceMemoryHandle
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkIndexType
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkMemoryAllocateInfo

/**
 * A vertex+index buffer rewritten every frame -- unlike [io.github.ronjunevaldoz.awake
 * .vulkan.mesh.Mesh] (built once, DEVICE_LOCAL, uploaded via a one-time staging-buffer copy,
 * no update path), this is HOST_VISIBLE|HOST_COHERENT so [update] can write straight into it
 * each frame with no staging buffer, no re-allocation. Sized once at [maxQuads] capacity;
 * [update] only ever writes fewer bytes than that, never reallocates.
 *
 * A distinct class from `Mesh` rather than an `update()` bolted onto it: `Mesh`'s DEVICE_LOCAL
 * + one-time-copy lifecycle and this class's HOST_VISIBLE + rewrite-every-frame lifecycle are
 * different contracts serving different callers (static scene geometry vs. a UI overlay whose
 * quads move every frame) -- merging them would force one class to carry two lifecycle
 * branches neither caller needs to know about.
 */
class DynamicMesh(
    private val graphicsDevice: GraphicsDevice,
    private val maxQuads: Int,
    /** Floats per vertex -- 6 for colored quads (pos2+color4, see `ui_quad.vert`), 8 for
     * textured glyph quads (pos2+uv2+color4, see `ui_glyph.vert`). Parameterized (not a
     * fixed companion constant) so this one class serves both vertex layouts. */
    private val floatsPerVertex: Int = FLOATS_PER_VERTEX,
    private val framesInFlight: Int = 1
) {
    private val device get() = graphicsDevice.device
    private val physicalDevice get() = graphicsDevice.physicalDevice

    private val maxVertices = maxQuads * VERTICES_PER_QUAD
    private val maxIndices = maxQuads * INDICES_PER_QUAD

    private data class FrameResources(
        val vertexBuffer: BufferHandle,
        val vertexBufferMemory: DeviceMemoryHandle,
        val indexBuffer: BufferHandle,
        val indexBufferMemory: DeviceMemoryHandle,
        var drawIndexCount: Int = 0
    )

    private val frameResources: Array<FrameResources>
    private var activeFrameIndex: Int = 0

    /** How many indices this frame's [update] actually wrote -- [draw] only draws this many,
     * not the full buffer capacity. */
    val drawIndexCount: Int
        get() = frameResources[activeFrameIndex].drawIndexCount

    init {
        require(framesInFlight > 0) { "framesInFlight must be positive." }
        frameResources = Array(framesInFlight) {
            val (vBuffer, vMemory) = allocateHostVisibleBuffer(
                byteSize = (maxVertices * floatsPerVertex * Float.SIZE_BYTES).toLong(),
                usage = VkBufferUsageFlagBits.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT
            )
            val (iBuffer, iMemory) = allocateHostVisibleBuffer(
                byteSize = (maxIndices * Int.SIZE_BYTES).toLong(),
                usage = VkBufferUsageFlagBits.VK_BUFFER_USAGE_INDEX_BUFFER_BIT
            )
            FrameResources(
                vertexBuffer = BufferHandle(vBuffer),
                vertexBufferMemory = DeviceMemoryHandle(vMemory),
                indexBuffer = BufferHandle(iBuffer),
                indexBufferMemory = DeviceMemoryHandle(iMemory)
            )
        }
    }

    /** Overwrites this frame's vertex/index contents. [vertices] must be at most
     * `maxQuads * VERTICES_PER_QUAD * FLOATS_PER_VERTEX` floats -- callers build this from
     * [io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive.Quad]s (2 floats position + 4 floats
     * color per vertex).
     *
     * Called unconditionally every frame by `Renderer.drawUi` regardless of whether this
     * frame actually has any quads for this mesh (e.g. `uiGlyphMesh` when a scene's
     * `onDrawUi` never calls `UiContext.text`) -- a 0-length [indices] is a legitimate,
     * common "nothing to draw" call, not a caller bug, so it's handled here rather than
     * pushing an empty-check onto every caller. Skipping the GPU write in that case isn't
     * just an optimization: `vkMapMemory`/`vkUnmapMemory` with a 0-byte range is invalid
     * per the Vulkan spec, and MoltenVK's `vkUnmapMemory` throws `VK_ERROR_MEMORY_MAP_FAILED`
     * ("Memory is not mapped") for it, since the preceding 0-size `vkMapMemory` never
     * actually establishes a mapping. */
    fun update(vertices: FloatArray, indices: IntArray) = update(frameIndex = 0, vertices = vertices, indices = indices)

    fun update(frameIndex: Int, vertices: FloatArray, indices: IntArray) {
        val frame = resourcesFor(frameIndex)
        require(vertices.size <= maxVertices * floatsPerVertex) {
            "UI quad count exceeds DynamicMesh capacity ($maxQuads quads) -- " +
                "raise maxQuads or reduce widgets drawn this frame."
        }
        require(indices.size <= maxIndices) {
            "UI index count exceeds DynamicMesh capacity ($maxQuads quads) -- " +
                "raise maxQuads or reduce widgets drawn this frame."
        }
        activeFrameIndex = frameIndex
        frame.drawIndexCount = indices.size
        if (indices.isEmpty()) return
        VulkanBuffers.writeBufferMemoryFloats(device, frame.vertexBufferMemory.handle, 0, vertices)
        VulkanBuffers.writeBufferMemoryBytes(device, frame.indexBufferMemory.handle, 0, indices.toByteArrayLE())
    }

    fun bind(commandBuffer: Long) = bind(activeFrameIndex, commandBuffer)

    fun bind(frameIndex: Int, commandBuffer: Long) {
        val frame = resourcesFor(frameIndex)
        VulkanBuffers.vkCmdBindVertexBuffers(
            commandBuffer,
            0,
            longArrayOf(frame.vertexBuffer.handle),
            longArrayOf(0L)
        )
        VulkanBuffers.vkCmdBindIndexBuffer(
            commandBuffer,
            frame.indexBuffer.handle,
            0,
            VkIndexType.VK_INDEX_TYPE_UINT32
        )
    }

    fun draw(commandBuffer: Long) = draw(activeFrameIndex, commandBuffer)

    fun draw(frameIndex: Int, commandBuffer: Long) {
        val frame = resourcesFor(frameIndex)
        if (frame.drawIndexCount == 0) return
        VulkanBuffers.vkCmdDrawIndexed(commandBuffer, frame.drawIndexCount, 1, 0, 0, 0)
    }

    fun destroy() {
        frameResources.forEach { frame ->
            VulkanBuffers.vkDestroyBuffer(device, frame.vertexBuffer.handle)
            VulkanBuffers.vkFreeMemory(device, frame.vertexBufferMemory.handle)
            VulkanBuffers.vkDestroyBuffer(device, frame.indexBuffer.handle)
            VulkanBuffers.vkFreeMemory(device, frame.indexBufferMemory.handle)
        }
    }

    private fun resourcesFor(frameIndex: Int): FrameResources {
        require(frameIndex in frameResources.indices) {
            "DynamicMesh frame index $frameIndex is outside 0..${frameResources.lastIndex}."
        }
        return frameResources[frameIndex]
    }

    private fun allocateHostVisibleBuffer(byteSize: Long, usage: Int): Pair<Long, Long> {
        val buffer = VulkanBuffers.vkCreateBuffer(device, VkBufferCreateInfo(size = byteSize, usage = usage))
        val requirements = VulkanBuffers.vkGetBufferMemoryRequirements(device, buffer)
        val memoryTypeIndex = VulkanBuffers.findMemoryType(
            physicalDevice,
            requirements.memoryTypeBits,
            VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT or
                VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
        )
        val memory = VulkanBuffers.vkAllocateMemory(
            device,
            VkMemoryAllocateInfo(allocationSize = requirements.size, memoryTypeIndex = memoryTypeIndex)
        )
        VulkanBuffers.vkBindBufferMemory(device, buffer, memory, 0)
        return buffer to memory
    }

    private fun IntArray.toByteArrayLE(): ByteArray {
        val out = ByteArray(size * 4)
        for (i in indices) {
            val v = this[i]
            out[i * 4] = (v and 0xFF).toByte()
            out[i * 4 + 1] = ((v shr 8) and 0xFF).toByte()
            out[i * 4 + 2] = ((v shr 16) and 0xFF).toByte()
            out[i * 4 + 3] = ((v shr 24) and 0xFF).toByte()
        }
        return out
    }

    companion object {
        /** Default (colored-quad) layout: pos (vec2) + color (vec4) + transform (vec4:
         * scale.xy + pivot.xy, see `UiPrimitiveTransform`) -- see `ui_quad.vert`. */
        const val FLOATS_PER_VERTEX = 10
        const val GLYPH_FLOATS_PER_VERTEX = 12

        /** pos(vec2) + localPos(vec2) + halfSize(vec2) + radius(float) + smoothing(float) + color(vec4) +
         * transform(vec4) -- see `ui_rounded_quad.vert`. */
        const val ROUNDED_QUAD_FLOATS_PER_VERTEX = 16
        const val VERTICES_PER_QUAD = 4
        const val INDICES_PER_QUAD = 6
    }
}
