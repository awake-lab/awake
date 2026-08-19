// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.pipeline

import io.github.ronjunevaldoz.awake.render.renderer.skyboxUniformFloats
import io.github.ronjunevaldoz.awake.vulkan.debug.SkyboxRenderPipeline

/**
 * The procedural sky, drawn FIRST in the scene pass with depth test/write off -- see
 * [SkyboxRenderPipeline]'s own doc comment. Registered before [OpaqueRenderFeature] so scene
 * geometry still paints over it.
 *
 * Authored content, not a capability (docs/reference/render-extensibility.md): this feature
 * only exists when the app's bootstrap opted into a skybox shader set, and even then draws
 * nothing unless `Renderer.showEnvironment` is on.
 */
internal class SkyboxRenderFeature(
    private val skyboxRenderPipeline: SkyboxRenderPipeline,
) : RenderFeature {
    override val pass = RenderPassSlot.Scene

    override fun recordCommands(context: RenderFrameContext): Unit = with(context) {
        if (!showEnvironment) return
        // Written here rather than before the frame's shadow pass (as it used to be): still a
        // plain host write into this frame slot's uniform buffer, still before this command
        // buffer is submitted, and the slot's fence was already waited on at image acquire.
        // null when this frame's viewProjection can't be inverted -- nothing to draw the sky
        // from, same skip the old inline path took.
        val uniforms = skyboxUniformFloats(
            viewProjection, cameraEye, light.direction, horizonColor, zenithColor,
        ) ?: return
        skyboxRenderPipeline.writeUniforms(frameIndex, uniforms)
        skyboxRenderPipeline.draw(commandBuffer, frameIndex)
    }

    override fun destroy() {
        skyboxRenderPipeline.destroy()
    }
}
