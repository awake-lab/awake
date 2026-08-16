// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.state

import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.studio.examples.StudioExamples
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StudioStoreTest {

    @Test
    fun selectExampleUpdatesStateAndQueuesOneLoadEffect() {
        val store = StudioStore()
        val targetId = StudioExamples[1].id

        store.dispatch(StudioContract.Intent.SelectExample(targetId))

        assertEquals(targetId, store.state.value.examples.activeExampleId)
        val effects = store.drainEffects()
        assertEquals(1, effects.size)
        val effect = effects.single()
        assertTrue(effect is StudioContract.Effect.LoadExample)
        assertEquals(targetId, effect.exampleId)
    }

    @Test
    fun drainEffectsReturnsQueuedEffectOnlyOnce() {
        val store = StudioStore()
        store.dispatch(StudioContract.Intent.SelectExample(StudioExamples.first().id))

        assertEquals(1, store.drainEffects().size)
        assertTrue(store.drainEffects().isEmpty())
    }

    @Test
    fun selectEntityUpdatesInspectorStateWithoutQueuingAnEffect() {
        val store = StudioStore()

        store.dispatch(StudioContract.Intent.SelectEntity(42))

        assertEquals(42, store.state.value.inspector.selectedEntityId)
        assertTrue(store.drainEffects().isEmpty())
    }

    @Test
    fun setCameraModeUpdatesStateWithoutQueuingAnEffect() {
        val store = StudioStore()
        assertEquals(CameraMode.ThirdPerson, store.state.value.camera.mode)

        store.dispatch(StudioContract.Intent.SetCameraMode(CameraMode.FirstPerson))

        assertEquals(CameraMode.FirstPerson, store.state.value.camera.mode)
        assertTrue(store.drainEffects().isEmpty())
    }

    @Test
    fun setProjectionFlipsProjection() {
        val store = StudioStore()
        assertEquals(StudioContract.Projection.Perspective, store.state.value.camera.projection)

        store.dispatch(StudioContract.Intent.SetProjection(StudioContract.Projection.Orthographic))
        assertEquals(StudioContract.Projection.Orthographic, store.state.value.camera.projection)

        store.dispatch(StudioContract.Intent.SetProjection(StudioContract.Projection.Perspective))
        assertEquals(StudioContract.Projection.Perspective, store.state.value.camera.projection)
    }

    @Test
    fun consoleEntriesAreRecordedAndCanBeCleared() {
        val store = StudioStore()

        store.dispatch(StudioContract.Intent.AppendConsole(StudioContract.ConsoleLevel.Info, "Example loaded"))
        store.dispatch(StudioContract.Intent.AppendConsole(StudioContract.ConsoleLevel.Warning, "Missing optional asset"))

        assertEquals(
            listOf(
                StudioContract.ConsoleEntry(StudioContract.ConsoleLevel.Info, "Example loaded"),
                StudioContract.ConsoleEntry(StudioContract.ConsoleLevel.Warning, "Missing optional asset"),
            ),
            store.state.value.console.entries,
        )

        store.dispatch(StudioContract.Intent.ClearConsole)

        assertTrue(store.state.value.console.entries.isEmpty())
    }
}
