// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio

import io.github.ronjunevaldoz.awake.engine.gameauthoring.game
import io.github.ronjunevaldoz.awake.engine.gameauthoring.module
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Where the camera actually ends up -- the part no unit test of the math can answer. */
class StudioCameraPoseTest {

    /** The scene's authored eye is 10 units back; CameraSystem recomputes the eye from
     * yaw/pitch/distance every frame, so without seeding those from the document the first frame
     * framed the scene from ~4.6 units -- inside the ground plane the scene draws. */
    @Test
    fun theFirstFrameKeepsTheDistanceTheSceneAuthored() = runTest {
        val renderer = RecordingCameraRenderer()
        val game = game { module(studioModule(StudioStore())) }
        game.ready(renderer)
        repeat(3) { game.render(1f / 60f, 1440f, 900f) }

        val eye = assertNotNull(renderer.lastEye)
        val distance = kotlin.math.sqrt(eye.x * eye.x + (eye.y - 0.5f) * (eye.y - 0.5f) + eye.z * eye.z)
        assertTrue(
            distance > 8f,
            "the authored camera sits ~11 units from its target; the rendered one was at $distance ($eye)",
        )
    }

    /**
     * First person must not start inside what it was aiming at.
     *
     * The orbit modes aim at `target + offsetPosition` while first person PLACES the eye there,
     * so a single shared offset cannot serve both: zeroing it (which fixed orbit framing) put the
     * viewer inside the cube.
     */
    @Test
    fun firstPersonStartsAboveTheAimPointNotInsideIt() = runTest {
        val renderer = RecordingCameraRenderer()
        val store = StudioStore()
        val game = game { module(studioModule(store)) }
        game.ready(renderer)
        game.render(1f / 60f, 1440f, 900f)

        store.dispatch(StudioContract.Intent.SetCameraMode(CameraMode.FirstPerson))
        repeat(3) { game.render(1f / 60f, 1440f, 900f) }

        val eye = assertNotNull(renderer.lastEye)
        assertTrue(eye.y > 1.5f, "first person must stand above the aim point, was $eye")
    }
}
