// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfParser
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.render.material.Material as RenderMaterial
import io.github.ronjunevaldoz.awake.render.mesh.Mesh as RenderMesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.vulkan.commands.TransferContext
import io.github.ronjunevaldoz.awake.vulkan.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.pipeline.ShaderPair
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import kotlinx.coroutines.runBlocking
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * User report: the Duck flickers while rotating when "Normalize scale" is on
 * (`effectiveScale ~= 0.0059`) but not when it's off (`effectiveScale = 1f`), and never flickers
 * while sitting still in either mode. Two theories were tested and ruled out with real headless
 * renders before landing on the actual fix:
 *
 * 1. GPU/render nondeterminism -- rendering the exact same frozen frame twice, both modes, came
 *    back byte-for-byte identical. Not random.
 * 2. Depth-buffer precision (`near` never scaled with zoom, only `far` did) -- tightening `near`
 *    6x changed the two-consecutive-frames pixel diff by exactly zero. Not z-fighting.
 *
 * The actual mechanism: [GltfViewerDemo] used to reapply a tiny `1/modelRadius` (~0.0059) scale
 * factor to Duck's *raw* ~170-unit-radius vertex coordinates in [Mat4.scale], fresh, every single
 * frame -- combining a very large and a very tiny magnitude in the same GPU matrix multiply,
 * every frame, while the mesh rotates (so different vertices trade magnitude combinations each
 * frame -- a plausible source of frame-to-frame float-precision jitter that a single static frame
 * can't reveal, which is exactly why theory 1 above missed it). The fix bakes that `1/modelRadius`
 * factor into the mesh's own vertex buffer ONCE (see [GltfViewerDemo]'s `normalizedInterleaved`),
 * so the only scale ever combined with already-small (~1 unit) coordinates at runtime is
 * `scaleMultiplier`'s safe 0.25x-4x range -- nothing near the precision cliff the raw ~170-unit
 * coordinates sat on.
 *
 * This test is the determinism half of that investigation, kept as a permanent regression guard:
 * renders the *exact same* Duck frame -- same orbit angle, same camera, same scale mode --
 * twice in a row and requires the two renders to be pixel-identical. A future change that
 * reintroduces per-frame GPU nondeterminism (in either scale mode) fails this immediately,
 * without needing to reproduce a subtle "only while rotating" symptom by eye.
 */
class GltfViewerDeterminismTest {

    @Test
    fun duckRenderIsDeterministicNormalizedAndRaw() {
        val graphicsDevice = GraphicsDevice()
        graphicsDevice.createHeadless()
        val swapchainManager = SwapchainManager(graphicsDevice, MAX_FRAMES_IN_FLIGHT)
        swapchainManager.createHeadless(TARGET_SIZE, TARGET_SIZE)
        val pipelineLayoutMaterial = Material(graphicsDevice)
        val renderPipeline = RenderPipeline(
            graphicsDevice,
            swapchainManager,
            pipelineLayoutMaterial.descriptorSetLayout,
            runBlocking { loadShaderPair("assets/shader/vulkan/triangle.vert.spv", "assets/shader/vulkan/triangle.frag.spv") },
            VertexFormat.PositionNormalColor,
            vertexEntryPoint = "vertexMain",
            fragmentEntryPoint = "fragmentMain"
        )
        val lineRenderPipeline = LineRenderPipeline(
            graphicsDevice,
            swapchainManager,
            renderPipeline.renderPass,
            runBlocking { loadShaderPair("assets/shader/vulkan/debug_line.vert.spv", "assets/shader/vulkan/debug_line.frag.spv") }
        )
        val transferContext = TransferContext(graphicsDevice)
        val renderer = Renderer(
            graphicsDevice,
            swapchainManager,
            renderPipeline,
            null,
            lineRenderPipeline,
            transferContext,
            runBlocking { loadShaderPair("assets/shader/vulkan/ui_quad.vert.spv", "assets/shader/vulkan/ui_quad.frag.spv") },
            runBlocking { loadShaderPair("assets/shader/vulkan/ui_glyph.vert.spv", "assets/shader/vulkan/ui_glyph.frag.spv") },
            runBlocking { loadShaderPair("assets/shader/vulkan/ui_texture.vert.spv", "assets/shader/vulkan/ui_texture.frag.spv") },
            runBlocking {
                loadShaderPair("assets/shader/vulkan/ui_rounded_quad.vert.spv", "assets/shader/vulkan/ui_rounded_quad.frag.spv")
            },
            MAX_FRAMES_IN_FLIGHT
        )

        var mesh: RenderMesh? = null
        var material: RenderMaterial? = null
        try {
            // Real Duck.gltf, real GltfParser -- same load path GltfViewerDemo.preload() uses.
            val duckMesh = runBlocking { GltfParser.parse(readResourceBytes("assets/models/Duck.gltf").decodeToString()) }
            val modelRadius = boundingRadius(duckMesh.positions)

            val target = renderer.createRenderTarget(TARGET_SIZE, TARGET_SIZE)
            val geometry = MeshGeometry(
                duckMesh.toInterleavedPositionNormalColor(),
                duckMesh.indices,
                format = VertexFormat.PositionNormalColor
            )
            val createdMesh = renderer.createMesh(geometry).also { mesh = it }
            val createdMaterial = renderer.createMaterial().also { material = it }

            fun renderOnce(normalizeScale: Boolean): TextureAsset {
                val effectiveScale = if (normalizeScale) 1f / modelRadius else 1f
                val duckModel = Mat4().scale(effectiveScale, effectiveScale, effectiveScale)
                val camera = OrbitCameraController(zoom = if (normalizeScale) 6f else modelRadius * 2.5f, zoomMin = 2f, zoomMax = 15f).apply {
                    orbitDegrees = 20f
                    far = if (normalizeScale) 100f else modelRadius * 40f
                }.computeCamera(Vec3(0f, 0f, 0f))
                renderer.renderToTexture(target, camera, listOf(DrawCall(createdMesh, createdMaterial, duckModel)))
                return runBlocking { renderer.readPixels(target) }
            }

            val findings = mutableListOf<String>()
            for (normalizeScale in listOf(true, false)) {
                val first = renderOnce(normalizeScale)
                val second = renderOnce(normalizeScale)
                if (!first.data.contentEquals(second.data)) {
                    findings.add("normalizeScale=$normalizeScale: two renders of the IDENTICAL scene differ")
                }
            }

            assertEquals(
                emptyList<String>(),
                findings,
                "Duck render is non-deterministic for an unchanging scene: " + findings.joinToString("; ")
            )
        } finally {
            mesh?.destroy()
            material?.destroy()
            renderer.destroy()
            lineRenderPipeline.destroy()
            renderPipeline.destroy()
            VulkanDescriptors.vkDestroyDescriptorSetLayout(graphicsDevice.device, pipelineLayoutMaterial.descriptorSetLayout.handle)
            transferContext.destroy()
            graphicsDevice.destroy()
        }
    }

    private fun boundingRadius(positions: FloatArray): Float {
        var maxDistanceSquared = 0f
        var i = 0
        while (i < positions.size) {
            val x = positions[i]
            val y = positions[i + 1]
            val z = positions[i + 2]
            val distanceSquared = x * x + y * y + z * z
            if (distanceSquared > maxDistanceSquared) maxDistanceSquared = distanceSquared
            i += 3
        }
        return if (maxDistanceSquared > 0f) sqrt(maxDistanceSquared) else 1f
    }

    private suspend fun loadShaderPair(vertexPath: String, fragmentPath: String): ShaderPair =
        ShaderPair(readResourceBytes(vertexPath), readResourceBytes(fragmentPath))

    private companion object {
        const val TARGET_SIZE = 256
        const val MAX_FRAMES_IN_FLIGHT = 1
    }
}
