// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.pipeline

import io.github.ronjunevaldoz.awake.render.command.PreparedDraw
import io.github.ronjunevaldoz.awake.webgpu.texture.ShadowMap
import io.ygdrasil.webgpu.GPUCommandEncoder
import io.ygdrasil.webgpu.GPULoadOp
import io.ygdrasil.webgpu.GPUStoreOp
import io.ygdrasil.webgpu.RenderPassDepthStencilAttachment
import io.ygdrasil.webgpu.RenderPassDescriptor
import io.ygdrasil.webgpu.beginRenderPass

/**
 * WebGPU directional shadow depth pre-pass.
 * Renders shadow casters into [shadowMap] before the main scene pass.
 */
class ShadowFeature(
    val shadowMap: ShadowMap,
    private val shadowRenderPipeline: ShadowRenderPipeline,
) {
    fun recordCommands(
        encoder: GPUCommandEncoder,
        draws: List<PreparedDraw>,
    ) {
        encoder.beginRenderPass(
            RenderPassDescriptor(
                colorAttachments = emptyList(),
                depthStencilAttachment = RenderPassDepthStencilAttachment(
                    view = shadowMap.depthView,
                    depthClearValue = 1.0f,
                    depthLoadOp = GPULoadOp.Clear,
                    depthStoreOp = GPUStoreOp.Store,
                ),
            ),
        ) {
            val recorder = WebGpuCommandRecorder(this)
            recorder.setScissor(0, 0, shadowMap.size, shadowMap.size)
            recorder.bindPipeline(shadowRenderPipeline.handle)

            var drawIndex = 0
            while (drawIndex < draws.size) {
                val prepared = draws[drawIndex]
                val vBuffer = prepared.vertexBuffer
                if (vBuffer != null) {
                    recorder.bindVertexBuffer(0, vBuffer)
                    val indexBuffer = prepared.indexBuffer
                    if (indexBuffer != null) {
                        recorder.bindIndexBuffer(indexBuffer)
                        recorder.bindMaterial(0, prepared.materialBinding)
                        recorder.drawIndexed(prepared.elementCount)
                    } else {
                        recorder.bindMaterial(0, prepared.materialBinding)
                        recorder.draw(prepared.elementCount)
                    }
                }
                drawIndex += 1
            }
            end()
        }
    }

    fun destroy() {
        shadowRenderPipeline.destroy()
        shadowMap.destroy()
    }
}
