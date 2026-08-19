// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.material

import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.fastArrayBufferOf
import io.github.ronjunevaldoz.awake.webgpu.pipeline.WebGpuBindGroupHandle
import io.github.ronjunevaldoz.awake.webgpu.texture.Texture
import io.ygdrasil.webgpu.BindGroupDescriptor
import io.ygdrasil.webgpu.BindGroupEntry
import io.ygdrasil.webgpu.BufferBinding
import io.ygdrasil.webgpu.BufferDescriptor
import io.ygdrasil.webgpu.GPUBindGroup
import io.ygdrasil.webgpu.GPUBuffer
import io.ygdrasil.webgpu.GPUBufferUsage
import io.ygdrasil.webgpu.GPURenderPipeline
import io.ygdrasil.webgpu.GPUSampler
import io.ygdrasil.webgpu.GPUTextureView
import io.github.ronjunevaldoz.awake.render.material.Material as RenderMaterial

/**
 * Two unrelated cases share this type, matching the shared `Renderer.createMaterial` contract:
 * a UI-compositing material ([createResourcesFromRenderTarget], sampled by
 * [io.github.ronjunevaldoz.awake.webgpu.ui.UiTextureRenderPipeline], which owns its own bind
 * groups) and a 3D `DrawCall.material` with a real base-color texture ([createResources]),
 * which owns the uniform buffer + bind group `textured.wgsl` declares.
 *
 * Unlike Vulkan's descriptor set (host-side writable, so one set can be rewritten per draw),
 * a `GPUBindGroup` is immutable once created -- so [bindGroup] is built once, lazily, against
 * the pipeline this material is first drawn through.
 */
class Material(graphicsDevice: GraphicsDevice, private val uniformFloatCount: Int = 16) : RenderMaterial {
    private val device = graphicsDevice.wgpuContext.device

    var previewTextureView: GPUTextureView? = null
        private set
    var previewSampler: GPUSampler? = null
        private set

    private var texture: Texture? = null
    private var pbrTextures: List<Texture> = emptyList()
    private var uniformBuffer: GPUBuffer? = null
    private var bindGroup: GPUBindGroup? = null

    /** Scoped to the UI-compositing case -- see this class's own doc comment. */
    fun createResourcesFromRenderTarget(textureView: GPUTextureView, sampler: GPUSampler) {
        previewTextureView = textureView
        previewSampler = sampler
    }

    /** The 3D `DrawCall.material` case: keeps [texture] plus [pbr] (metallicRoughness,
     * normal, occlusion, emissive -- in `textured.wgsl`'s binding 5-8 order, each a neutral
     * 1x1 stand-in when the material has no such map) and allocates this material's own
     * uniform buffer sized by [uniformFloatCount]. The bind group itself waits for
     * [bindGroupFor]: its layout comes from the pipeline, which this class never sees. */
    fun createResources(texture: Texture, pbr: List<Texture>) {
        this.texture = texture
        pbrTextures = pbr
        uniformBuffer = device.createBuffer(
            BufferDescriptor(
                size = (uniformFloatCount * Float.SIZE_BYTES).toULong(),
                usage = GPUBufferUsage.Uniform or GPUBufferUsage.CopyDst,
            ),
        )
    }

    /** True once [createResources] has run -- a `PositionNormalColorUv` draw call whose
     * material has no texture can't be drawn through `textured.wgsl` at all. */
    val hasTexture: Boolean get() = texture != null

    /** Builds (once) this material's `textured.wgsl` bind group -- uniform, base-color view,
     * sampler, then the four PBR views at 5-8 (all sampled through the base-color sampler,
     * matching that shader) -- against [pipeline]'s group-0 layout, then reuses it every frame.
     * ponytail: cached for the first pipeline only; make it a per-pipeline map if one material
     * ever gets drawn through two pipelines (e.g. a wireframe variant of the textured one). */
    fun bindGroupFor(pipeline: GPURenderPipeline): GPUBindGroup = bindGroup ?: device.createBindGroup(
        BindGroupDescriptor(
            layout = pipeline.getBindGroupLayout(0u),
            entries = listOf(
                BindGroupEntry(binding = 0u, resource = BufferBinding(buffer = requireUniformBuffer())),
                BindGroupEntry(binding = 1u, resource = requireTexture().view),
                BindGroupEntry(binding = 2u, resource = requireTexture().sampler),
            ) + pbrTextures.mapIndexed { index, pbrTexture ->
                BindGroupEntry(binding = (PBR_FIRST_BINDING + index).toUInt(), resource = pbrTexture.view)
            },
        ),
    ).also { bindGroup = it }

    /** [bindGroupFor] as the shared render layer's opaque handle, cached the same way. */
    fun bindingFor(pipeline: GPURenderPipeline): WebGpuBindGroupHandle =
        bindGroupHandle ?: WebGpuBindGroupHandle(bindGroupFor(pipeline)).also { bindGroupHandle = it }

    private var bindGroupHandle: WebGpuBindGroupHandle? = null

    /** Writes `textured.wgsl`'s `Uniforms.mvp`. Like every other `queue.writeBuffer` on this
     * backend this is queue-scheduled, not interleaved into the encoder -- so two draw calls
     * sharing ONE material within a frame would both see the last write (see `Renderer`'s own
     * class doc comment for the same ceiling on the shared uniform buffer). */
    override fun updateUniformBuffer(mvp: FloatArray) {
        // Same check Vulkan's Material.updateUniformBuffer has -- catches an oversized write
        // here, in Kotlin, instead of writeBuffer silently overrunning the GPU buffer (WebGPU
        // has no equivalent of Vulkan's vkMapMemory validation error to catch it for us).
        require(mvp.size <= uniformFloatCount) {
            "Uniform write of ${mvp.size} floats overflows this Material's " +
                "$uniformFloatCount-float buffer -- createMaterial(uniformFloatCount = ...) " +
                "was sized for a smaller layout than what's actually being written."
        }
        device.queue.writeBuffer(requireUniformBuffer(), 0uL, fastArrayBufferOf(mvp))
    }

    override fun bind(commandBuffer: Long, pipelineLayout: Long) {
        TODO("WebGPU bind group binding happens in Renderer.draw() directly, see docs/MVP_PLAN.md")
    }

    /** Must be safe to call unconditionally (every `Game.dispose` calls it regardless of which
     * of the two cases above the material was built for). WebGPU's views/samplers/bind groups
     * are garbage-collected by the JS runtime; only the buffer has an explicit release. The
     * [texture] itself is owned by `Renderer` (which created it), same as Vulkan. */
    override fun destroy() {
        previewTextureView = null
        previewSampler = null
        uniformBuffer?.close()
        uniformBuffer = null
        bindGroup = null
        bindGroupHandle = null
        texture = null
        pbrTextures = emptyList()
    }

    private companion object {
        /** `textured.wgsl`'s metallicRoughness binding; normal/occlusion/emissive follow it. */
        const val PBR_FIRST_BINDING = 5
    }

    private fun requireTexture(): Texture = requireNotNull(texture) {
        "Material has no texture -- was it built via createMaterial(texture = ...)?"
    }

    private fun requireUniformBuffer(): GPUBuffer = requireNotNull(uniformBuffer) {
        "Material has no uniform buffer -- was it built via createMaterial(texture = ...)?"
    }
}
