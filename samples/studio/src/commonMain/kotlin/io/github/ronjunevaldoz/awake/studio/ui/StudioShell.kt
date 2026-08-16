// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.render.renderer.RenderViewport
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.scene.core.components.Name
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.headlessFrame
import io.github.ronjunevaldoz.awake.studio.gizmo.StudioViewportRect
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnResizableHandle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnResizablePanel
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnResizablePanelGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSelect
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSeparator
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnToggleGroup
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiResizableDirection
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.weight
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.toPx

internal val StudioTheme = shadcnThemeValues(dark = true)

// Dark neutral gray, not pure black -- Renderer.clearColor otherwise defaults to stark black,
// which reads as "nothing rendered" rather than a real viewport background. Same reasoning
// scene3d-playground's own VIEWPORT_CLEAR_COLOR documents.
@Suppress("MagicNumber") // The literals define the constant; naming each channel adds nothing.
private val ViewportClearColor = floatArrayOf(0.14f, 0.14f, 0.16f, 1f)

internal fun SceneGameRuntime.drawStudioShell(
    store: StudioStore,
    backend: String,
    viewportWidth: Float,
    viewportHeight: Float,
    viewportRect: StudioViewportRect = StudioViewportRect(),
) {
    renderer.clearColor = ViewportClearColor
    headlessFrame(viewportWidth, viewportHeight) {
        shadcnTheme(theme = StudioTheme) {
            drawStudioShellBody(store, world, renderer, backend, viewportRect)
        }
    }
}

// Sum to exactly 1f -- shadcnResizablePanelGroup lays each panel out independently at
// fraction * availableWidth (see ResizablePanelGroupScope.panel), not "last panel fills the
// remainder", so a sum under/over 1 leaves a gap or overlap on first mount.
private const val SIDEBAR_FRACTION = 0.18f
private const val VIEWPORT_FRACTION = 0.62f
private const val INSPECTOR_FRACTION = 0.20f
private const val MAIN_WORKSPACE_FRACTION = 0.72f
private const val BOTTOM_DOCK_FRACTION = 0.28f

// Same default shadcnSeparator() itself uses -- passed explicitly (not left to the default) so
// this file's own height math below stays in sync with what actually gets drawn.
private val SEPARATOR_THICKNESS = 1f.dp

/**
 * The docked shell chrome: a full-width top bar, a 3-way resizable split (Scene Browser |
 * viewport | Inspector), and a full-width status bar -- flush to every frame edge, no outer
 * margin, so it reads as a docked IDE rather than the floating light-theme cards with margins
 * around a dark viewport this replaced. The only separators are hairlines
 * ([io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSeparator]) and the resizable
 * group's own draggable handles.
 *
 * Plain [store]/[world]/[renderer] params instead of a [SceneGameRuntime] receiver -- letting a
 * bare [io.github.ronjunevaldoz.awake.ui.context.UiContext] drive this directly is what makes it
 * testable without a full game/runtime bootstrap (see `StudioShellLayoutTest`).
 *
 * The workspace group's height is computed explicitly (shell height minus the top bar, the
 * status bar, and both hairlines) rather than `Modifier.weight(1f)` -- a weight fill here left a
 * gap exactly the status bar's own height tall between the panels and the status bar, since
 * `shadcnResizablePanelGroup` already runs its own dry-count-then-real pass over its content
 * independently of the outer column's weight-distribution trial; computing the height directly
 * sidesteps that interaction instead of depending on it. A vertical resizable group owns the
 * main three-panel workspace and the bottom dock; the horizontal group remains nested inside
 * the main panel, matching the ui-showcase nested-resizable example.
 */
internal fun UiScope.drawStudioShellBody(
    store: StudioStore,
    world: World,
    renderer: Renderer,
    backend: String,
    viewportRect: StudioViewportRect = StudioViewportRect(),
) {
    column(
        verticalArrangement = Arrangement.spacedBy(0f.dp),
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
    ) { shellSlot ->
        val playing = store.state.value.mode == StudioContract.Mode.Play
        drawStudioTopBar(
            activeExampleId = store.state.value.examples.activeExampleId,
            playing = playing,
            onSelectExample = { store.dispatch(StudioContract.Intent.SelectExample(it)) },
            onSave = { store.dispatch(StudioContract.Intent.SaveScene) },
            // Stop reloads the scene (see StudioStore), which is what discards play-mode edits.
            onTogglePlay = {
                val next = if (playing) StudioContract.Mode.Edit else StudioContract.Mode.Play
                store.dispatch(StudioContract.Intent.SetMode(next))
            },
        )
        shadcnSeparator(thickness = SEPARATOR_THICKNESS)
        val workspaceHeightPx = (
            shellSlot.height - TOP_BAR_HEIGHT.toPx() - STATUS_BAR_HEIGHT.toPx() -
                SEPARATOR_THICKNESS.toPx() * 2f
            ).coerceAtLeast(0f)
        drawStudioWorkspace(
            store = store,
            world = world,
            renderer = renderer,
            heightPx = workspaceHeightPx,
            viewportRect = viewportRect,
        )
        shadcnSeparator(thickness = SEPARATOR_THICKNESS)
        drawStudioStatusBar(
            mode = if (playing) "Play mode" else "Edit mode",
            backend = backend,
            entityCount = world.namedEntityCount(),
        )
    }
}

/**
 * Points the renderer's 3D pass at the viewport panel instead of the whole window.
 *
 * Without this the scene fills the surface and every panel is a hole punched over it, and the
 * projection uses the window's aspect rather than the panel's. The overlay runs after the frame's
 * scene draw, so a resize lands one frame later -- invisible, and cheaper than measuring the
 * shell twice.
 *
 * Only assigns on change: this is called every frame, and a per-frame [RenderViewport] would be
 * an allocation per frame for a value that changes only on resize or panel drag.
 */
private fun Renderer.confineSceneTo(bounds: UiBounds) {
    val current = sceneViewport
    val unchanged = current != null &&
        current.x == bounds.x &&
        current.y == bounds.y &&
        current.width == bounds.width &&
        current.height == bounds.height
    if (unchanged) return
    sceneViewport = RenderViewport(bounds.x, bounds.y, bounds.width, bounds.height)
}

/** Counts without allocating -- `queryEach` is the non-allocating iteration path, and this runs
 * once per frame from the overlay. */
private fun World.namedEntityCount(): Int {
    var count = 0
    queryEach<Name> { _, _ -> count++ }
    return count
}

private fun UiScope.drawStudioWorkspace(
    store: StudioStore,
    world: World,
    renderer: Renderer,
    heightPx: Float,
    viewportRect: StudioViewportRect,
) {
    shadcnResizablePanelGroup(
        id = "studio-workspace-group",
        direction = UiResizableDirection.Vertical,
        // heightPx is already pixels (computed from the shell slot) -- .px, not .dp: wrapping a
        // pixel value as Dp re-multiplies by UiDensity.scale and doubled the workspace on Retina.
        modifier = Modifier.fillMaxWidth().height(heightPx.px),
    ) {
        shadcnResizablePanel(
            id = "studio-workspace-main",
            defaultSize = MAIN_WORKSPACE_FRACTION,
            minSize = 0.4f,
            maxSize = 0.86f,
        ) {
            shadcnResizablePanelGroup(
                id = "studio-panels-group",
                direction = UiResizableDirection.Horizontal,
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            ) {
                shadcnResizablePanel(
                    id = "studio-panel-sidebar",
                    defaultSize = SIDEBAR_FRACTION,
                    minSize = 0.12f,
                    maxSize = 0.32f,
                ) {
                    drawHierarchyPanel(
                        world = world,
                        selectedEntityId = store.state.value.inspector.selectedEntityId,
                        onSelectEntity = { store.dispatch(StudioContract.Intent.SelectEntity(it)) },
                    )
                }
                shadcnResizableHandle(id = "studio-panel-handle-left", withHandle = true)
                shadcnResizablePanel(id = "studio-panel-viewport", defaultSize = VIEWPORT_FRACTION, minSize = 0.3f) {
                    drawStudioViewportPanel(store, renderer, viewportRect)
                }
                shadcnResizableHandle(id = "studio-panel-handle-right", withHandle = true)
                shadcnResizablePanel(
                    id = "studio-panel-inspector",
                    defaultSize = INSPECTOR_FRACTION,
                    minSize = 0.14f,
                    maxSize = 0.32f,
                ) {
                    drawInspectorPanel(world, selectedEntityId = store.state.value.inspector.selectedEntityId)
                }
            }
        }
        shadcnResizableHandle(id = "studio-bottom-dock-handle", withHandle = true)
        shadcnResizablePanel(
            id = "studio-bottom-dock-panel",
            defaultSize = BOTTOM_DOCK_FRACTION,
            minSize = 0.14f,
            maxSize = 0.6f,
        ) {
            drawStudioBottomDock(store)
        }
    }
}

/** The viewport panel's own content: the floating icon rail hugging its left edge, the 3D
 * viewport region (the toolbar used to float here too -- it lives in the top bar now), and the
 * right-click camera menu hooked to that same region's bounds. */
private fun UiScope.drawStudioViewportPanel(
    store: StudioStore,
    renderer: Renderer,
    viewportRect: StudioViewportRect,
) {
    column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        drawStudioViewportHeader(store)
        row(
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) {
            drawIconRail(
                onResetExample = { store.dispatch(StudioContract.Intent.SelectExample(store.state.value.examples.activeExampleId)) },
                onSelectCameraMode = { store.dispatch(StudioContract.Intent.SetCameraMode(it)) },
                onSelectCameraProjection = { store.dispatch(StudioContract.Intent.SetProjection(it)) },
            )
            val viewportBounds = column(
                modifier = Modifier.weight(1f).fillMaxHeight().padding(8f.dp),
            ) { }
            renderer.confineSceneTo(viewportBounds)
            viewportRect.bounds = viewportBounds
            drawDisplayRail(
                wireframe = renderer.wireframe,
                shadows = renderer.shadowsEnabled,
                onWireframeChange = { renderer.wireframe = it },
                onShadowsChange = { renderer.shadowsEnabled = it },
            )
            viewportCameraMenu(
                id = "studio-viewport-camera-menu",
                bounds = viewportBounds,
                onPick = { index ->
                    dispatchCameraMenuPick(
                        index,
                        onSelectMode = { store.dispatch(StudioContract.Intent.SetCameraMode(it)) },
                        onSelectProjection = { store.dispatch(StudioContract.Intent.SetProjection(it)) },
                    )
                },
            )
        }
    }
}

private fun UiScope.drawStudioViewportHeader(store: StudioStore) {
    val camera = store.state.value.camera
    val modes = CameraMode.entries
    val projections = StudioContract.Projection.entries
    row(
        verticalAlignment = UiAlignment.Vertical.Center,
        modifier = Modifier.fillMaxWidth().height(40f.dp),
    ) {
        shadcnSelect(
            id = "studio-viewport-camera-mode",
            options = modes.map { it.name },
            selectedIndex = modes.indexOf(camera.mode),
            modifier = Modifier.width(156f.dp),
        )?.let { index ->
            modes.getOrNull(index)?.let { store.dispatch(StudioContract.Intent.SetCameraMode(it)) }
        }
        shadcnToggleGroup(
            id = "studio-viewport-projection",
            options = listOf("Perspective", "Ortho"),
            selectedIndex = projections.indexOf(camera.projection),
            modifier = Modifier.width(176f.dp).height(36f.dp),
        ) { index ->
            projections.getOrNull(index)?.let { store.dispatch(StudioContract.Intent.SetProjection(it)) }
        }
    }
}
