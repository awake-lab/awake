// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.ClipSpace
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.studio.examples.StudioExamples
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import io.github.ronjunevaldoz.awake.ui.UiDensity
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.headless.createUiScope
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

    private fun renderShell(): List<UiSemanticNode> {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(shadcnThemeValues(dark = true))
        ui.beginFrame(FRAME_WIDTH, FRAME_HEIGHT, UiInputState(pointerX = -100f, pointerY = -100f))
        ui.createUiScope(slot = UiBounds(0f, 0f, FRAME_WIDTH, FRAME_HEIGHT))
            .drawStudioShellBody(StudioStore(), World(), NoopRenderer())
        return ui.finishFrame().semantics
    }

    // The live app runs at Retina density; the shell computes its workspace height in pixels,
    // and wrapping that pixel value back as Dp re-multiplied it by UiDensity.scale -- the
    // workspace claimed twice the available height, pushing the dock handle and status bar off
    // the frame. Only reproduces with scale != 1, which every other test here leaves at 1.
    @Test
    fun workspaceStaysInsideTheShellAtRetinaDensity() {
        val previousScale = UiDensity.scale
        UiDensity.scale = 2f
        try {
            val semantics = renderShell()
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
        } finally {
            UiDensity.scale = previousScale
        }
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
    fun activeExampleItemPaintsAVisibleHighlightNotATransparentFill() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(shadcnThemeValues(dark = true))
        val activeId = StudioExamples.first().id
        ui.beginFrame(400f, 600f, UiInputState(pointerX = -100f, pointerY = -100f))
        ui.createUiScope(slot = UiBounds(0f, 0f, 400f, 600f))
            .drawExampleRail(activeExampleId = activeId, onSelectExample = {})
        val item = assertNotNull(ui.finishFrame().semantics.firstOrNull { it.id == "studio-example-$activeId" })
        // Regression: shadcnButton's Ghost variant hardcodes an idle (non-hover) fill of fully
        // transparent regardless of what a caller's own Style sets, so the "active" item's real
        // highlight never painted while staying Ghost -- exampleMenuItem switches to Primary
        // (which always honors the resolved background) for the active item instead.
        assertTrue((item.backgroundColor?.a ?: 0f) > 0f, "active item background: ${item.backgroundColor}")
    }

    @Test
    fun viewportHeaderExposesCameraModeAndProjectionControls() {
        val semantics = renderShell()

        assertNotNull(semantics.firstOrNull { it.id == "studio-viewport-camera-mode" })
        assertNotNull(semantics.firstOrNull { it.id == "studio-viewport-projection.0" })
        assertNotNull(semantics.firstOrNull { it.id == "studio-viewport-projection.1" })
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
    fun switchingBottomDockTabsKeepsMeasuredChildrenInSync() {
        val ui = UiContext()
        val store = StudioStore()
        val world = World()
        ui.pushFont(BitmapFont())
        ui.pushTheme(shadcnThemeValues(dark = true))

        fun frame(input: UiInputState): List<UiSemanticNode> {
            ui.beginFrame(FRAME_WIDTH, FRAME_HEIGHT, input)
            ui.createUiScope(slot = UiBounds(0f, 0f, FRAME_WIDTH, FRAME_HEIGHT))
                .drawStudioShellBody(store, world, NoopRenderer())
            return ui.finishFrame().semantics
        }

        fun click(tabId: String) {
            val tab = assertNotNull(
                frame(UiInputState(pointerX = -100f, pointerY = -100f))
                    .firstOrNull { it.id == tabId },
            )
            val x = tab.bounds.x + tab.bounds.width / 2f
            val y = tab.bounds.y + tab.bounds.height / 2f
            frame(UiInputState(pointerX = x, pointerY = y, pointerDown = true))
            frame(UiInputState(pointerX = x, pointerY = y, pointerDown = false))
        }

        click("studio-bottom-dock.Timeline")
        click("studio-bottom-dock.Console")
    }
}

private class NoopRenderer : Renderer {
    override val clipSpace: ClipSpace = ClipSpace.WebGpu
    override var clearColor: FloatArray = floatArrayOf(0f, 0f, 0f, 1f)
    override var wireframe: Boolean = false
    override var shadowsEnabled: Boolean = true

    override fun createMesh(geometry: MeshGeometry): Mesh = object : Mesh {
        override val format = geometry.format
        override fun bind(commandBuffer: Long) = Unit
        override fun draw(commandBuffer: Long) = Unit
        override fun destroy() = Unit
    }

    override fun createMaterial(texture: TextureAsset?, renderTarget: RenderTarget?, uniformFloatCount: Int): Material =
        object : Material {
            override fun updateUniformBuffer(mvp: FloatArray) = Unit
            override fun bind(commandBuffer: Long, pipelineLayout: Long) = Unit
            override fun destroy() = Unit
        }

    override fun createRenderTarget(width: Int, height: Int): RenderTarget = object : RenderTarget {
        override val width: Int = width
        override val height: Int = height
        override fun destroy() = Unit
    }

    override fun draw(camera: Camera, drawCalls: List<DrawCall>, light: SceneLight) = Unit
    override fun renderToTexture(target: RenderTarget, camera: Camera, drawCalls: List<DrawCall>) = Unit
    override suspend fun readPixels(target: RenderTarget): TextureAsset = TextureAsset(ByteArray(0), 0, 0)
    override fun drawUi(primitives: List<UiDrawPrimitive>, font: UiFont?) = Unit
    override fun drawDebugLines(lines: List<LineSegment>) = Unit
    override fun destroy() = Unit
}
