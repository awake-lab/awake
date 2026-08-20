// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.engine.app.dsl.requireService
import io.github.ronjunevaldoz.awake.engine.platformauthoring.dsl.app
import io.github.ronjunevaldoz.awake.engine.platformauthoring.dsl.module
import io.github.ronjunevaldoz.awake.scene.runtime.SceneAppLifecycleRuntime
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import io.github.ronjunevaldoz.awake.ui.context.UiCursor
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Shell chrome contracts that only show up through a real [SceneAppLifecycleRuntime] frame:
 * the resize cursor actually reaching the runtime's host-facing field, and the bottom dock
 * painting an opaque background instead of exposing the renderer clear color. */
class StudioShellChromeTest {

    @Test
    fun hoveringAWorkspaceHandlePublishesTheResizeCursor() = runTest {
        val renderer = RecordingCameraRenderer()
        val store = StudioStore()
        val game = app { module(studioModule(store)) }
        game.ready(renderer)
        val runtime = game.requireService<SceneAppLifecycleRuntime>()
        val input = game.requireService<Input>()

        game.update(1f / 60f, 1440f, 900f)
        assertEquals(UiCursor.Default, runtime.cursor, "no hover -> default cursor")

        val handle = assertNotNull(
            runtime.uiContext.finishFrame().semantics.firstOrNull { it.id == "studio-panel-handle-left" },
            "left workspace handle must exist",
        )
        input.setPointer(
            down = false,
            x = handle.bounds.x + handle.bounds.width / 2f,
            y = handle.bounds.y + handle.bounds.height / 2f,
        )
        input.updateSnapshot()
        game.update(1f / 60f, 1440f, 900f)

        assertEquals(
            UiCursor.ResizeHorizontal,
            runtime.cursor,
            "hovering the left panel handle must publish the horizontal resize cursor to the host",
        )
    }

    @Test
    fun bottomDockPaintsAnOpaqueBackground() = runTest {
        val renderer = RecordingCameraRenderer()
        val store = StudioStore()
        val game = app { module(studioModule(store)) }
        game.ready(renderer)
        val runtime = game.requireService<SceneAppLifecycleRuntime>()
        game.update(1f / 60f, 1440f, 900f)

        val dock = assertNotNull(
            runtime.uiContext.finishFrame().semantics.firstOrNull { it.id == "studio-bottom-dock-surface" },
            "dock surface must exist",
        )
        val background = assertNotNull(dock.backgroundColor, "dock must declare a background")
        assertTrue(background.a > 0.99f, "dock background must be opaque, was $background")
    }
}
