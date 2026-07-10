package io.github.ronjunevaldoz.awake.vulkan.webgpu

import io.ygdrasil.webgpu.Color
import io.ygdrasil.webgpu.CompositeAlphaMode
import io.ygdrasil.webgpu.GPULoadOp
import io.ygdrasil.webgpu.GPUStoreOp
import io.ygdrasil.webgpu.GPUTextureUsage
import io.ygdrasil.webgpu.RenderPassColorAttachment
import io.ygdrasil.webgpu.RenderPassDescriptor
import io.ygdrasil.webgpu.SurfaceConfiguration
import io.ygdrasil.webgpu.autoClosableContext
import io.ygdrasil.webgpu.beginRenderPass
import io.ygdrasil.webgpu.canvasContextRenderer
import web.html.HTMLCanvasElement

/**
 * Phase 2.5 milestone 2 spike (not the real backend): proves wgpu4k can acquire a real
 * WebGPU device against a canvas and clear it to a color, end to end -- verified 2026-07-10
 * running this exact function in a real Chromium browser via a temporary wasmJs executable
 * (removed after confirming; see docs/MVP_PLAN.md's Phase 2.5 section for the result and
 * the "context is not configured" gotcha this surfaced: canvasContextRenderer() never calls
 * Surface.configure() itself, unlike wgpu4k-toolkit's own Application.configureRenderingContext()
 * helper -- the caller has to call it, as done below).
 */
suspend fun webGpuClearColorSpike(canvas: HTMLCanvasElement) = autoClosableContext {
    val canvasContext = canvasContextRenderer(htmlCanvas = canvas)
    val wgpuContext = canvasContext.wgpuContext
    val device = wgpuContext.device
    val renderingContext = wgpuContext.renderingContext

    // getCurrentTexture() throws "context is not configured" without this -- wgpu4k-toolkit's
    // own Application.configureRenderingContext() does this same call, but
    // canvasContextRenderer() itself never does, so it's the caller's job.
    wgpuContext.surface.configure(
        SurfaceConfiguration(
            device = device,
            format = renderingContext.textureFormat,
            usage = GPUTextureUsage.RenderAttachment or GPUTextureUsage.CopySrc,
            alphaMode = CompositeAlphaMode.Opaque
        )
    )

    val encoder = device.createCommandEncoder().bind()
    val texture = renderingContext.getCurrentTexture()

    encoder.beginRenderPass(
        RenderPassDescriptor(
            colorAttachments = listOf(
                RenderPassColorAttachment(
                    view = texture.createView().bind(),
                    loadOp = GPULoadOp.Clear,
                    // Deliberately not black -- a color no default/failure state would
                    // produce, so seeing it on screen is real proof this ran.
                    clearValue = Color(0.2, 0.5, 0.9, 1.0),
                    storeOp = GPUStoreOp.Store
                )
            )
        )
    ) {
        end()
    }

    val commandBuffer = encoder.finish().bind()
    device.queue.submit(listOf(commandBuffer))
}
