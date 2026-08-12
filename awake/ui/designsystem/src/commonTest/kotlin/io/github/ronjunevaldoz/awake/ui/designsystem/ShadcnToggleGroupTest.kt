// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnToggleGroup
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ShadcnToggleGroupTest {

    /**
     * The bordered container was attempted and reverted twice because it made unchecked
     * segment labels unreadable -- [toggleGroup]'s own default foreground is `mutedForeground`,
     * picked for a transparent background.
     */
    @Test
    fun uncheckedSegmentLabelsUseTheGroupForegroundNotMutedForeground() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(300f, 120f, testSnapshot())

        ui.headlessRoot().column(modifier = Modifier.fillMaxSize()) {
            shadcnToggleGroup(
                id = "group",
                options = listOf("Left", "Center", "Right"),
                selectedIndex = 0,
                modifier = Modifier.width(300f.dp).height(40f.dp),
            )
        }

        val colors = ui.currentTheme.asShadcnTheme().colors
        val labelColors = ui.finishFrame().semantics
            .filter { it.id?.endsWith(".label") == true }
            .mapNotNull { it.foregroundColor }

        assertEquals(3, labelColors.size, "expected one recorded label per segment")
        labelColors.forEach { color ->
            assertEquals(colors.foreground, color, "segment labels must use the group foreground")
            assertNotEquals(colors.mutedForeground, color, "muted on the container is unreadable")
        }
    }

    @Test
    fun containerWrapsEverySegmentWithoutCollapsingTheirWidths() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(300f, 120f, testSnapshot())

        ui.headlessRoot().column(modifier = Modifier.fillMaxSize()) {
            shadcnToggleGroup(
                id = "group",
                options = listOf("Left", "Center", "Right"),
                selectedIndex = 1,
                modifier = Modifier.width(300f.dp).height(40f.dp),
            )
        }

        val widths = ui.finishFrame().semantics
            .filter { it.id?.startsWith("group.") == true && it.id?.endsWith(".label") == false }
            .map { it.bounds.width }

        assertTrue(widths.isNotEmpty(), "expected recorded segment semantics")
        widths.forEach { assertTrue(it > 0f, "every segment must keep a positive width, was $it") }
    }

    @Test
    fun wrapHeightDoesNotExpandSegmentsToTheMeasurementSentinel() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        ui.beginFrame(300f, 120f, testSnapshot())

        ui.headlessRoot().column(modifier = Modifier.fillMaxSize()) {
            shadcnToggleGroup(
                id = "wrap-height-group",
                options = listOf("Left", "Right"),
                selectedIndex = 0,
                modifier = Modifier.width(300f.dp),
            )
        }

        val firstSegment = ui.finishFrame().semantics.first { it.id == "wrap-height-group.0" }
        assertEquals(40f, firstSegment.bounds.height, "wrap-height groups must use the toggle height")
    }
}
