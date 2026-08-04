// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfMesh
import io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfParser
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.scene.components.Camera as SceneCamera
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Real glTF viewer -- loads Khronos's own `Duck.gltf` reference sample (`assets/models/
 * Duck.gltf`, downloaded from `KhronosGroup/glTF-Sample-Assets`, see that file's sibling
 * `Duck-LICENSE.txt`) via [GltfParser.parse]. `Duck.gltf` is the plain-JSON embedded-buffer
 * form (not a GLB binary container), so [GltfParser.parseScene] -- which requires a GLB
 * header/magic -- doesn't apply here; [GltfParser.parse] is the JSON-text entry point, which
 * reads only the first mesh/primitive (no scene graph) -- exactly Duck's own single-mesh shape.
 * Spawns one ECS entity for that mesh with an identity [Transform.worldMatrix] (Duck's own
 * single node has no meaningful transform to bake in) -- NOT driven by
 * [io.github.ronjunevaldoz.awake.scene.systems.TransformSystem] (not registered on this
 * playground's scene at all): that system recomputes `worldMatrix` from position/rotation/scale
 * every frame, which would silently overwrite a directly-set matrix back to identity -- a real
 * trap caught while designing this migration, not a hypothetical one.
 *
 * The actual byte load ([readResourceBytes]) is suspend, so it can't happen in [Scene3DDemo
 * .onActivate] (a plain, non-suspend per-frame hook) -- [preload] does it once, during the
 * scene's own suspend `onReady` block (see `scene3DPlaygroundModule`).
 *
 * [ensureSpawned] (not just [onActivate]) is what actually spawns the mesh/camera entities,
 * and is called from both [onActivate] AND every [onUpdate] tick, idempotently (no-ops once
 * [meshEntity] exists) -- `SceneGameRuntime.ready()` runs an "initial sync pass" over every
 * Infrastructure system (this demo's activation driver included) BEFORE `onReadyBlock`/[preload]
 * ever runs, so if this demo happens to be the very first one active, its first [onActivate]
 * call is guaranteed to see [loadedMesh] still `null` -- a real race this project's own demo
 * driver hit, not a hypothetical one (see `Scene3DPlaygroundFeature.kt`'s own comment on system
 * registration order). Retrying from [onUpdate] instead of only [onActivate] makes this
 * self-healing once [preload] actually finishes, regardless of activation timing. */
internal object GltfViewerDemo {
    private var loadedMesh: GltfMesh? = null
    private var autoRotate = true
    private var orbitDegrees = 20f
    private var pitchDegrees = 15f
    private var zoom = DEFAULT_ZOOM

    /** Bounding-sphere radius of [loadedMesh]'s raw positions -- [GltfParser.parse] reads only
     * raw mesh/primitive attributes, no scene-graph node transform (glTF's usual place for a
     * corrective scale, e.g. Duck.gltf's own node scales its ~100-unit-tall raw mesh down to
     * roughly 1 unit) -- so [zoom]'s default/range is fit to whatever scale the mesh data
     * actually is, rather than assuming a roughly-unit-sized model like [DEFAULT_ZOOM] would. */
    private var modelRadius = 1f

    private var meshEntity: Entity? = null
    private var cameraEntity: Entity? = null

    suspend fun preload() {
        if (loadedMesh != null) return
        val bytes = readResourceBytes("assets/models/Duck.gltf")
        val mesh = GltfParser.parse(bytes.decodeToString())
        loadedMesh = mesh
        modelRadius = boundingRadius(mesh.positions)
        zoom = modelRadius * ZOOM_FIT_FACTOR
    }

    /** Largest distance from the origin across every vertex position -- a cheap bounding-sphere
     * radius, good enough to pick a zoom that frames the whole model without computing a real
     * AABB. */
    private fun boundingRadius(positions: FloatArray): Float {
        var maxDistanceSquared = 0f
        var i = 0
        while (i < positions.size) {
            val x = positions[i]
            val y = positions[i + 1]
            val z = positions[i + 2]
            val distanceSquared = x * x + y * y + z * z
            if (distanceSquared > maxDistanceSquared) maxDistanceSquared = distanceSquared
            i += 3
        }
        return if (maxDistanceSquared > 0f) sqrt(maxDistanceSquared) else 1f
    }

    val entry = Scene3DDemo(
        id = "gltf-viewer",
        title = "glTF viewer",
        renderViewport = {
            // Real geometry is drawn by RenderSystem via this demo's own ECS entities (see
            // onActivate below) -- nothing UI-authored belongs in this viewport column.
        },
        renderControls = {
            shadcnSurface(id = "gltf-controls-panel", modifier = Modifier.fillMaxWidth()) {
                autoRotate = shadcnSwitch(id = "gltf-auto-rotate", checked = autoRotate, label = "Auto-rotate")
                orbitDegrees = shadcnFieldSliderWithValue(id = "gltf-orbit", label = "Orbit", min = 0f, max = 360f, value = orbitDegrees, enabled = !autoRotate)
                pitchDegrees = shadcnFieldSliderWithValue(id = "gltf-pitch", label = "Pitch", min = -80f, max = 80f, value = pitchDegrees)
                zoom = shadcnFieldSliderWithValue(
                    id = "gltf-zoom",
                    label = "Zoom",
                    min = modelRadius * 0.5f,
                    max = modelRadius * 20f,
                    value = zoom
                )
            }
        },
        onActivate = { ensureSpawned(this) },
        onDeactivate = { world ->
            meshEntity?.let { world.destroy(it) }
            meshEntity = null
            cameraEntity?.let { world.destroy(it) }
            cameraEntity = null
        },
        onUpdate = { delta ->
            ensureSpawned(this)
            if (autoRotate) orbitDegrees = (orbitDegrees + delta * ORBIT_DEGREES_PER_SECOND) % 360f
            cameraEntity?.let { entity -> world.add(entity, SceneCamera(computeCamera(), isPrimary = true)) }
        }
    )

    private fun ensureSpawned(runtime: SceneGameRuntime) {
        if (meshEntity != null) return
        val mesh = loadedMesh ?: return
        val geometry = MeshGeometry(mesh.toInterleavedPositionNormalColor(), mesh.indices, format = VertexFormat.PositionNormalColor)
        val mesh3d = runtime.renderer.createMesh(geometry)
        val material = runtime.renderer.createMaterial()
        val entity = runtime.world.create()
        runtime.world.add(entity, Transform())
        runtime.world.add(entity, MeshRenderer(mesh3d, material))
        meshEntity = entity
        val camera = runtime.world.create()
        runtime.world.add(camera, SceneCamera(computeCamera(), isPrimary = true))
        cameraEntity = camera
    }

    /** Simple orbit around the model's origin -- [orbitDegrees]/[pitchDegrees]/[zoom] mirror
     * [RotatingCubeDemo]'s own orbit math (see that object's `computeCamera` doc comment), just
     * without the free-look/lock-target modes this single-model viewer has no use for. */
    private fun computeCamera(): CoreCamera {
        val orbitRad = orbitDegrees * DEGREES_TO_RADIANS
        val pitchRad = pitchDegrees * DEGREES_TO_RADIANS
        val horizontalRadius = zoom * cos(pitchRad)
        val eye = Vec3(
            horizontalRadius * sin(orbitRad),
            zoom * sin(pitchRad),
            horizontalRadius * cos(orbitRad)
        )
        return CoreCamera(
            eye = eye,
            center = Vec3(0f, 0f, 0f),
            fovYRadians = 45f * DEGREES_TO_RADIANS,
            near = 0.1f,
            far = 10000f
        )
    }

    private const val ORBIT_DEGREES_PER_SECOND = 15f
    private const val DEGREES_TO_RADIANS = (PI / 180.0).toFloat()

    /** Pre-[preload] fallback only -- overwritten with `modelRadius * ZOOM_FIT_FACTOR` the
     * moment real bounding data is available. */
    private const val DEFAULT_ZOOM = 200f

    /** How many bounding-sphere radii away the camera sits by default -- far enough that a
     * roughly-spherical model (like Duck) doesn't clip the near plane at default pitch/orbit. */
    private const val ZOOM_FIT_FACTOR = 2.5f
}
