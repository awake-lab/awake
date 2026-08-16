// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnScrollArea
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** [shadcnScrollArea] composes existing scroll physics ([io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll]
 * + [io.github.ronjunevaldoz.awake.ui.rememberScrollState]) with a shadcn-shaped overlay thumb --
 * these prove the thumb only appears (and is sized/positioned right) when content overflows. */
class ShadcnScrollAreaTest {

    private fun buildFrame(contentHeightDp: Float) = renderShadcnComponent(width = 300f, height = 220f) {
            shadcnScrollArea(
                id = "scroll-area",
                modifier = Modifier.width(200f.dp).height(100f.dp),
            ) {
                spacer(Modifier.height(contentHeightDp.dp))
            }
    }

    @Test
    fun overflowingContentDrawsProportionalThumbOnRightEdge() {
        val output = buildFrame(contentHeightDp = 400f)
        val primitives = output.primitives

        val viewport = output.semantics
            .first { it.role == UiSemanticRole.ScrollPanel && it.id == "scroll-area" }
            .contentBounds!!

        val thumbs = primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>()
        assertEquals(1, thumbs.size, "expected exactly one thumb, found: $thumbs")
        val thumb = thumbs.single()

        // Height fraction ~= viewport/content (100/400 = 0.25), independent of pixel density.
        val expectedFraction = 100f / 400f
        val actualFraction = thumb.h / viewport.height
        assertTrue(
            kotlin.math.abs(actualFraction - expectedFraction) < 0.05f,
            "expected thumb height fraction ~$expectedFraction, got $actualFraction (thumb=$thumb, viewport=$viewport)",
        )

        // On the right edge: within the configured 10dp thumb width + 2dp gap of the viewport's
        // own right edge, not drifted to the left/top of the container.
        val viewportRight = viewport.x + viewport.width
        assertTrue(
            thumb.x + thumb.w <= viewportRight + 1f && thumb.x > viewportRight - 20f,
            "expected thumb flush against the right edge (viewportRight=$viewportRight), got $thumb",
        )
    }

    @Test
    fun fittingContentDrawsNoThumb() {
        val primitives = buildFrame(contentHeightDp = 50f).primitives

        val thumbs = primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>()
        assertTrue(thumbs.isEmpty(), "content that fits the viewport must draw no thumb, found: $thumbs")
    }
}
