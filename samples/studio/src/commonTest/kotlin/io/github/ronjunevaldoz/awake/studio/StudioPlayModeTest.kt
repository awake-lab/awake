// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio

import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.engine.app.dsl.requireService
import io.github.ronjunevaldoz.awake.engine.platformauthoring.dsl.app
import io.github.ronjunevaldoz.awake.engine.platformauthoring.dsl.module
import io.github.ronjunevaldoz.awake.scene.core.components.Name
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.runtime.SceneAppLifecycleRuntime
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StudioPlayModeTest {

    /** Edit mode must hold still: a spin that keeps ticking overwrites every inspector edit. */
    @Test
    fun theCubeSpinsOnlyInPlayMode() = runTest {
        val store = StudioStore()
        val game = app { module(studioModule(store)) }
        game.ready(RecordingCameraRenderer())
        val runtime = game.requireService<SceneAppLifecycleRuntime>()
        repeat(3) { game.update(1f / 60f, 1440f, 900f) }

        val cube = assertNotNull(runtime.world.namedEntity("cube"))
        val rotation = assertNotNull(runtime.world.get<Transform>(cube)).rotation
        val atRest = rotation.y
        repeat(5) { game.update(1f / 60f, 1440f, 900f) }
        assertEquals(atRest, rotation.y, "edit mode must not tick the spin system")

        store.dispatch(StudioContract.Intent.SetMode(StudioContract.Mode.Play))
        repeat(5) { game.update(1f / 60f, 1440f, 900f) }
        assertTrue(rotation.y != atRest, "play mode must tick the spin system, still ${rotation.y}")
    }

    /** Play-mode edits are deliberately thrown away on stop, which is what makes play safe. */
    @Test
    fun stoppingDiscardsWhatPlayModeChanged() = runTest {
        val store = StudioStore()
        val game = app { module(studioModule(store)) }
        game.ready(RecordingCameraRenderer())
        val runtime = game.requireService<SceneAppLifecycleRuntime>()
        game.update(1f / 60f, 1440f, 900f)

        store.dispatch(StudioContract.Intent.SetMode(StudioContract.Mode.Play))
        game.update(1f / 60f, 1440f, 900f)
        val cube = assertNotNull(runtime.world.namedEntity("cube"))
        assertNotNull(runtime.world.get<Transform>(cube)).position.x = 9f

        store.dispatch(StudioContract.Intent.SetMode(StudioContract.Mode.Edit))
        game.update(1f / 60f, 1440f, 900f)

        val reloaded = assertNotNull(
            runtime.world.namedEntity("cube"),
            "stopping must re-instantiate the scene"
        )
        assertEquals(
            0f,
            assertNotNull(runtime.world.get<Transform>(reloaded)).position.x,
            "a play-mode edit must not survive stop",
        )
    }
}

internal fun World.namedEntity(name: String): Entity? {
    var found: Entity? = null
    queryEach<Name> { entity, value -> if (found == null && value.value == name) found = entity }
    return found
}
