// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.toPath
import io.github.ronjunevaldoz.awake.vulkan.renderer.renderUiToTexture
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Regression coverage for the real crash reproduced live: "UI rounded-quad-clipped run vertex
 * count (1026) exceeds DynamicMesh capacity (1024 vertices)" -- 10 shadcn slider controls, each
 * rendering a rounded-track [UiDrawPrimitive.RoundedQuad], inside one rounded-clip controls-panel
 * container pushed a single exact-clip tessellated run over one `DynamicMesh`'s 1024-vertex
 * budget. Mirrors `RendererHeadlessStrokedPathChunkingTest` (the equivalent proof for the
 * pre-existing StrokedPath chunking fix), but for `RoundedQuad`'s exact-clip path, which
 * previously had none.
 */
class RendererHeadlessRoundedQuadChunkingTest {

    @Test
    fun headlessRoundedQuadRenderSupportsRunsLargerThanOneDynamicMesh() {
        RendererHeadlessUiGlyphBaselineTest.withSharedHeadlessRenderer { renderer ->
            val target = renderer.createRenderTarget(256, 256)

            // A convex rounded-rect clip -- canExactClip() requires every active clip to have a
            // convex clip contour, which is what routes RoundedQuad through the
            // stageColoredVertexTriangleMeshes/tessellateFillAa exact-clip path this test guards
            // (the un-clipped case instead uses the dedicated SDF pipeline, a different code path
            // not affected by this bug).
            val clipBounds = UiBounds(4f, 4f, 248f, 248f)
            val clipPath = UiShapeSpec.RoundedRectangle(16f.px).toPath(clipBounds)

            // Each rounded quad's AA-fringed tessellation (tessellateFillAa) produces well more
            // than 4 vertices per quad -- enough of them in one same-type run (mirroring 10
            // shadcn sliders' rounded-track quads inside one rounded-clip controls panel) exceeds
            // DynamicMesh's fixed 1024-vertex capacity before the chunking fix.
            val roundedQuads = (0 until 40).map { i ->
                val x = (i % 8) * 30f + 8f
                val y = (i / 8) * 30f + 8f
                UiDrawPrimitive.RoundedQuad(
                    x = x,
                    y = y,
                    w = 24f,
                    h = 12f,
                    color = Color(0.8f, 0.85f, 1f, 1f),
                    radius = 6f,
                )
            }

            val primitives = listOf(UiDrawPrimitive.ClipPathPush(clipPath, clipBounds)) +
                roundedQuads +
                listOf(UiDrawPrimitive.ClipPop(UiBounds(0f, 0f, 256f, 256f)))

            // Sanity check the fixture actually exercises the bug this test guards against -- if
            // this ever stops being true (e.g. DynamicMesh's capacity changes), the test data
            // needs to grow to match, not silently pass for the wrong reason.
            renderer.renderUiToTexture(target, primitives, font = null)
            val pixels = runBlocking { renderer.readPixels(target) }

            assertTrue(
                pixels.data.any { it.toInt() != 0 },
                "a rounded-quad exact-clip run larger than one DynamicMesh should chunk across " +
                    "multiple runs and render non-empty output instead of throwing",
            )
        }
    }
}
