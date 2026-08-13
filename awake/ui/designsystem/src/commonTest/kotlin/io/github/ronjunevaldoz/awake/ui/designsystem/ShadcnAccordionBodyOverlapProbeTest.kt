// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.testing.ui.UiInspectionIssueKind
import io.github.ronjunevaldoz.awake.testing.ui.inspectUiFrame
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnAccordion
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.Test

/**
 * Reproduces the ui-showcase accordion "expanded item covers its neighbour" report using the
 * *real* `shadcnAccordion`/`shadcnBodyText` (the exact shape of `drawShadcnAccordionDemoPreview`)
 * rather than `animatedHeight` in isolation.
 *
 * `animatedHeight`'s own claim-vs-clip pairing was verified sound (see
 * `AnimatedHeightWrapContentOverlapTest` in ui-core: a trial-vs-real width mismatch does not
 * reproduce, even through the full row/scrollPanel/nested-WrapContent-surface chain the showcase
 * page actually uses). The real cause is one layer up: `text()`'s DSL overload defaults to
 * `UiTextWrap.None`, which sizes a label to its own full unwrapped width -- ignoring the
 * enclosing column's `Modifier.fillMaxWidth()` entirely -- unless `wrap` is explicitly set.
 * `shadcnBodyText` (what the accordion demo's body actually calls) never set it, so a body
 * sentence long enough to need wrapping instead claimed (and drew) a slot several times wider
 * than its column. The sidebar's own collapsible content is always short menu/category labels
 * that never hit this path, which is why "the same widget" looked fine there.
 */
class ShadcnAccordionBodyOverlapProbeTest {

    private val columnWidth = 280f
    private val longBodies = mapOf(
        "item-1" to "Yes. It adheres to the WAI-ARIA design pattern for accordion components.",
        "item-2" to "Yes. It comes with default styles that match the other components' aesthetic.",
    )

    @Test
    fun expandedAccordionBodyStaysWithinItsColumnAcrossASelectionSwitch() {
        ensureShadcnTestIconsInitialized()
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        val items = listOf("item-1", "item-2")
        var selectedId: String? = "item-1"

        fun frame(): List<UiDrawPrimitive> {
            ui.beginFrame(320f, 600f, UiInputState())
            ui.headlessRoot().column(modifier = Modifier.width(columnWidth.dp)) {
                shadcnAccordion(
                    items = items,
                    selectedId = selectedId,
                    onSelectId = { selectedId = it },
                    idProvider = { it },
                    titleProvider = { it },
                ) { item ->
                    shadcnText(longBodies.getValue(item))
                }
            }
            return ui.endFrame()
        }

        // Settle with item-1 open, then click item-2 (real accordion mutex: item-1 collapses,
        // item-2 expands in the same frame) and sample every frame of the transition.
        repeat(10) { frame() }
        selectedId = "item-2"
        repeat(20) { frameIndex ->
            val primitives = frame()
            val glyphs = primitives.filterIsInstance<UiDrawPrimitive.Glyph>()
            val maxGlyphRight = glyphs.maxOfOrNull { it.x + it.w } ?: 0f
            check(maxGlyphRight <= columnWidth + 0.5f) {
                "frame $frameIndex: body text glyph right edge $maxGlyphRight exceeds the " +
                    "column width $columnWidth -- text claimed a slot wider than its column " +
                    "and will visually spill into whatever sits next to it."
            }

            val inspection = inspectUiFrame(
                primitives,
                frame = UiBounds(0f, 0f, 320f, 600f),
                font = BitmapFont(),
            )
            val boundsIssues = inspection.issues.filter { it.kind != UiInspectionIssueKind.GlyphMissingFont }
            check(boundsIssues.isEmpty()) {
                "frame $frameIndex: primitives escaped their clip/frame: $boundsIssues"
            }
        }
    }
}
