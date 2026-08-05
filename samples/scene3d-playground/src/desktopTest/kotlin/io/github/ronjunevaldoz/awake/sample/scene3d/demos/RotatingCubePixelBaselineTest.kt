// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.render.material.Material as RenderMaterial
import io.github.ronjunevaldoz.awake.render.mesh.Mesh as RenderMesh
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.testing.comparePixels
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
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Guards against a repeat of a real, previously-shipped regression: [rotatingCubeGeometry]'s 24
 * face-duplicated vertices/exact per-face normals (see that val's own doc comment) replaced an
 * earlier 8-shared-corner version whose "closest-diagonal-direction" normals made every flat
 * cube face look domed/warped -- a purely visual bug no compile-time check catches. Manual
 * screenshot review is what actually caught it, but AppleScript automation can't reliably drive
 * this project's own Vulkan-rendered UI in this environment (custom widget tree, not a native
 * OS control tree), and ad-hoc screenshots have grabbed the wrong window before -- so this test
 * renders the same real geometry headlessly instead, deterministically, in a JVM test process.
 *
 * Same headless construction path [RendererHeadlessPixelBaselineTest] (awake-backend-vulkan)
 * already proved out ([GraphicsDevice.createHeadless]/[SwapchainManager.createHeadless] -- no
 * GLFW window, no `VkSurfaceKHR`, no swapchain, ever created) -- this test is deliberately NOT
 * built on top of the real `SceneGameRuntime`/ECS/`RenderSystem` path [RotatingCubeDemo] itself
 * runs on in the app, even though that would exercise more of the demo's own glue code. Two
 * reasons: (1) `SceneGameRuntime`'s public construction path is the `game { }`/`scene { }` DSL
 * (see `Scene3DPlaygroundUiTest`, this module's own commonTest), which drives a full
 * `Game.render()` frame -- UI pass, fixed-timestep simulation pump, `RenderSystem` -- ending in
 * `Renderer.draw()` (a present to the swapchain), not `renderToTexture`/`readPixels`; a headless
 * swapchain has no window to present into, and nothing in that path exposes an offscreen
 * readback the way `SceneGameRuntime.readback()` does *outside* the normal render loop. Threading
 * a real GPU `Renderer` through that whole DSL just to reach for a side door back out to
 * `readback()` is a lot of moving parts for what's fundamentally still "render this exact mesh
 * from this exact camera." (2) `SceneGameDslTest` (awake-scene-dsl)'s own precedent always drives
 * that DSL against a no-op recording `Renderer` test double, never a real backend -- there's no
 * existing proof the full DSL round-trip is even exercised against a real GPU anywhere in this
 * repo yet. So: reuse the *real* [rotatingCubeGeometry] (this demo's actual mesh data, not a
 * hand-copied duplicate) and the real [OrbitCameraController]/[ManualTimeController] (this
 * demo's actual camera/spin logic), but drive them straight against the raw [Renderer] API the
 * same way [RendererHeadlessPixelBaselineTest] does -- smaller, more maintainable diff, and it
 * still catches the actual regression class this test exists for (bad per-vertex normals on
 * this exact mesh).
 *
 * The camera intentionally does NOT use [RotatingCubeDemo]'s own on-screen-default
 * `OrbitCameraController(zoom = 15f, ...)` framing: at that distance (with the default 45-degree
 * FOV) the unit cube covers only ~8% of the frame's height -- fine for a live, freely-orbitable
 * viewport a person can zoom into, useless for a fixed 128x128 pixel-diff baseline where the
 * warping bug needs to be visible in a meaningful number of pixels. [TEST_ZOOM]/[TEST_ORBIT_DEG]/
 * [TEST_PITCH_DEG] are tuned instead to frame three cube faces at a healthy size, still going
 * through the real [OrbitCameraController.computeCamera] codepath (not a hand-built [Camera]).
 *
 * The baseline (`src/desktopTest/resources/baselines/rotating-cube-headless.rgba`) was generated
 * by a one-time run of this exact test/render path on real hardware (Vulkan-over-MoltenVK,
 * macOS) and committed alongside it, same as [RendererHeadlessPixelBaselineTest]'s own baseline.
 */
private suspend fun loadShaderPair(vertexPath: String, fragmentPath: String): ShaderPair =
    ShaderPair(readResourceBytes(vertexPath), readResourceBytes(fragmentPath))

/** Same encode [RendererHeadlessPixelBaselineTest.writeRgbaPng] uses -- duplicated here for the
 * same reason that one is: test-only, no shared module both tests could pull it from without
 * restructuring for a debug convenience. */
private fun writeRgbaPng(pixels: ByteArray, width: Int, height: Int, file: File) {
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    var offset = 0
    for (y in 0 until height) {
        for (x in 0 until width) {
            val r = pixels[offset].toInt() and 0xFF
            val g = pixels[offset + 1].toInt() and 0xFF
            val b = pixels[offset + 2].toInt() and 0xFF
            val a = pixels[offset + 3].toInt() and 0xFF
            image.setRGB(x, y, (a shl 24) or (r shl 16) or (g shl 8) or b)
            offset += 4
        }
    }
    ImageIO.write(image, "png", file)
}

/** One (hours, baseline resource, failure-dir name) case rendered by
 * [RotatingCubePixelBaselineTest.headlessRotatingCubeRenderMatchesBaselines]. */
private data class CubeFrameCase(val hours: Float, val baselineResourcePath: String, val failureDirName: String)

class RotatingCubePixelBaselineTest {

    /** Both known frames in one test, sharing a single headless [GraphicsDevice]/[Renderer]
     * rather than one `@Test` per frame -- creating a second headless [GraphicsDevice] in the
     * same JVM process (two `@Test` methods, same class, same test worker) threw
     * `VK_ERROR_EXTENSION_NOT_PRESENT` on the second `createHeadless()` call, confirmed the hard
     * way: isolating that method alone (`--tests` filtered to just it) got past device creation
     * fine, so it's cross-instance native/global Vulkan loader state colliding within one
     * process, not a bug in either case's own render logic. One shared device sidesteps it
     * instead of chasing a fix in [GraphicsDevice]'s own teardown.
     *
     * - `hours = 0f`: front-on framing, the original regression case (bad per-vertex normals
     *   doming a flat face -- see [rotatingCubeGeometry]'s own doc comment).
     * - `hours = 12.5f` (187.5 degrees, a corner-ish view -- two faces visible, meeting at a
     *   near-vertical edge): a screen recording of the live app at roughly this angle showed
     *   what looked like a curved top edge, reported as a possible new warp regression.
     *   Investigated and ruled out -- a lossless screenshot at the same angle, pixel-scanned
     *   column by column, showed two clean straight edges meeting at a sharp corner. The curve
     *   only existed in the H.264-compressed screen recording (codec ringing on a fast-moving,
     *   high-contrast diagonal edge at a low bitrate), not in the actual render. No code changed
     *   as a result. This case exists so a REAL warp at this specific angle -- unlike the false
     *   alarm that prompted it -- doesn't go unnoticed just because the `hours = 0f` case above
     *   never rotates the cube at all. */
    @Test
    fun headlessRotatingCubeRenderMatchesBaselines() {
        val graphicsDevice = GraphicsDevice()
        graphicsDevice.createHeadless()
        val swapchainManager = SwapchainManager(graphicsDevice, MAX_FRAMES_IN_FLIGHT)
        swapchainManager.createHeadless(TARGET_SIZE, TARGET_SIZE)
        val pipelineLayoutMaterial = Material(graphicsDevice)
        val renderPipeline = RenderPipeline(
            graphicsDevice,
            swapchainManager,
            pipelineLayoutMaterial.descriptorSetLayout,
            // This sample's own triangle shader (PositionNormalColor / Lambertian diffuse from
            // commonMain/shaders/triangle.wgsl), NOT awake-backend-vulkan's generic
            // PositionColorUv one -- same .spv bytes the real app loads from appMain's compiled
            // shader output, mirrored into this module's own desktopTest/resources (see this
            // file's own doc comment for why that's a copy, not a source-set dependency).
            runBlocking { loadShaderPair("assets/shader/vulkan/triangle.vert.spv", "assets/shader/vulkan/triangle.frag.spv") },
            VertexFormat.PositionNormalColor,
            // NOT RenderPipeline's own "main" default -- this .spv came from naga (WGSL ->
            // SPIR-V, see awake.shader-pipeline-convention's vertexEntryPoint/
            // fragmentEntryPoint conventions and gameShaderSet's matching "vertexMain"/
            // "fragmentMain" defaults), which preserves the WGSL function names as the SPIR-V
            // entry point names instead of renaming to "main". Using the default here fails
            // pipeline creation with VK_ERROR_INITIALIZATION_FAILED (no entry point named
            // "main" in the module) -- confirmed the hard way getting this test running.
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
            val target = renderer.createRenderTarget(TARGET_SIZE, TARGET_SIZE)
            val createdMesh = renderer.createMesh(rotatingCubeGeometry).also { mesh = it }
            val createdMaterial = renderer.createMaterial().also { material = it }

            for (case in CASES) {
                // Frozen at this case's hours (autoPlay off) -- going through the real
                // ManualTimeController (rather than skipping straight to "rotateY(angle)")
                // documents intent: this is the demo's own real time/spin driver, deliberately
                // parked at a reproducible frame, not a hand-picked rotation this test invented.
                val timeController = ManualTimeController().apply { autoPlay = false; hours = case.hours }
                val spinRadians = timeController.hours * HOURS_TO_DEGREES * DEGREES_TO_RADIANS
                // Mirrors RotatingCubeDemo.cubeModelMatrix()/CUBE_REST_HEIGHT exactly (both
                // private to that object, so re-stated here rather than imported) -- rests the
                // cube's bottom face on y=0 instead of sinking it half beneath the reference grid.
                val cubeModel = Mat4().translate(0f, CUBE_REST_HEIGHT, 0f).rotateY(spinRadians)
                val cubeWorldPosition = Vec3(0f, CUBE_REST_HEIGHT, 0f)

                val camera = OrbitCameraController(zoom = TEST_ZOOM, zoomMin = 2f, zoomMax = 15f).apply {
                    orbitDegrees = TEST_ORBIT_DEG
                    pitchDegrees = TEST_PITCH_DEG
                    elevation = CUBE_REST_HEIGHT
                }.computeCamera(cubeWorldPosition)

                renderer.renderToTexture(target, camera, listOf(DrawCall(createdMesh, createdMaterial, cubeModel)))
                val pixels = runBlocking { renderer.readPixels(target) }

                val baseline = runBlocking { readResourceBytes(case.baselineResourcePath) }
                val result = comparePixels(pixels.data, baseline)
                if (!result.matches) {
                    // Same "dump both PNGs" convenience RendererHeadlessPixelBaselineTest's own
                    // failure branch uses -- findable from the assertion message without digging
                    // through Gradle's build directory structure.
                    val failureDir = File("build/test-failures/${case.failureDirName}").apply { mkdirs() }
                    val actualPngFile = File(failureDir, "actual.png")
                    val baselinePngFile = File(failureDir, "baseline.png")
                    writeRgbaPng(pixels.data, TARGET_SIZE, TARGET_SIZE, actualPngFile)
                    writeRgbaPng(baseline, TARGET_SIZE, TARGET_SIZE, baselinePngFile)
                    assertTrue(
                        false,
                        "Headless rotating-cube render (hours=${case.hours}) diverged from baseline: " +
                            "${result.diffPixelCount} pixels differ (max channel diff ${result.maxChannelDiff}). " +
                            "Compare ${actualPngFile.absolutePath} against ${baselinePngFile.absolutePath}. " +
                            "Re-record ${case.baselineResourcePath} if this divergence is an intentional " +
                            "rendering change."
                    )
                }
            }
        } finally {
            // Same teardown order/reasoning as RendererHeadlessPixelBaselineTest's own finally
            // block -- see that test for why pipelineLayoutMaterial only has its descriptor set
            // layout destroyed, not the full Material.
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

    private companion object {
        const val TARGET_SIZE = 128
        const val MAX_FRAMES_IN_FLIGHT = 1

        // See headlessRotatingCubeRenderMatchesBaselines's own doc comment for what each case
        // is and why it exists. 12.5f (187.5 degrees) matches the corner-angle investigation
        // exactly -- not a round number picked after the fact.
        val CASES = listOf(
            CubeFrameCase(hours = 0f, baselineResourcePath = "baselines/rotating-cube-headless.rgba", failureDirName = "RotatingCubePixelBaselineTest"),
            CubeFrameCase(
                hours = 12.5f,
                baselineResourcePath = "baselines/rotating-cube-headless-corner-angle.rgba",
                failureDirName = "RotatingCubePixelBaselineTest-CornerAngle"
            )
        )

        // Same constants RotatingCubeDemo/ManualTimeController use, re-stated here since
        // they're private to those types -- see this class's own doc comment for why this test
        // still goes through the real OrbitCameraController/ManualTimeController *classes*
        // rather than hand-rolling their math too.
        const val CUBE_REST_HEIGHT = 0.5f
        const val HOURS_TO_DEGREES = 360f / 24f
        val DEGREES_TO_RADIANS = (kotlin.math.PI / 180.0).toFloat()

        // Tuned framing -- see this class's own doc comment for why these differ from
        // RotatingCubeDemo's on-screen defaults (zoom=15, orbit=0, pitch=0).
        const val TEST_ZOOM = 3f
        const val TEST_ORBIT_DEG = 35f
        const val TEST_PITCH_DEG = 20f
    }
}
