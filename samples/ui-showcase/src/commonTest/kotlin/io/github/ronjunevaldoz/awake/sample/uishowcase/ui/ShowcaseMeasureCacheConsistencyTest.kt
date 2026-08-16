// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.context.UiWeightCacheConsistencyCheck
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Arms the stale-cacheKey detector and drives the shell's cached sidebar subtrees through their
 * real mutations -- collapsing and re-expanding a category -- so every cache hit re-runs the
 * trial and throws if a supplied key failed to change when the measured content did. Guards the
 * keys wired for the wasm 1 fps fix (sidebar menus keyed by category, collapsible shells keyed
 * by expanded).
 */
class ShowcaseMeasureCacheConsistencyTest {

    @Test
    fun sidebarCacheKeysStayConsistentThroughCollapseAndExpand() {
        UiWeightCacheConsistencyCheck.enabled = true
        try {
            showcaseTestSession(width = 400f, height = 1600f, theme = shadcnThemeValues(dark = false)) {
                fun shellFrame(x: Float = -100f, y: Float = -100f, down: Boolean = false) =
                    frame(x = x, y = y, down = down) {
                        column(modifier = Modifier.fillMaxHeight()) {
                            drawUiShowcaseSidebar(compact = false)
                        }
                    }

                repeat(3) { shellFrame() }
                val trigger = shellFrame().bounds("ui-showcase-sidebar-category-Inputs.trigger")
                val tx = trigger.x + trigger.width / 2f
                val ty = trigger.y + trigger.height / 2f

                // Collapse the Inputs category: press, release, settle.
                shellFrame(tx, ty, down = true)
                shellFrame(tx, ty, down = false)
                // 250ms tween at the default frame delta -- give it room to fully settle.
                repeat(30) { shellFrame() }
                val collapsed = shellFrame()
                assertTrue(
                    collapsed.boundsOrNull("ui-showcase-page-button") == null,
                    "collapsing Inputs must hide its page items",
                )

                // Expand again and settle -- every one of these frames runs with the stale-key
                // detector on, so a wrong cacheKey throws here instead of shipping.
                shellFrame(tx, ty, down = true)
                shellFrame(tx, ty, down = false)
                repeat(30) { shellFrame() }
                shellFrame().bounds("ui-showcase-page-button")
            }
        } finally {
            UiWeightCacheConsistencyCheck.enabled = false
        }
    }
}
