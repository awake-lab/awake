// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.core.components.Name
import io.github.ronjunevaldoz.awake.studio.examples.StudioExamples
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import io.github.ronjunevaldoz.awake.testing.render.NoopRenderer
import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val FRAME_WIDTH = 1440f
private const val FRAME_HEIGHT = 900f

// The one hairline separator between the top bar and the panels group, and again between the
// panels group and the status bar (see drawStudioShellBody) -- any observed gap beyond this is
// the "floating card with a gap strip at the bottom" bug this docked layout replaced, not a
// deliberate seam.
private const val HAIRLINE_TOLERANCE = 2f

/** The resizable handle between the workspace and the dock is thicker than a hairline. */
private const val SEPARATOR_SLACK = 8f

/** Generous headroom, so this only fires on a real escape (a sentinel), not a few px of overhang. */
private const val ESCAPE_FACTOR = 2f

/** Semantic (bounds-only) coverage for the docked shell layout -- pixel/theme snapshots are
 * ui-showcase's job, not this sample's; see the design-system engineer's validation notes. */
class StudioShellLayoutTest {

    private fun renderShell(density: Float = 1f): List<UiSemanticNode> = renderUiComponent(
        width = FRAME_WIDTH,
        height = FRAME_HEIGHT,
        font = BitmapFont(),
        density = density,
    ) {
        shadcnTheme(theme = shadcnThemeValues(dark = true)) {
            drawStudioShellBody(StudioStore(), World(), NoopRenderer(), backend = "Vulkan")
        }
    }.semantics

    // The live app runs at Retina density; the shell computes its workspace height in pixels,
    // and wrapping that pixel value back as Dp re-multiplied it by UiDensity.scale -- the
    // workspace claimed twice the available height, pushing the dock handle and status bar off
    // the frame. Only reproduces with scale != 1, which every other test here leaves at 1.
    @Test
    fun workspaceStaysInsideTheShellAtRetinaDensity() {
        val semantics = renderShell(density = 2f)
        val statusBar = assertNotNull(semantics.firstOrNull { it.id == "studio-status-bar" })
        val dockHandle = assertNotNull(semantics.firstOrNull { it.id == "studio-bottom-dock-handle" })
        assertEquals(
            FRAME_HEIGHT,
            statusBar.bounds.y + statusBar.bounds.height,
            HAIRLINE_TOLERANCE,
            "status bar must stay flush with the bottom edge at density 2",
        )
        assertTrue(
            dockHandle.bounds.y + dockHandle.bounds.height < FRAME_HEIGHT,
            "dock handle must stay inside the frame at density 2 " +
                "(was at y=${dockHandle.bounds.y})",
        )
    }

    @Test
    fun panelsDockFlushToEveryFrameEdgeWithNoGapBelowTheViewport() {
        val semantics = renderShell()
        val topBar = assertNotNull(semantics.firstOrNull { it.id == "studio-top-bar" })
        val statusBar = assertNotNull(semantics.firstOrNull { it.id == "studio-status-bar" })
        val sidebar = assertNotNull(semantics.firstOrNull { it.id == "studio-panel-sidebar" })
        val viewport = assertNotNull(semantics.firstOrNull { it.id == "studio-panel-viewport" })
        val inspector = assertNotNull(semantics.firstOrNull { it.id == "studio-panel-inspector" })

        // Full-bleed: top bar touches the top edge, status bar touches the bottom edge.
        assertEquals(0f, topBar.bounds.y)
        assertEquals(FRAME_HEIGHT, statusBar.bounds.y + statusBar.bounds.height, 0.5f)

        // The Scene Browser hugs the left edge, the Inspector hugs the right edge -- both span
        // the full height of the panels row.
        assertEquals(0f, sidebar.bounds.x)
        assertEquals(FRAME_WIDTH, inspector.bounds.x + inspector.bounds.width, 0.5f)
        assertEquals(sidebar.bounds.height, inspector.bounds.height, 0.5f)
        assertEquals(sidebar.bounds.height, viewport.bounds.height, 0.5f)

        // No stray gap between the top bar and the panels: sidebar y starts right below topBar.
        assertEquals(topBar.bounds.height, sidebar.bounds.y, HAIRLINE_TOLERANCE)

        // ...and none between the workspace and the status bar. The three panels sit in the UPPER
        // panel of a vertical split, so what meets the status bar is the BOTTOM DOCK, not the
        // sidebar -- asserting the sidebar here predates the dock and measured a ~236px "gap"
        // that is simply the dock itself.
        val bottomDock = assertNotNull(semantics.firstOrNull { it.id == "studio-bottom-dock-panel" })
        assertEquals(statusBar.bounds.y, bottomDock.bounds.y + bottomDock.bounds.height, HAIRLINE_TOLERANCE)
        assertEquals(sidebar.bounds.y + sidebar.bounds.height, bottomDock.bounds.y, HAIRLINE_TOLERANCE + SEPARATOR_SLACK)
    }

    @Test
    fun selectedHierarchyRowPaintsAVisibleHighlightNotATransparentFill() {
        val world = World()
        val entity = world.create()
        world.add(entity, Name("Camera"))
        val frame = renderUiComponent(width = 400f, height = 600f, font = BitmapFont()) {
            shadcnTheme(theme = shadcnThemeValues(dark = true)) {
                drawHierarchyPanel(world, selectedEntityId = entity.id, onSelectEntity = {})
            }
        }
        val item = assertNotNull(frame.semantics.firstOrNull { it.id == "studio-hierarchy-entity-${entity.id}" })
        // Regression: shadcnButton's Ghost variant hardcodes an idle (non-hover) fill of fully
        // transparent regardless of what a caller's own Style sets, so an "active" menu item's
        // real highlight never painted while staying Ghost -- shadcnSidebarMenuItem switches to
        // Primary (which always honors the resolved background) for the active item instead.
        assertTrue((item.backgroundColor?.a ?: 0f) > 0f, "selected row background: ${item.backgroundColor}")
    }

    /** The example picker is the only way to change scene at runtime. It lived in the left dock
     * until the hierarchy took that slot, and was left orphaned -- rendered by no shell code at
     * all, so the running app was stuck on whichever example it booted into. */
    @Test
    fun topBarExposesTheExamplePicker() {
        val picker = assertNotNull(renderShell().firstOrNull { it.id == "studio-top-bar-example" })
        assertTrue(picker.bounds.height > 0f, "picker must be laid out, was ${picker.bounds}")
        assertTrue(StudioExamples.size > 1, "a picker over one example would be pointless")
    }

    /** The two floating pills, and the split between them: tools are modal, view state is not. */
    @Test
    fun theViewportCarriesAToolPillAndAViewPill() {
        val semantics = renderShell()

        assertNotNull(semantics.firstOrNull { it.id == "studio-tool-pill" }, "tool pill")
        StudioContract.Tool.entries.forEach { tool ->
            assertNotNull(
                semantics.firstOrNull { it.id == "studio-tool-${tool.name.lowercase()}" },
                "no control for $tool",
            )
        }
        assertNotNull(semantics.firstOrNull { it.id == "studio-view-pill" }, "view pill")
        assertNotNull(semantics.firstOrNull { it.id == "studio-view-mode" }, "camera mode")
        assertNotNull(semantics.firstOrNull { it.id == "studio-view-projection" }, "projection")
        assertNotNull(semantics.firstOrNull { it.id == "studio-view-wireframe" }, "wireframe")
        assertNotNull(semantics.firstOrNull { it.id == "studio-view-shadows" }, "shadows")
    }

    /**
     * Nothing in the shell may lay out past the frame it was given.
     *
     * The invariant the shell never had. A container that hands a child the measurement sentinel
     * instead of a real bound produces coordinates in the tens of thousands, and every symptom of
     * that reads as something else entirely -- a popup "not opening" when its item is really
     * sitting under a widget stretched to 100,000px, or a scrollbar on content that fits. Bounds
     * are the cheapest place to catch the whole class.
     */
    @Test
    fun nothingLaysOutBeyondTheFrame() {
        val semantics = renderShell()

        val escaped = semantics
            .filter { node ->
                val b = node.bounds
                b.y + b.height > FRAME_HEIGHT * ESCAPE_FACTOR ||
                    b.x + b.width > FRAME_WIDTH * ESCAPE_FACTOR
            }
            .sortedByDescending { it.bounds.y + it.bounds.height }

        val report = escaped.take(10).joinToString("\n") {
            "  ${it.id} role=${it.role} x=${it.bounds.x} y=${it.bounds.y} " +
                "w=${it.bounds.width} h=${it.bounds.height}"
        }
        assertTrue(
            escaped.isEmpty(),
            "${escaped.size} node(s) lay out past ${FRAME_WIDTH}x$FRAME_HEIGHT " +
                "(x${ESCAPE_FACTOR.toInt()} headroom):\n$report",
        )
    }

    @Test
    fun bottomDockExposesConsoleTimelineAndAssetsTabs() {
        val semantics = renderShell()

        // shadcnTabs keys its triggers by UiTabItem.value, not by index -- "$id.${item.value}".
        // The list overload maps each label to its own value, so these are the tab LABELS.
        assertNotNull(semantics.firstOrNull { it.id == "studio-bottom-dock.Console" })
        assertNotNull(semantics.firstOrNull { it.id == "studio-bottom-dock.Timeline" })
        assertNotNull(semantics.firstOrNull { it.id == "studio-bottom-dock.Assets" })
    }

    @Test
    fun switchingBottomDockTabsKeepsMeasuredChildrenInSync() =
        uiTestSession(
            width = FRAME_WIDTH,
            height = FRAME_HEIGHT,
            font = BitmapFont(),
        ) {
            val store = StudioStore()
            val world = World()

            fun shellFrame(x: Float = -100f, y: Float = -100f, down: Boolean = false): List<UiSemanticNode> =
                frame(x = x, y = y, down = down) {
                    shadcnTheme(theme = shadcnThemeValues(dark = true)) {
                        drawStudioShellBody(store, world, NoopRenderer(), backend = "Vulkan")
                    }
                }.semantics

            fun click(tabId: String) {
                val tab = assertNotNull(shellFrame().firstOrNull { it.id == tabId })
                val x = tab.bounds.x + tab.bounds.width / 2f
                val y = tab.bounds.y + tab.bounds.height / 2f
                shellFrame(x, y, down = true)
                shellFrame(x, y, down = false)
            }

            click("studio-bottom-dock.Timeline")
            click("studio-bottom-dock.Console")
        }
}
