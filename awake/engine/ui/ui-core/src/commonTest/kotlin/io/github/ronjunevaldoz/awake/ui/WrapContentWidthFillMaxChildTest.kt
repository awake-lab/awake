// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Regression test for the real bug report (Task #21): `shadcnCard(modifier =
 * Modifier.width(Dimension.WrapContent))` stretched to fill the available width instead of
 * hugging its content. Root cause: a `Dimension.FillMax`-width child inside a WrapContent
 * surface/column (e.g. `shadcnCard`'s divider -- `separator()` defaults to `fillMaxWidth()`)
 * resolved its own trial width against the surface's probe bound (effectively "whatever's
 * available"), and that inflated width was fed straight back into the WrapContent surface's own
 * measured width -- so the surface adopted "whatever the parent had available" the moment any
 * FillMax child appeared in its content, regardless of what the caller actually asked for.
 *
 * Fix: [io.github.ronjunevaldoz.awake.ui.context.UiContextMeasureState.record] no longer lets a
 * FillMax-width child's slot contribute to the running max-right used as the WrapContent
 * container's own measured width -- a FillMax child has no intrinsic width of its own to report.
 */
class WrapContentWidthFillMaxChildTest {
    @Test
    fun wrapContentSurfaceHugsShortFixedWidthContent() {
        val ui = UiContext()
        ui.beginFrame(1000f, 400f, testSnapshot())

        var slot: UiBounds? = null
        ui.createBox(x = 0f, y = 0f, width = 900f, height = 400f).column(
            id = "parent",
            modifier = Modifier.width(Dimension.Fixed(900f.px)).height(Dimension.WrapContent),
        ) {
            slot = surface(
                id = "card",
                modifier = Modifier.width(Dimension.WrapContent).height(Dimension.WrapContent),
            ) {
                spacer(Modifier.width(Dimension.Fixed(80f.px)).height(Dimension.Fixed(20f.px)))
            }
        }
        ui.endFrame()

        val width = requireNotNull(slot).width
        assertTrue(
            width < 200f,
            "WrapContent surface with only an 80px-wide child must hug it, not fill the 900px " +
                "parent -- got width=$width",
        )
    }

    @Test
    fun wrapContentSurfaceIgnoresFillMaxChildWhenSizingHugsFixedSiblingInstead() {
        // Mirrors shadcnCard's real shape: a divider (fillMaxWidth(), like separator()) mixed
        // with fixed-width body content. The card must hug the fixed content, not the divider.
        val ui = UiContext()
        ui.beginFrame(1000f, 400f, testSnapshot())

        var slot: UiBounds? = null
        ui.createBox(x = 0f, y = 0f, width = 900f, height = 400f).column(
            id = "parent",
            modifier = Modifier.width(Dimension.Fixed(900f.px)).height(Dimension.WrapContent),
        ) {
            slot = surface(
                id = "card",
                modifier = Modifier.width(Dimension.WrapContent).height(Dimension.WrapContent),
            ) {
                spacer(Modifier.width(Dimension.Fixed(80f.px)).height(Dimension.Fixed(20f.px)))
                // The "divider" -- explicit FillMax width, like separator()'s default.
                spacer(Modifier.width(Dimension.FillMax).height(Dimension.Fixed(2f.px)))
            }
        }
        ui.endFrame()

        val width = requireNotNull(slot).width
        assertTrue(
            width < 200f,
            "WrapContent surface must hug its 80px fixed-width child, not the fillMaxWidth() " +
                "divider mixed in alongside it -- got width=$width",
        )
    }
}
