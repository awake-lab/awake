// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.ClipSpace
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.engine.game.requireService
import io.github.ronjunevaldoz.awake.engine.gameauthoring.game
import io.github.ronjunevaldoz.awake.engine.gameauthoring.module
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import io.github.ronjunevaldoz.awake.studio.ui.drawStudioShell
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput
import io.github.ronjunevaldoz.awake.ui.font.UiFont
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

    /**
     * The same reproduction, but driving the real click path -- the icon rail's camera
     * button and dropdown, exactly as [drawStudioShell] composes them -- instead of
     * dispatching straight to the store. Rules out cause #3 (menu wired to the wrong
     * callback / index) against the actual `StudioShell`/`IconRail` composition, not just
     * `dispatchCameraMenuPick`'s own index mapping.
     */
    @Test
    fun clickingTopInTheIconRailCameraMenuMovesTheRenderedCamera() = runTest {
        val renderer = RecordingCameraRenderer()
        val store = StudioStore()
        val game = game { module(studioModule(store)) }

        game.ready(renderer)
        val runtime = game.requireService<SceneGameRuntime>()

        fun drive(input: UiInputState) = runtime.uiContext.let { ui ->
            ui.beginFrame(UiFrameInput(viewportWidth = 800f, viewportHeight = 600f, input = input))
            with(runtime) { drawStudioShell(store, 800f, 600f) }
            ui.finishFrame()
        }

        // Frame 1: no input, just to learn where the icon rail laid the camera button out.
        val discovered = drive(UiInputState(pointerX = -1f, pointerY = -1f)).semantics
        val buttonBounds =
            assertNotNull(
                discovered.firstOrNull { it.id == "studio-tool-camera" },
                "Camera rail button not found.",
            ).bounds
        val buttonX = buttonBounds.x + buttonBounds.width / 2f
        val buttonY = buttonBounds.y + buttonBounds.height / 2f

        // Frames 2-3: press + release the button (buttonSlot registers a click on release).
        drive(UiInputState(pointerX = buttonX, pointerY = buttonY, pointerDown = true))
        drive(UiInputState(pointerX = buttonX, pointerY = buttonY, pointerDown = false))

        // Frame 4: menu now open -- find Top Down (action index 3).
        val opened = drive(UiInputState(pointerX = -1f, pointerY = -1f)).semantics
        val itemBounds = assertNotNull(
            opened.firstOrNull { it.id == "studio-tool-camera-menu.dropdown.item.3" },
            "Top Down menu item not found -- menu never opened.",
        ).bounds
        val itemX = itemBounds.x + itemBounds.width / 2f
        val itemY = itemBounds.y + itemBounds.height / 2f

        // Frames 5-6: press + release the "Top" item.
        drive(UiInputState(pointerX = itemX, pointerY = itemY, pointerDown = true))
        drive(UiInputState(pointerX = itemX, pointerY = itemY, pointerDown = false))

        assertEquals(CameraMode.TopDown, store.state.value.camera.mode)

        // And the next real frame moves the camera the renderer actually consumes.
        game.render(1f / 60f, 800f, 600f)
        val topEye = assertNotNull(renderer.lastEye)
        val cubeCameraCenterY = 0.5f
        assertEquals(0f, topEye.x, TOLERANCE)
        assertEquals(cubeCameraCenterY + TOP_DOWN_DISTANCE * sin(TOP_DOWN_ANGLE), topEye.y, TOLERANCE)
        assertEquals(TOP_DOWN_DISTANCE * 0.5f, topEye.z, TOLERANCE)
    }

    /**
     * The projection half of the same report: the Perspective/Orthographic menu set store
     * state that no camera ever read, because `Camera.viewProjectionMatrix` always built
     * `Mat4.perspective`. Asserted on the projection the *renderer* was handed, same reason
     * the eye assertions above are.
     */
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

internal class RecordingCameraRenderer : Renderer {
    var lastEye: Vec3? = null
    var lastProjection: Camera.Projection? = null
    var lastOrthoHalfHeight: Float = 0f

    override val clipSpace: ClipSpace = ClipSpace.WebGpu
    override var clearColor: FloatArray = floatArrayOf(0f, 0f, 0f, 1f)
    override var wireframe: Boolean = false
    override var shadowsEnabled: Boolean = true

    override fun createMesh(geometry: MeshGeometry): Mesh = object : Mesh {
        override val format: VertexFormat = geometry.format
        override fun bind(commandBuffer: Long) = Unit
        override fun draw(commandBuffer: Long) = Unit
        override fun destroy() = Unit
    }

    override fun createMaterial(
        texture: TextureAsset?,
        renderTarget: RenderTarget?,
        uniformFloatCount: Int,
    ): Material =
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

    override fun draw(camera: Camera, drawCalls: List<DrawCall>, light: SceneLight) {
        // Snapshot, not a reference -- `camera.eye` is mutated in place next frame, so holding
        // the live object would make every past frame's assertion read the *latest* value.
        lastEye = Vec3(camera.eye.x, camera.eye.y, camera.eye.z)
        lastProjection = camera.projection
        lastOrthoHalfHeight = camera.orthoHalfHeight
    }

    override fun renderToTexture(target: RenderTarget, camera: Camera, drawCalls: List<DrawCall>) =
        Unit

    override suspend fun readPixels(target: RenderTarget): TextureAsset =
        TextureAsset(ByteArray(target.width * target.height * 4), target.width, target.height)

    override fun drawUi(primitives: List<UiDrawPrimitive>, font: UiFont?) = Unit

    override fun drawDebugLines(lines: List<LineSegment>) = Unit

    override fun destroy() = Unit
}
