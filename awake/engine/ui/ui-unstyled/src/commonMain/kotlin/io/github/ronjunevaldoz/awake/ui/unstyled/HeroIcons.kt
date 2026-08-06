// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled

import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.uiImageVector

/** [Heroicons](https://heroicons.com) vector paths, ported by hand into [UiImageVector]'s
 * `path { moveTo/lineTo/close }` builder -- no SVG-file import pipeline exists in this engine
 * (see [UiImageVector]'s own doc comment), so each glyph here is transcribed from Heroicons'
 * published SVG path data, not generated. Grows one glyph at a time, on demand -- this is not
 * meant to become a full icon-font port.
 *
 * Nested one object per real Heroicons style+size tier, not a flat glyph list -- Heroicons ships
 * genuinely different path data at each size (24px solid, 20px "mini" solid, 16px "micro"
 * solid); a smaller tier simplifies detail for legibility, it is NOT the same path rescaled, so
 * there is no single correct [UiImageVector] per glyph name to hang a flat property off of.
 * [Solid20Mini] is the only tier transcribed so far (the collapsible chevron's real size) --
 * add [Solid24]/[Solid16Micro] objects here (matching this same nesting) the day an icon
 * actually needs one, not ahead of that need.
 *
 * No `Outline` tier exists, and can't yet: [UiImageVector]/
 * [io.github.ronjunevaldoz.awake.ui.unstyled.components.icon] only fill paths -- no stroke-width
 * primitive to render Heroicons' outline variant with. An outline glyph can only be approximated
 * today by hand-building a thin filled ribbon per icon (what [Solid20Mini.chevronDown] already
 * does, coincidentally, since Heroicons' own *solid mini* chevron already looks like a thin
 * ribbon rather than a filled triangle) -- that is real per-icon transcription work, not a flag.
 *
 * A generic, unbranded home for icon vector data -- [io.github.ronjunevaldoz.awake.ui.designsystem
 * .components.ShadcnIcons] is the shadcn-facing, *overridable* pointer registry that defaults to
 * these but doesn't have to stay pinned to them; a caller wanting a different icon set entirely
 * (a different open-source library, or hand-drawn brand glyphs) reassigns `ShadcnIcons.xxx`
 * without this object needing to know or care. */
object HeroIcons {
    /** Heroicons' `solid/20/solid` tier ("mini") -- the size real shadcn/ui's own chrome
     * (dropdown/collapsible chevrons, small inline affordances) actually uses. */
    object Solid20Mini {
        /** `ChevronDownIcon` -- a thin filled chevron ribbon, not a solid triangle. Real SVG
         * path: `M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.938a.75.75 0 111.08
         * 1.04l-4.25 4.5a.75.75 0 01-1.08 0l-4.25-4.5a.75.75 0 01.02-1.06z` (20x20 viewBox),
         * with its four tiny (0.75-radius) corner-rounding arcs collapsed to their
         * straight-line endpoints -- at a ~14-16px icon size those arcs round well under a
         * pixel, so the sharp-cornered hexagon this produces is visually indistinguishable
         * from the real rounded glyph. */
        val chevronDown: UiImageVector = uiImageVector(
            defaultWidth = 16f.dp,
            defaultHeight = 16f.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ) {
            path {
                moveTo(4f, 4.8f)
                lineTo(8f, 8.8f)
                lineTo(12f, 4.8f)
                lineTo(13.2f, 6f)
                lineTo(8f, 11.2f)
                lineTo(2.8f, 6f)
                close()
            }
        }

        val chevronUp: UiImageVector = uiImageVector(
            defaultWidth = 16f.dp,
            defaultHeight = 16f.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ) {
            path {
                moveTo(4f, 11.2f)
                lineTo(8f, 7.2f)
                lineTo(12f, 11.2f)
                lineTo(13.2f, 10f)
                lineTo(8f, 4.8f)
                lineTo(2.8f, 10f)
                close()
            }
        }
    }
}
