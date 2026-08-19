// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio

import io.github.ronjunevaldoz.awake.engine.app.dsl.requireService
import io.github.ronjunevaldoz.awake.engine.gameauthoring.game
import io.github.ronjunevaldoz.awake.engine.gameauthoring.module
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.runtime.SceneAppLifecycleRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.SceneLoader
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Save exports the LIVE world: what the inspector edited is what lands in the file. */
class StudioSaveTest {

    @Test
    fun savingWritesTheEditedTransformNotTheLoadedDocument() = runTest {
        var written: Pair<String, String>? = null
        val store = StudioStore()
        val game = game {
            module(
                studioModule(store, writeScene = { name, json ->
                    written = name to json
                    "/tmp/$name"
                }),
            )
        }
        game.ready(RecordingCameraRenderer())
        val runtime = game.requireService<SceneAppLifecycleRuntime>()
        game.update(1f / 60f, 1440f, 900f)

        val cube = assertNotNull(
            runtime.world.namedEntity("cube"),
            "the default example must instantiate a 'cube' entity",
        )
        assertNotNull(runtime.world.get<Transform>(cube)).position.x = 4f

        store.dispatch(StudioContract.Intent.SaveScene)
        game.update(1f / 60f, 1440f, 900f)

        val (fileName, json) = assertNotNull(written, "save must reach the writer")
        assertEquals("rotating-cube.scene.json", fileName)
        val document = SceneLoader.decode(json)
        val savedCube = assertNotNull(
            document.nodes.firstOrNull { it.name == "cube" },
            "exported document must contain the cube: $json",
        )
        assertEquals(
            4f,
            savedCube.transform.position.x,
            "the edited position must be what gets saved"
        )
        assertTrue(
            savedCube.components.any { it is io.github.ronjunevaldoz.awake.scene.runtime.SceneMeshRenderer },
            "the cube's authored mesh/material IDs must survive the round trip",
        )
    }
}
