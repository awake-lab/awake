// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.engine.app.dsl.requireService
import io.github.ronjunevaldoz.awake.engine.app.lifecycle.AppLifecycle
import io.github.ronjunevaldoz.awake.engine.gameauthoring.game
import io.github.ronjunevaldoz.awake.engine.gameauthoring.module
import io.github.ronjunevaldoz.awake.scene.core.components.Name
import io.github.ronjunevaldoz.awake.scene.runtime.SceneAppLifecycleRuntime
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val FRAME_WIDTH = 1440f
private const val FRAME_HEIGHT = 900f

/**
 * The gizmo through a real frame: pointer coordinates start in frame pixels, the viewport panel
 * is only part of that frame, and the rebasing between the two is exactly what a unit test of the
 * math cannot cover.
 */
class StudioGizmoWiringTest {

    @Test
    fun clickingInsideTheViewportSelectsAndClickingOutsideItDoesNot() = runTest {
        val store = StudioStore()
        val game = game { module(studioModule(store)) }
        game.ready(RecordingCameraRenderer())
        val runtime = game.requireService<SceneAppLifecycleRuntime>()
        val input = game.requireService<Input>()
        game.update(1f / 60f, FRAME_WIDTH, FRAME_HEIGHT)

        val viewport = assertNotNull(
            runtime.uiContext.finishFrame().semantics.firstOrNull { it.id == "studio-panel-viewport" },
        )
        val inspector = assertNotNull(
            runtime.uiContext.finishFrame().semantics.firstOrNull { it.id == "studio-panel-inspector" },
        )

        // Over the inspector: outside the viewport entirely, so no pick may run. A gizmo handed
        // raw frame coordinates would happily hit-test this against a shifted scene.
        click(
            game,
            input,
            inspector.bounds.x + inspector.bounds.width / 2f,
            inspector.bounds.y + 40f
        )
        assertEquals(
            null,
            store.state.value.inspector.selectedEntityId,
            "a click outside must not select"
        )

        // The scene is framed on its camera target, so the middle of the viewport is over geometry.
        click(
            game,
            input,
            viewport.bounds.x + viewport.bounds.width / 2f,
            viewport.bounds.y + viewport.bounds.height / 2f
        )

        val selected = assertNotNull(
            store.state.value.inspector.selectedEntityId,
            "clicking the middle of the viewport must select the geometry under it",
        )
        var selectedName: String? = null
        runtime.world.queryEach<Name> { entity, name ->
            if (entity.id == selected) selectedName = name.value
        }
        assertTrue(selectedName == "cube" || selectedName == "ground", "selected $selectedName")
    }

    private suspend fun click(appLifecycle: AppLifecycle, input: Input, x: Float, y: Float) {
        input.setPointer(down = true, x = x, y = y)
        input.updateSnapshot()
        appLifecycle.update(1f / 60f, FRAME_WIDTH, FRAME_HEIGHT)
        input.setPointer(down = false, x = x, y = y)
        input.updateSnapshot()
        appLifecycle.update(1f / 60f, FRAME_WIDTH, FRAME_HEIGHT)
    }

    /**
     * Holding the button after a pick must keep the selection.
     *
     * The press frame selects and every later frame of the same hold reports "no press" -- which
     * the caller used to read as "clicked empty space" and dispatch as a deselect, so a pick
     * visibly reset itself one frame after landing.
     */
    @Test
    fun holdingTheButtonAfterPickingKeepsTheSelection() = runTest {
        val store = StudioStore()
        val game = game { module(studioModule(store)) }
        game.ready(RecordingCameraRenderer())
        val runtime = game.requireService<SceneAppLifecycleRuntime>()
        val input = game.requireService<Input>()
        game.update(1f / 60f, FRAME_WIDTH, FRAME_HEIGHT)

        val viewport = assertNotNull(
            runtime.uiContext.finishFrame().semantics.firstOrNull { it.id == "studio-panel-viewport" },
        )
        val x = viewport.bounds.x + viewport.bounds.width / 2f
        val y = viewport.bounds.y + viewport.bounds.height / 2f

        input.setPointer(down = true, x = x, y = y)
        input.updateSnapshot()
        game.update(1f / 60f, FRAME_WIDTH, FRAME_HEIGHT)
        val afterPress = assertNotNull(
            store.state.value.inspector.selectedEntityId,
            "the press must select something",
        )

        // Still held, pointer unmoved and then nudged -- neither may clear the selection.
        repeat(3) { game.update(1f / 60f, FRAME_WIDTH, FRAME_HEIGHT) }
        input.setPointer(down = true, x = x + 4f, y = y + 2f)
        input.updateSnapshot()
        game.update(1f / 60f, FRAME_WIDTH, FRAME_HEIGHT)

        assertEquals(
            afterPress,
            store.state.value.inspector.selectedEntityId,
            "holding the button must not reset the selection",
        )
    }

    /** Handles are staged into the 3D pass, not the UI overlay, so they depth-test against the
     * scene -- the renderer is what proves they were emitted at all. */
    @Test
    fun aSelectedEntityStagesThreeHandleLines() = runTest {
        val store = StudioStore()
        val renderer = RecordingCameraRenderer()
        val game = game { module(studioModule(store)) }
        game.ready(renderer)
        val runtime = game.requireService<SceneAppLifecycleRuntime>()
        game.update(1f / 60f, FRAME_WIDTH, FRAME_HEIGHT)

        assertTrue(renderer.debugLines.isEmpty(), "nothing selected must draw no handles")

        val cube = assertNotNull(runtime.world.namedEntity("cube"))
        store.dispatch(StudioContract.Intent.SelectEntity(cube.id))
        game.update(1f / 60f, FRAME_WIDTH, FRAME_HEIGHT)

        assertEquals(3, renderer.debugLines.size, "one handle per translate axis")
    }
}
