// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0

import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanGameApplication

/**
 * The minimal "hello, cube" sample this whole module exists to demonstrate: everything a
 * new game needs to supply on top of [VulkanGameApplication] is geometry + a scene file --
 * no `GraphicsDevice`/`SwapchainManager`/`RenderPipeline`/`Mesh`/`Material` wiring, no
 * texture (passing `texture = null` uses the base class's built-in 1x1 white placeholder).
 * A single static cube, no player/camera-follow/NavMesh -- see `awake-demo` for a full game
 * built the same way, just with more entities and game-specific systems layered on top via
 * `onSceneReady`/`onFixedUpdate`.
 */
class SampleApplication : VulkanGameApplication(
    vertexShaderResourcePath = "assets/shader/vulkan/triangle.vert.spv",
    fragmentShaderResourcePath = "assets/shader/vulkan/triangle.frag.spv",
    vertexStride = 8 * Float.SIZE_BYTES,
    meshes = mapOf("cube" to MeshGeometry(cubeVertices, cubeIndices)),
    scenePath = "scenes/sample.scene.json"
) {
    // Smallest possible proof the custom UI overlay pipeline works end to end: a toggle
    // rendered top-left over the existing cube scene (see docs/MVP_PLAN.md's custom-UI
    // decision log entry) -- not the actual model-viewer/camera-catalog feature itself.
    private var debugOverlayOn = false

    override fun onDrawUi(ui: UiContext) {
        debugOverlayOn = ui.toggle("debug-toggle", 20f, 20f, 120f, 40f, debugOverlayOn)
        val label = if (debugOverlayOn) "DEBUG: ON" else "DEBUG: OFF"
        ui.text(150f, 32f, label, floatArrayOf(1f, 1f, 1f, 1f), font)
    }

    companion object {
        // Same interleaved position(vec3) + color(vec3) + uv(vec2) layout the shared
        // triangle.vert/.frag shaders expect -- see awake-demo's VulkanApplication.kt for
        // the full rationale behind this exact vertex format/palette.
        val cubeVertices = floatArrayOf(
            -0.5f, -0.5f, -0.5f, 0f, 0f, 0f, 0f, 0f, // v0
            0.5f, -0.5f, -0.5f, 1f, 0f, 0f, 1f, 0f, // v1
            0.5f, 0.5f, -0.5f, 1f, 1f, 0f, 1f, 1f, // v2
            -0.5f, 0.5f, -0.5f, 0f, 1f, 0f, 0f, 1f, // v3
            -0.5f, -0.5f, 0.5f, 0f, 0f, 1f, 0f, 0f, // v4
            0.5f, -0.5f, 0.5f, 1f, 0f, 1f, 1f, 0f, // v5
            0.5f, 0.5f, 0.5f, 1f, 1f, 1f, 1f, 1f, // v6
            -0.5f, 0.5f, 0.5f, 0f, 1f, 1f, 0f, 1f, // v7
        )
        val cubeIndices = intArrayOf(
            0, 1, 2, 2, 3, 0, // back
            4, 5, 6, 6, 7, 4, // front
            0, 3, 7, 7, 4, 0, // left
            1, 5, 6, 6, 2, 1, // right
            0, 4, 5, 5, 1, 0, // bottom
            3, 2, 6, 6, 7, 3, // top
        )
    }
}
