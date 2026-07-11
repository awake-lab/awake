// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package demo

import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanGameApplication

/**
 * The MVP demo's game-specific `Application`: geometry/texture/scene data only -- the
 * generic engine bootstrap (`GraphicsDevice`/`SwapchainManager`/`RenderPipeline`/`Mesh`/
 * `Material`/`Renderer`/scene loading) now lives in [VulkanGameApplication]
 * (`awake-backend-vulkan`), see docs/MVP_PLAN.md's Decision Log ("reusable-Application gap
 * fix"). [SceneRuntimeHost] owns exactly the parts that are specific to *this* demo (player
 * movement, camera-follow, NavMesh chase AI, cube-spin, pause toggle, debug HUD text).
 */
class VulkanApplication : VulkanGameApplication(
    vertexShaderResourcePath = "assets/shader/vulkan/triangle.vert.spv",
    fragmentShaderResourcePath = "assets/shader/vulkan/triangle.frag.spv",
    vertexStride = VERTEX_STRIDE,
    meshes = mapOf(
        "cube" to MeshGeometry(cubeVertices, cubeIndices),
        "ground" to MeshGeometry(groundVertices, groundIndices)
    ),
    texture = TextureAsset(textureData, TEXTURE_WIDTH, TEXTURE_HEIGHT),
    scenePath = "scenes/mvp.scene.json"
) {
    private lateinit var sceneHost: SceneRuntimeHost

    override fun onSceneReady() {
        sceneHost = SceneRuntimeHost.create(scene, world)
    }

    override fun onFixedUpdate(delta: Float) {
        sceneHost.fixedUpdate(delta)
        super.onFixedUpdate(delta)
    }

    companion object {
        const val VERTEX_STRIDE = 8 * Float.SIZE_BYTES

        // interleaved position(vec3) + color(vec3) + uv(vec2), matching triangle.vert's
        // location 0 / location 1 / location 2 inputs. 8 unique corners of a unit cube,
        // colored with the classic RGB-cube palette (black/red/yellow/green/blue/magenta/
        // white/cyan) so every face is visually distinguishable; UVs are approximate
        // (shared corners can't have per-face-correct UVs without duplicating vertices,
        // out of scope for this MVP proof of indexed drawing + a real MVP matrix).
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

        // 12 triangles, 2 per face. cullMode is set to NONE in RenderPipeline specifically
        // so this winding order doesn't need to be outward-consistent per face -- depth
        // testing alone resolves correct occlusion.
        val cubeIndices = intArrayOf(
            0, 1, 2, 2, 3, 0, // back
            4, 5, 6, 6, 7, 4, // front
            0, 3, 7, 7, 4, 0, // left
            1, 5, 6, 6, 2, 1, // right
            0, 4, 5, 5, 1, 0, // bottom
            3, 2, 6, 6, 7, 3, // top
        )

        // MVP1a ground-plane slice (see docs/MMORPG_ROADMAP.md): a flat quad matching
        // DemoNavMeshGeometry's invisible navmesh extent (half-extent 10, awake-scene's
        // GROUND_HALF_EXTENT), so the visible floor lines up with the walkable area. Flat
        // white vertex color (not the cube's per-corner RGB gradient) so the checkerboard
        // texture reads as a plain floor rather than a color gradient; UVs scaled to 0..8 so
        // the 2x2 checkerboard tiles via the sampler's default REPEAT addressing (see
        // Texture.kt's VkSamplerCreateInfo() defaults).
        val groundVertices = floatArrayOf(
            -10f, 0f, -10f, 1f, 1f, 1f, 0f, 0f, // v0
            10f, 0f, -10f, 1f, 1f, 1f, 8f, 0f, // v1
            10f, 0f, 10f, 1f, 1f, 1f, 8f, 8f, // v2
            -10f, 0f, 10f, 1f, 1f, 1f, 0f, 8f, // v3
        )
        val groundIndices = intArrayOf(0, 2, 1, 0, 3, 2)

        // A tiny 2x2 RGBA8 checkerboard (white/black) -- proves real texture sampling
        // without needing an image file loader (out of scope for this MVP phase).
        const val TEXTURE_WIDTH = 2
        const val TEXTURE_HEIGHT = 2
        val textureData = byteArrayOf(
            // white, black
            -1, -1, -1, -1, 0, 0, 0, -1,
            // black, white
            0, 0, 0, -1, -1, -1, -1, -1,
        )
    }
}
