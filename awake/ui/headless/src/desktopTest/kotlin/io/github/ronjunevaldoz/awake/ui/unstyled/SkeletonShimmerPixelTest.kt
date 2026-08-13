// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.testing.ui.rasterize
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.testSnapshot
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.skeleton
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Real-rendered proof (via the same CPU [rasterize] pipeline `UiSnapshotWriter.kt` uses) that
 * `skeleton(shimmer = true)` at a large corner radius actually clips its shimmer band to the
 * rounded shape instead of painting the highlight straight across the bounding rect. If the
 * pending GradientQuad/activePathClips backend threading fix (owned separately, see
 * `awake/backend/vulkan`/`awake/backend/webgpu`) hasn't landed on the *real* GPU backends yet,
 * this CPU test still passes -- `UiRasterizer.kt`'s `fillGradientRect` already threads
 * `activePathClips` through `GradientQuad` correctly, independent of that GPU-side fix.
 */
class SkeletonShimmerPixelTest {

    private val width = 120
    private val height = 60
    private val radiusDp = 28f

    private fun render(shimmer: Boolean): ByteArray {
        val ui = UiContext()
        // 36 frames * (1/60)s == 600ms == half of the shimmer's 1200ms cycle -- lands the sweep
        // phase at exactly 0.5, where the band's rect fully spans the slot's width, the case
        // where an unclipped band would visibly leak past the rounded corners. Both variants
        // walk the identical frame count/delta so the pulse-fill's own elapsed-time state
        // (unrelated to shimmer) matches between the two renders.
        val frameDeltaSeconds = 1f / 60f
        var primitives: List<io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive> = emptyList()
        repeat(36) {
            ui.beginFrame(
                width.toFloat(),
                height.toFloat(),
                testSnapshot(),
                deltaSeconds = frameDeltaSeconds,
            )
            val scope = ui.createAbsolute(x = 0f, y = 0f)
            scope.skeleton(
                id = "skeleton-shimmer-probe",
                modifier = Modifier.width(width.dp).height(height.dp),
                style = Style { shape(radiusDp.dp) },
                shimmer = shimmer,
            )
            primitives = ui.endFrame()
        }
        return primitives.rasterize(width, height, background = Color.Black)
    }

    private fun pixelAt(pixels: ByteArray, x: Int, y: Int): Color {
        val offset = (y * width + x) * 4
        return Color(
            (pixels[offset].toInt() and 0xFF) / 255f,
            (pixels[offset + 1].toInt() and 0xFF) / 255f,
            (pixels[offset + 2].toInt() and 0xFF) / 255f,
            (pixels[offset + 3].toInt() and 0xFF) / 255f,
        )
    }

    @Test
    fun shimmerBandStaysClippedToTheRoundedCornerInsteadOfLeakingIntoTheCutCorner() {
        // Top-left corner: at radius 28 on a 120x60 box, (2, 2) sits well outside the rounded
        // shape (distance from the corner's radius center (28, 28) is far greater than 28),
        // the same untouched background both with pulse-only and pulse+shimmer.
        val pulseOnly = render(shimmer = false)
        val pulseAndShimmer = render(shimmer = true)

        val cornerNoShimmer = pixelAt(pulseOnly, 2, 2)
        val cornerWithShimmer = pixelAt(pulseAndShimmer, 2, 2)

        assertEquals(
            Color.Black,
            cornerNoShimmer,
            "sanity: the cut corner outside the rounded shape must be untouched background with pulse alone",
        )
        assertEquals(
            Color.Black,
            cornerWithShimmer,
            "the shimmer band must be clipped to the rounded shape -- it must NOT paint past the cut corner " +
                "(a failure here means GradientQuad clipping regressed, not that shimmer itself is broken)",
        )
    }

    @Test
    fun shimmerBandVisiblyBrightensTheSkeletonInteriorWhenActive() {
        val pulseOnly = render(shimmer = false)
        val pulseAndShimmer = render(shimmer = true)

        // Center of the shape, well inside the rounded bounds, under the mid-sweep band.
        val centerNoShimmer = pixelAt(pulseOnly, width / 2, height / 2)
        val centerWithShimmer = pixelAt(pulseAndShimmer, width / 2, height / 2)

        val brightnessNoShimmer = centerNoShimmer.r + centerNoShimmer.g + centerNoShimmer.b
        val brightnessWithShimmer = centerWithShimmer.r + centerWithShimmer.g + centerWithShimmer.b

        assertTrue(
            brightnessWithShimmer > brightnessNoShimmer,
            "shimmer=true must visibly brighten the skeleton interior versus pulse alone: " +
                "$brightnessWithShimmer vs $brightnessNoShimmer",
        )
    }
}
