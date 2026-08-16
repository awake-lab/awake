// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.engine.game.requireService
import io.github.ronjunevaldoz.awake.engine.gameauthoring.game
import io.github.ronjunevaldoz.awake.engine.gameauthoring.module
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import io.github.ronjunevaldoz.awake.testing.render.NoopRenderer
import kotlinx.coroutines.test.runTest
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.tan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

/**
 * Reproduces the "picking a camera mode does nothing" report end to end: real
 * [studioModule], real [StudioStore]/reducer, a real [SceneGameRuntime] driving the real
 * `rotating-cube` scene document -- only the GPU [Renderer] is faked, same shape as
 * `awake:scene-dsl`'s own `SceneGameDslTest`. Asserts on the eye position the *renderer*
 * was actually handed by [Renderer.draw], since that -- not the store's own state -- is
 * the nearest real proxy for "did the rendered camera move" this module exposes headlessly.
 */
class StudioModuleCameraTest {

    @Test
    fun cameraModeHotkeyUpdatesTheStudioHeaderState() = runTest {
        val renderer = RecordingCameraRenderer()
        val store = StudioStore()
        val game = game { module(studioModule(store)) }

        game.ready(renderer)
        game.render(1f / 60f, 800f, 600f)

        val input = game.requireService<Input>()
        input.setKeyDown(Key.F5, true)
        input.updateSnapshot()
        game.render(1f / 60f, 800f, 600f)

        // CameraInputSystem changes the component; Studio's bridge must reflect it back to the
        // store so the visible header does not claim the old mode.
        assertEquals(CameraMode.TopDown, store.state.value.camera.mode)
    }

    @Test
    fun setCameraModeMovesTheCameraTheRendererActuallyDraws() = runTest {
        val renderer = RecordingCameraRenderer()
        val store = StudioStore()
        val game = game { module(studioModule(store)) }

        game.ready(renderer)
        val runtime = game.requireService<SceneGameRuntime>()
        game.render(1f / 60f, 800f, 600f)

        val orbitEye =
            assertNotNull(renderer.lastEye, "No draw() call captured for the Orbit default.")

        store.dispatch(StudioContract.Intent.SetCameraMode(CameraMode.TopDown))
        game.render(1f / 60f, 800f, 600f)
        val topEye =
            assertNotNull(renderer.lastEye, "No draw() call captured after SetCameraMode(Top).")

        // The regression: before the fix, the driver system applied the preset to a camera
        // object the renderer never reads, so this eye position never moved.
        assertNotEquals(orbitEye, topEye)

        // And it moved to CameraSystem's engine-owned TopDown pose -- not just "somewhere
        // else". rotating-cube.scene.json authors the camera's center at (0, 0.5, 0), which
        // Studio supplies as CameraComponent's target transform.
        val cubeCameraCenterY = 0.5f
        assertEquals(0f, topEye.x, TOLERANCE)
        assertEquals(cubeCameraCenterY + TOP_DOWN_DISTANCE * sin(TOP_DOWN_ANGLE), topEye.y, TOLERANCE)
        assertEquals(TOP_DOWN_DISTANCE * 0.5f, topEye.z, TOLERANCE)

        // Same object the driver system mutated -- confirms the renderer and the camera
        // entity the ECS world holds are one and the same, not two independent cameras.
        val worldEye = assertNotNull(runtime.findCamera("camera")).camera.eye
        assertEquals(topEye.x, worldEye.x, TOLERANCE)
        assertEquals(topEye.y, worldEye.y, TOLERANCE)
        assertEquals(topEye.z, worldEye.z, TOLERANCE)
    }

    @Test
    fun setProjectionSwitchesTheProjectionTheRendererActuallyDraws() = runTest {
        val renderer = RecordingCameraRenderer()
        val store = StudioStore()
        val game = game { module(studioModule(store)) }

        game.ready(renderer)
        val runtime = game.requireService<SceneGameRuntime>()
        game.render(1f / 60f, 800f, 600f)

        assertEquals(Camera.Projection.Perspective, renderer.lastProjection)

        store.dispatch(StudioContract.Intent.SetProjection(StudioContract.Projection.Orthographic))
        game.render(1f / 60f, 800f, 600f)

        assertEquals(Camera.Projection.Orthographic, renderer.lastProjection)

        // And it is framed to match what the perspective lens showed at CameraSystem's
        // third-person distance,
        // so the toggle changes the projection without resizing the subject.
        val fovYRadians = assertNotNull(runtime.findCamera("camera")).camera.fovYRadians
        val expectedHalfHeight = THIRD_PERSON_DISTANCE * tan(fovYRadians / 2f)
        assertEquals(expectedHalfHeight, renderer.lastOrthoHalfHeight, TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 1e-4f
        const val THIRD_PERSON_DISTANCE = 5f
        const val TOP_DOWN_DISTANCE = 15f

        // kotlin.math.PI, not java.lang.Math: this is commonTest, and Math only exists on the
        // JVM -- the wasmJs and native test compilations failed on it. Desktop-only runs cannot
        // see that, which is why it survived until a full `gradlew build`.
        val TOP_DOWN_ANGLE = (60.0 * PI / 180.0).toFloat()
    }
}

internal class RecordingCameraRenderer : NoopRenderer() {
    var lastEye: Vec3? = null

    /** Staged debug lines from the most recent frame -- the gizmo's handles land here, since
     * they are world-space 3D geometry rather than UI primitives. */
    var debugLines: List<LineSegment> = emptyList()
        private set
    var lastProjection: Camera.Projection? = null
    var lastOrthoHalfHeight: Float = 0f

    override fun draw(camera: Camera, drawCalls: List<DrawCall>, light: SceneLight) {
        // Snapshot, not a reference -- `camera.eye` is mutated in place next frame, so holding
        // the live object would make every past frame's assertion read the *latest* value.
        lastEye = Vec3(camera.eye.x, camera.eye.y, camera.eye.z)
        lastProjection = camera.projection
        lastOrthoHalfHeight = camera.orthoHalfHeight
    }

    override fun drawDebugLines(lines: List<LineSegment>) {
        debugLines = lines.toList()
    }
}
