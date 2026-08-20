// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.passes

import io.github.ronjunevaldoz.awake.render.command.CommandRecorder
import io.github.ronjunevaldoz.awake.render.command.MaterialBinding
import io.github.ronjunevaldoz.awake.render.command.PipelineHandle

/**
 * Procedural skybox render feature (`skybox.wgsl`), shared across Vulkan and WebGPU.
 *
 * Records a full-screen triangle draw with depth testing/writes disabled, drawing the sky
 * as the first operation inside the main 3D pass.
 */
class SharedSkyboxRenderFeature {

    /**
     * Records the skybox draw into the provided [recorder].
     *
     * @param recorder The command recorder for the active 3D render pass.
     * @param pipeline The skybox pipeline handle.
     * @param uniformBinding The material/descriptor binding holding the skybox uniform block.
     */
    fun recordCommands(
        recorder: CommandRecorder,
        pipeline: PipelineHandle,
        uniformBinding: MaterialBinding,
    ) {
        recorder.bindPipeline(pipeline)
        recorder.bindMaterial(UNIFORM_SET, uniformBinding)
        recorder.draw(FULLSCREEN_TRIANGLE_VERTICES, 1)
    }

    companion object {
        const val UNIFORM_SET = 0
        const val FULLSCREEN_TRIANGLE_VERTICES = 3
    }
}
