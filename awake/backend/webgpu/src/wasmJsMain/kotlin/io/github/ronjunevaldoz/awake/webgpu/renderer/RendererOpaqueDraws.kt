// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.renderer

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.render.command.BufferHandle
import io.github.ronjunevaldoz.awake.render.command.MaterialBinding
import io.github.ronjunevaldoz.awake.render.command.PipelineHandle
import io.github.ronjunevaldoz.awake.render.command.PreparedDraw
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.webgpu.fastArrayBufferOf
import io.github.ronjunevaldoz.awake.webgpu.material.Material
import io.github.ronjunevaldoz.awake.webgpu.mesh.Mesh
import io.github.ronjunevaldoz.awake.webgpu.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.pipeline.WebGpuBindGroupHandle
import io.github.ronjunevaldoz.awake.webgpu.pipeline.WebGpuPipelineHandle
import io.ygdrasil.webgpu.GPUBuffer
import io.ygdrasil.webgpu.GPURenderPipeline

/**
 * The half of this backend's opaque pass that stays backend-specific: resolving each [DrawCall]
 * to a pipeline, writing its uniforms, filling its instance buffers, and naming the bind groups
 * it draws with. Recording those draws is `SharedOpaqueRenderFeature`'s job, once, for both
 * backends -- this file's output is its input.
 *
 * Runs BEFORE `beginRenderPass`, where the recording loop used to do all of this inline. Safe:
 * every write here is a `queue.writeBuffer`, which is queue-scheduled and therefore already
 * executed before the encoder this frame builds is submitted -- it never interleaved with
 * encoding in the first place (the ceiling `Renderer`'s own class doc comment describes).
 */
internal fun Renderer.prepareOpaqueDraws(
    drawCalls: List<DrawCall>,
    cameraEye: Vec3,
    viewProjection: Mat4,
    lightFloats: FloatArray,
    primary: PrimaryPipelineBinding,
): Map<PipelineHandle, List<PreparedDraw>> {
    val prepared = ArrayList<PreparedDraw>(drawCalls.size)
    var instancedIndex = 0
    var drawIndex = 0
    while (drawIndex < drawCalls.size) {
        val drawCall = drawCalls[drawIndex]
        val draw = if (drawCall.instanceModels != null) {
            prepareInstancedDraw(drawCall, instancedIndex, viewProjection, lightFloats)
                ?.also { instancedIndex += 1 }
        } else {
            prepareSingleDraw(drawCall, cameraEye, viewProjection, lightFloats, primary)
        }
        if (draw != null) prepared += draw
        drawIndex += 1
    }
    // Same stable `groupBy` the Vulkan backend already batches with, keyed by the port's pipeline
    // handle (one per RenderPipeline object, so the shared feature's identity check holds).
    return prepared.groupBy { it.pipeline }
}

/** The primary (or wireframe) pipeline plus the `Renderer`-owned uniform buffer/bind group every
 * primary-format draw shares -- resolved once per frame by `performDraw`. */
internal class PrimaryPipelineBinding(
    val pipeline: WebGpuPipelineHandle,
    val uniformBuffer: GPUBuffer,
    val binding: WebGpuBindGroupHandle,
    val wireframe: Boolean,
)

private fun Renderer.prepareSingleDraw(
    drawCall: DrawCall,
    cameraEye: Vec3,
    viewProjection: Mat4,
    lightFloats: FloatArray,
    primary: PrimaryPipelineBinding,
): PreparedDraw? {
    // A mesh whose format has neither the primary pipeline nor an additionalPipelines entry is
    // skipped rather than drawn through a pipeline that expects a different vertex layout,
    // matching Vulkan's Renderer.pipelinesByFormat skip-on-mismatch guard.
    val isPrimaryFormat = drawCall.mesh.format == primaryVertexFormat
    val extraPipeline = if (isPrimaryFormat) null else additionalPipelines[drawCall.mesh.format]
    val extraMaterial = (drawCall.material as? Material)?.takeIf { it.hasTexture }
    if (!isPrimaryFormat && (extraPipeline == null || extraMaterial == null)) {
        reportSkippedFormat(drawCall)
        return null
    }
    // Kotlin's `A * B` computes the conventional `B * A` (see Mat4.times/
    // Camera.viewProjectionMatrix's docs), matching vulkanMain's Renderer.
    val mvp = drawCall.model * viewProjection
    return if (extraPipeline != null && extraMaterial != null) {
        texturedDraw(drawCall, extraPipeline, mvp, cameraEye, lightFloats)
    } else {
        primaryDraw(drawCall, mvp, lightFloats, primary)
    }
}

/** The material owns its uniform buffer + bind group. Its Uniforms is mvp + light + model +
 * cameraPosition (textured.wgsl's PBR specular needs a world-space position and view vector) --
 * the same 44 floats vulkanMain's `prepareDrawCalls` writes for this format. Wireframe has no
 * companion pipeline per additional format, so these stay filled (same fallback as Vulkan's
 * `pipelineFor`). */
private fun Renderer.texturedDraw(
    drawCall: DrawCall,
    pipeline: RenderPipeline,
    mvp: Mat4,
    cameraEye: Vec3,
    lightFloats: FloatArray,
): PreparedDraw {
    val material = drawCall.material as Material
    material.updateUniformBuffer(
        mvp.data + lightFloats + drawCall.model.data +
            floatArrayOf(cameraEye.x, cameraEye.y, cameraEye.z, 0f) +
            pbrTexturedMaterialFloats(drawCall) + fogFloats(),
    )
    val mesh = drawCall.mesh as Mesh
    return WebGpuPreparedDraw(
        pipeline = pipeline.handle,
        materialBinding = material.bindingFor(pipeline.handle.pipeline),
        vertexBuffer = mesh.vertexBinding,
        indexBuffer = mesh.indexBinding,
        elementCount = mesh.indexCount,
    )
}

/** Wireframe only applies here: an additional pipeline has no `LineList` companion, so feeding it
 * a mesh's line index buffer would hand line indices to a `TriangleList` pipeline. */
private fun Renderer.primaryDraw(
    drawCall: DrawCall,
    mvp: Mat4,
    lightFloats: FloatArray,
    primary: PrimaryPipelineBinding,
): PreparedDraw {
    graphicsDevice.wgpuContext.device.queue.writeBuffer(
        primary.uniformBuffer,
        0uL,
        fastArrayBufferOf(mvp.data + lightFloats),
    )
    val mesh = drawCall.mesh as Mesh
    val lineIndices = primary.wireframe
    return WebGpuPreparedDraw(
        pipeline = primary.pipeline,
        materialBinding = primary.binding,
        vertexBuffer = mesh.vertexBinding,
        indexBuffer = if (lineIndices) mesh.lineIndexBinding else mesh.indexBinding,
        elementCount = if (lineIndices) mesh.lineIndexCount else mesh.indexCount,
    )
}

/** One uniform write and one instanced draw for every transform, instead of a per-draw mvp.
 * Animated instancing (skinned_instanced.wgsl) resolves against its own pipeline map and
 * additionally binds a per-instance joint-palette storage buffer at group 1; particles carry a
 * real textured material and their own camera-basis uniform block (particle.wgsl). */
private fun Renderer.prepareInstancedDraw(
    drawCall: DrawCall,
    instancedIndex: Int,
    viewProjection: Mat4,
    lightFloats: FloatArray,
): PreparedDraw? {
    val instanceModels = drawCall.instanceModels.orEmpty()
    val isParticle = drawCall.mesh.format == VertexFormat.PositionUv
    val instancedPipeline = instancedPipelineFor(drawCall, isParticle)
    val particleMaterial = (drawCall.material as? Material)?.takeIf { isParticle && it.hasTexture }
    val drawable = instancedPipeline != null && instanceModels.isNotEmpty()
    if (!drawable || (isParticle && particleMaterial == null)) {
        reportSkippedInstanced(drawCall)
        return null
    }
    val resolved = requireNotNull(instancedPipeline).handle
    val mesh = drawCall.mesh as Mesh
    return WebGpuPreparedDraw(
        pipeline = resolved,
        materialBinding = instancedMaterialBinding(
            drawCall,
            particleMaterial,
            viewProjection,
            lightFloats,
            resolved.pipeline,
        ),
        vertexBuffer = mesh.vertexBinding,
        indexBuffer = mesh.indexBinding,
        elementCount = mesh.indexCount,
        instances = instanceModels.size,
        instanceVertexBuffer = instanceBufferForRun(instancedIndex)
            .also { it.update(instanceModels) }.binding,
        jointPaletteBinding = jointPaletteBinding(drawCall, instancedIndex, resolved.pipeline),
        instanceColorBuffer = particleMaterial?.let {
            alphaInstanceBufferForRun(instancedIndex)
                .also { buffer -> buffer.update(drawCall.instanceColors.orEmpty()) }.binding
        },
        instanceFrameBuffer = particleMaterial?.let {
            frameInstanceBufferForRun(instancedIndex)
                .also { buffer -> buffer.update(drawCall.instanceFrames.orEmpty()) }.binding
        },
    )
}

private fun Renderer.instancedPipelineFor(drawCall: DrawCall, isParticle: Boolean): RenderPipeline? =
    when {
        drawCall.instanceJointPalettes != null -> skinnedInstancedPipelines[drawCall.mesh.format]
        isParticle -> particlePipelines[drawCall.mesh.format]
        else -> instancedPipelines[drawCall.mesh.format]
    }

/** Writes this instanced draw's group-0 uniform block and names the group that binds it. Three
 * cases, three separately-owned bind groups -- see `ensureInstancedUniformResources`' own doc
 * comment for why they can't be one. */
private fun Renderer.instancedMaterialBinding(
    drawCall: DrawCall,
    particleMaterial: Material?,
    viewProjection: Mat4,
    lightFloats: FloatArray,
    resolved: GPURenderPipeline,
): MaterialBinding {
    val queue = graphicsDevice.wgpuContext.device.queue
    return when {
        particleMaterial != null -> {
            // Particles bind their own material's group (uniform + texture + sampler), sized for
            // particle.wgsl's viewProjection(16) + cameraRight(4) + cameraUp(4) block, not the
            // Renderer-shared viewProjection+light one.
            particleMaterial.updateUniformBuffer(viewProjection.data + drawCall.extraUniformFloats)
            particleMaterial.bindingFor(resolved)
        }
        drawCall.instanceJointPalettes != null -> {
            ensureSkinnedInstancedUniformResources(resolved)
            queue.writeBuffer(
                skinnedInstancedUniformBuffer!!,
                0uL,
                fastArrayBufferOf(viewProjection.data + lightFloats),
            )
            skinnedInstancedUniformBinding!!
        }
        else -> {
            ensureInstancedUniformResources(resolved)
            queue.writeBuffer(
                instancedUniformBuffer!!,
                0uL,
                fastArrayBufferOf(viewProjection.data + lightFloats),
            )
            instancedUniformBinding!!
        }
    }
}

private fun Renderer.jointPaletteBinding(
    drawCall: DrawCall,
    instancedIndex: Int,
    resolved: GPURenderPipeline,
): MaterialBinding? = drawCall.instanceJointPalettes?.let { palettes ->
    skinnedInstanceBufferForRun(instancedIndex).run {
        update(palettes)
        bindingFor(resolved)
    }
}

private fun Renderer.reportSkippedFormat(drawCall: DrawCall) {
    if (!debugMode) return
    println(
        "Awake (WebGPU): DrawCall skipped -- no pipeline registered for mesh format " +
            "${drawCall.mesh.format}.",
    )
}

private fun Renderer.reportSkippedInstanced(drawCall: DrawCall) {
    if (!debugMode) return
    val skinned = if (drawCall.instanceJointPalettes != null) "skinned-" else ""
    println(
        "Awake (WebGPU): instanced DrawCall skipped -- no ${skinned}instanced pipeline " +
            "registered for mesh format ${drawCall.mesh.format}, or instanceModels was empty, " +
            "or the particle material had no texture.",
    )
}

/** This backend's [PreparedDraw]: a plain per-draw carrier, unlike Vulkan's, which already had a
 * per-frame prepared type to hang the port's members off. */
internal class WebGpuPreparedDraw(
    override val pipeline: PipelineHandle,
    override val materialBinding: MaterialBinding,
    override val vertexBuffer: BufferHandle?,
    override val indexBuffer: BufferHandle?,
    override val elementCount: Int,
    override val instances: Int = 1,
    override val instanceVertexBuffer: BufferHandle? = null,
    override val jointPaletteBinding: MaterialBinding? = null,
    override val instanceColorBuffer: BufferHandle? = null,
    override val instanceFrameBuffer: BufferHandle? = null,
) : PreparedDraw
