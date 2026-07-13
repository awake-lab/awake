// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.core.application.FixedTimestepLoop
import io.github.ronjunevaldoz.awake.core.math.Grid
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.engine.application.Game
import io.github.ronjunevaldoz.awake.physics.BoxShape
import io.github.ronjunevaldoz.awake.physics.MotionType
import io.github.ronjunevaldoz.awake.physics.PhysicsWorld
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.PhysicsBody
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.runtime.SceneRuntime
import io.github.ronjunevaldoz.awake.scene.systems.PhysicsSystem
import io.github.ronjunevaldoz.awake.ui.ColumnScope
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.toggle

/**
 * A dedicated Jolt Physics demo -- a static ground box and a dynamic cube dropped from a
 * height above it, wired through `awake:scene`'s [PhysicsBody]/[PhysicsSystem] (see
 * docs/MVP_PLAN.md's decision log for the full Jolt Physics integration entry).
 *
 * A new catalog entry rather than added onto [CubeDemo] -- [CubeDemo] already juggles two
 * camera modes, a frustum toggle, and a minimap; bolting physics onto that would mean a
 * fourth concern fighting the same `render()`/HUD wiring. This demo instead owns its own
 * (fixed, non-orbiting) camera, exactly like [CubeDemo] would if it had nothing to orbit
 * around, so physics settling is easy to observe without a moving viewpoint fighting it.
 *
 * [createPhysicsWorld] returns `null` on platforms without a real `PhysicsWorld` backend yet
 * (iOS/wasmJs -- `awake:backend:jolt`'s cinterop/JS backends are deferred). On those
 * platforms this demo still loads/renders the scene (ground + cube at rest height) but never
 * steps physics, so it degrades to a static diorama instead of crashing.
 *
 * Takes [ui]/[font] from [DemoCatalog], same as [CubeDemo] does -- this is the first widget
 * [PhysicsDemo] needs (a GRID toggle for the reference floor grid), so it now needs the same
 * shared [UiContext]/[BitmapFont] instance [CubeDemo] already appends its own widgets to (see
 * [CubeDemo]'s own doc comment for why only [DemoCatalog] may call `ui.beginFrame`/
 * `renderer.drawUi`).
 */
class PhysicsDemo(private val ui: UiContext, private val font: BitmapFont) : Game, DebugReadout, DebugCameraTarget, PanelUser {
    private val fixedTimestepLoop = FixedTimestepLoop()

    private lateinit var renderer: Renderer
    private lateinit var sceneRuntime: SceneRuntime
    private lateinit var cubeMesh: Mesh
    private lateinit var material: Material
    private lateinit var cameraComponent: Camera

    private var physicsWorld: PhysicsWorld? = null
    private var physicsSystem: PhysicsSystem? = null

    private lateinit var cubeTransform: Transform

    // Reference/floor grid at the ground plane's top surface (ground's authored
    // position.y=-0.25 + half its scale.y=0.25 == 0f, see physics.scene.json) -- makes the
    // ground box's extent and the falling cube's landing spot easier to judge visually.
    // Off by default, same as CubeDemo's own toggles.
    private var showGrid = false

    // Logs the falling cube's Y position once a second (not every frame) -- enough to
    // observe it fall then settle, matching DemoCatalog's own "log HUD state once a second"
    // cadence rather than a bespoke frame-count accumulator.
    private var logAccumulator = 0f

    override suspend fun ready(renderer: Renderer) {
        this.renderer = renderer
        cubeMesh = renderer.createMesh(sampleCubeGeometry)
        material = renderer.createMaterial()
        sceneRuntime = SceneRuntime(renderer)
        sceneRuntime.load(SCENE_PATH) { request ->
            val mesh = cubeMesh.takeIf { request.meshRenderer.mesh == "cube" }
                ?: error("Unsupported scene mesh '${request.meshRenderer.mesh}'.")
            MeshRenderer(mesh, material)
        }

        val world = sceneRuntime.world
        val scene = sceneRuntime.scene
        val groundEntity = scene.roots.firstOrNull { it.name == "ground" }?.entity
            ?: error("physics.scene.json is missing a root node named 'ground'.")
        val cubeEntity = scene.roots.firstOrNull { it.name == "cube" }?.entity
            ?: error("physics.scene.json is missing a root node named 'cube'.")
        val cameraEntity = scene.roots.firstOrNull { it.name == "camera" }?.entity
            ?: error("physics.scene.json is missing a root node named 'camera'.")

        cubeTransform = world.get(cubeEntity) ?: error("'cube' node has no Transform.")
        val groundTransform: Transform = world.get(groundEntity) ?: error("'ground' node has no Transform.")
        cameraComponent = world.get(cameraEntity) ?: error("'camera' node has no Camera component.")

        // Half-extents matching each node's authored render scale (unit cube mesh * scale),
        // NOT the render scale itself -- BoxShape wants half the box's total size per axis.
        val groundHalfExtents = Vec3(
            groundTransform.scale.x / 2f,
            groundTransform.scale.y / 2f,
            groundTransform.scale.z / 2f
        )
        world.add(
            groundEntity,
            PhysicsBody(shape = BoxShape(halfExtents = groundHalfExtents), motionType = MotionType.STATIC)
        )
        world.add(
            cubeEntity,
            PhysicsBody(
                shape = BoxShape(halfExtents = Vec3(0.5f, 0.5f, 0.5f)),
                motionType = MotionType.DYNAMIC
            )
        )

        val createdPhysicsWorld = createPhysicsWorld()
        physicsWorld = createdPhysicsWorld
        physicsSystem = createdPhysicsWorld?.let(::PhysicsSystem)
    }

    override fun render(delta: Float, viewportWidth: Float, viewportHeight: Float) {
        // Always call drawDebugLines (with an empty list when off) rather than only when
        // showGrid is true -- drawDebugLines replaces (doesn't accumulate) the staged lines
        // each call, see its own doc comment, so skipping the call on the "off" frame would
        // leave the previous frame's grid lines staged and re-drawn indefinitely.
        val lines = if (showGrid) {
            Grid.lines(size = GRID_SIZE, divisions = GRID_DIVISIONS).map { (a, b) -> LineSegment(a, b, GRID_COLOR) }
        } else {
            emptyList()
        }
        renderer.drawDebugLines(lines)

        fixedTimestepLoop.advance(
            frameDelta = delta,
            fixedUpdate = { step ->
                physicsSystem?.update(sceneRuntime.world, step)
            },
            render = {
                sceneRuntime.render(delta)
            }
        )

        logAccumulator += delta
        if (logAccumulator >= 1f) {
            logAccumulator = 0f
            println("PHYSICS DEMO: cube Y = ${cubeTransform.position.y}")
        }
    }

    /** First row this demo contributes to the shared panel column, right below
     * [DemoCatalog]'s own demo-picker dropdown (this demo has no debug-toggle/camera-mode
     * rows of its own, unlike [CubeDemo]). */
    override fun drawPanel(panel: ColumnScope) {
        // width = 0f falls back to the column's own configured width (see
        // ColumnScope.claimSlot) -- this demo doesn't need to know/duplicate that value.
        showGrid = panel.toggle("show-grid", showGrid, 0f, 32f, "GRID")
    }

    /** Releases the mesh/materials this demo created in [ready], and the [PhysicsWorld] it
     * owns -- required now that [DemoCatalog] can switch away from this demo and back, not
     * just construct it once for the whole app's lifetime (same as [CubeDemo.dispose]). */
    override fun dispose() {
        cubeMesh.destroy()
        material.destroy()
        physicsWorld?.destroy()
    }

    override fun debugLines(): List<String> {
        val y = cubeTransform.position.y
        return listOf("PHYSICS: cube Y=${(kotlin.math.round(y * 100f) / 100f)}")
    }

    override fun getCameraEye(): Vec3 = cameraComponent.camera.eye
    override fun setCameraEye(eye: Vec3) {
        cameraComponent.camera.eye = eye
    }
    override fun getCameraCenter(): Vec3 = cameraComponent.camera.center
    override fun setCameraCenter(center: Vec3) {
        cameraComponent.camera.center = center
    }

    companion object {
        private const val SCENE_PATH = "scenes/physics.scene.json"

        // Matches physics.scene.json's ground node scale (10x10 on X/Z) -- the grid spans
        // exactly the ground box's footprint.
        private const val GRID_SIZE = 10f
        private const val GRID_DIVISIONS = 10
        // Dim gray, same subtle-reference-line color CubeDemo's own grid toggle uses.
        private val GRID_COLOR = floatArrayOf(0.4f, 0.4f, 0.4f, 1f)
    }
}
