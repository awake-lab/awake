// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.graphics.createBitmap
import io.github.ronjunevaldoz.awake.core.graphics.toRgba8Bytes
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.OrbitCameraController
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.boundingRadius
import io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfMesh
import io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfParser
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.controls.OrbitCameraDemoRig
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth

/**
 * Real glTF viewer -- loads Khronos's own `Duck.gltf` reference sample (`assets/models/
 * Duck.gltf`, downloaded from `KhronosGroup/glTF-Sample-Assets`, see that file's sibling
 * `Duck-LICENSE.txt`) via [GltfParser.parse]. `Duck.gltf` is the plain-JSON embedded-buffer
 * form (not a GLB binary container), so [GltfParser.parseScene] -- which requires a GLB
 * header/magic -- doesn't apply here; [GltfParser.parse] is the JSON-text entry point, which
 * reads only the first mesh/primitive (no scene graph) -- exactly Duck's own single-mesh shape.
 * Duck's material has a real embedded `baseColorTexture` (base64 PNG, see
 * [GltfMesh.baseColorImageBytes]'s own doc comment), decoded once in [preload] via the shared
 * [io.github.ronjunevaldoz.awake.core.graphics.createBitmap]/[toRgba8Bytes] utility.
 *
 * Renders through the ordinary [MeshRenderer]/[io.github.ronjunevaldoz.awake.scene.systems
 * .RenderSystem] path, same as every other demo -- [rig]'s placement entity carries
 * [MeshRenderer] directly, its mesh's own [VertexFormat.PositionNormalColorUv] resolving to
 * the `textured.wgsl` pipeline via `Renderer.pipelinesByFormat` (see that property's own doc
 * comment). Scale is applied via [Transform.worldMatrix] each frame, same as [SpinSystem]
 * writes it, not passed to a bespoke draw call.
 *
 * `Renderer.drawTexturedMesh` (the bypass this demo used before `RenderSystem` supported
 * more than one vertex format) is gone now that nothing needs it.
 *
 * The actual byte load ([readResourceBytes]) is suspend, so it can't happen in [Scene3DDemo
 * .onActivate] (a plain, non-suspend per-frame hook) -- [preload] does it once, during the
 * scene's own suspend `onReady` block (see `scene3DPlaygroundModule`).
 *
 * [ensureSpawned] (not just [onActivate]) is what actually builds the mesh/material/camera
 * entity, and is called from both [onActivate] AND every [onUpdate] tick, idempotently (no-ops
 * once [spawned] is `true`) -- `SceneGameRuntime.ready()` runs an "initial sync pass" over every
 * Infrastructure system (this demo's activation driver included) BEFORE `onReadyBlock`/[preload]
 * ever runs, so if this demo happens to be the very first one active, its first [onActivate]
 * call is guaranteed to see [loadedMesh] still `null` -- a real race this project's own demo
 * driver hit, not a hypothetical one (see `Scene3DPlaygroundFeature.kt`'s own comment on system
 * registration order). Retrying from [onUpdate] instead of only [onActivate] makes this
 * self-healing once [preload] actually finishes, regardless of activation timing. */
internal object GltfViewerDemo {
    private val rig = OrbitCameraDemoRig(OrbitCameraController())
    private val camera get() = rig.camera

    private var loadedMesh: GltfMesh? = null
    private var autoRotate = true

    /** [loadedMesh] interleaved once at [preload] time, in BOTH scales -- raw (Duck's own
     * ~170-unit-radius authored coordinates) and pre-normalized (positions baked down to a ~1
     * unit bounding radius, [modelRadius]'s own reciprocal already multiplied in). Only
     * [scaleMultiplier] (0.25x-4x) is ever applied at runtime via [Transform.worldMatrix]
     * now -- see that field's own doc comment for why combining the ~0.0059
     * `1/modelRadius` factor with Duck's raw ~170-unit vertex coordinates in the SAME per-frame
     * GPU matrix multiply was worth eliminating outright, not just working around. */
    private var rawInterleaved: FloatArray? = null
    private var normalizedInterleaved: FloatArray? = null

    /** `true` (default): [normalizedInterleaved] is bound instead of [rawInterleaved] (the
     * mesh's own bounding radius already baked down to ~1 unit at [preload] time), and [camera]
     * uses the same small, friendly zoom range every other demo's viewport uses (2..15,
     * [OrbitCameraController]'s own defaults) -- a raw glTF file's units are whatever its
     * authoring tool used (Duck.gltf's own ~170-unit bounding radius, since [GltfParser.parse]
     * reads only raw mesh data, no scene-graph node transform to apply glTF's usual corrective
     * scale -- see [modelRadius]'s own doc comment), so without this every new model would need
     * its own hand-tuned zoom range instead of just working. `false`: [rawInterleaved] is bound,
     * the model renders at its real raw scale, and [applyScaleMode] widens [camera]'s zoom/pan/far
     * range to match instead. */
    private var normalizeScale = true

    /** Fine-tune on top of [normalizeScale]'s automatic normalization -- 1x is "exactly fills the
     * same ~1-unit bounding sphere every other demo's content uses". Applied every [onUpdate] tick
     * to [Transform.worldMatrix] (unlike the `1/modelRadius` factor, which
     * is baked into [normalizedInterleaved] once instead -- see that field's own doc comment)
     * since its whole 0.25x-4x range is safe to combine with already-normalized ~1-unit
     * coordinates in one GPU matrix multiply; nothing here approaches the precision cliff
     * `1/modelRadius` sat on. */
    private var scaleMultiplier = 1f

    /** Which of [rawInterleaved]/[normalizedInterleaved] [mesh]'s current GPU mesh was built
     * from -- `null` before the first [ensureSpawned]. Compared against [normalizeScale] every
     * [onUpdate] tick so toggling the switch rebinds the GPU mesh (destroy + recreate from
     * the other cached array) instead of scaling the same raw-coordinate mesh differently, which
     * is what reintroduced the precision cliff this whole split exists to avoid. */
    private var meshIsNormalized: Boolean? = null

    /** Bounding-sphere radius of [loadedMesh]'s raw positions -- [GltfParser.parse] reads only
     * raw mesh/primitive attributes, no scene-graph node transform (glTF's usual place for a
     * corrective scale, e.g. Duck.gltf's own node scales its ~170-unit-radius raw mesh down to
     * roughly 1 unit) -- [normalizeScale] redoes that same correction generically, and
     * [applyScaleMode] fits [camera] to whichever mode is active. */
    private var modelRadius = 1f

    /** Duck's `baseColorTexture`, decoded once in [preload] (CPU-only, no GPU resource yet) --
     * consumed by [ensureSpawned] to build [material] via `renderer.createMaterial(texture =
     * ...)` once a [SceneGameRuntime] (and its `renderer`) actually exists. */
    private var textureAsset: TextureAsset? = null

    private var spawned = false
    private var mesh: Mesh? = null
    private var material: Material? = null

    init {
        camera.orbitDegrees = 20f
    }

    suspend fun preload() {
        if (loadedMesh != null) return
        val bytes = readResourceBytes("assets/models/Duck.gltf")
        val gltfMesh = GltfParser.parse(bytes.decodeToString())
        loadedMesh = gltfMesh
        modelRadius = boundingRadius(gltfMesh.positions)
        rawInterleaved = gltfMesh.toInterleavedPositionNormalColorUv()
        normalizedInterleaved = scalePositions(rawInterleaved!!, 1f / modelRadius)
        applyScaleMode()

        val imageBytes = requireNotNull(gltfMesh.baseColorImageBytes) {
            "Duck.gltf's material has no baseColorTexture -- GltfViewerDemo expects a real texture."
        }
        val bitmap = createBitmap(imageBytes)
        textureAsset = TextureAsset(bitmap.toRgba8Bytes(), bitmap.width, bitmap.height)
    }

    /** Multiplies every position component (the first 3 of each 11-float
     * [GltfMesh.toInterleavedPositionNormalColorUv] stride -- normal, color, and uv are left
     * untouched) by [factor], returning a new array -- [source] is never mutated, since both
     * [rawInterleaved] and [normalizedInterleaved] need to stay independently valid for as long
     * as [normalizeScale] can still be toggled back. `internal`, not `private`, so
     * `GltfViewerScalePositionsTest` (`desktopTest`) can verify this exact baking math directly,
     * fast and without a GPU -- this is the actual mechanism the Duck flicker fix relies on. */
    internal fun scalePositions(source: FloatArray, factor: Float): FloatArray {
        val result = source.copyOf()
        var i = 0
        while (i < result.size) {
            result[i] *= factor
            result[i + 1] *= factor
            result[i + 2] *= factor
            i += TEXTURED_VERTEX_STRIDE_COMPONENTS
        }
        return result
    }

    /** Fits [camera]'s zoom/pan/far range to whichever of [normalizeScale]'s two modes is
     * active -- called once after [preload] resolves [modelRadius], and again any time
     * [normalizeScale] itself is toggled from the controls panel. */
    private fun applyScaleMode() {
        if (normalizeScale) {
            camera.zoomMin = 2f
            camera.zoomMax = 15f
            camera.zoom = 6f
            camera.panRange = 5f
            camera.far = 100f
        } else {
            camera.zoomMin = modelRadius * 0.5f
            camera.zoomMax = modelRadius * 20f
            camera.zoom = modelRadius * ZOOM_FIT_FACTOR
            camera.panRange = modelRadius * 2f
            // OrbitCameraController's own `far = 100f` default only suits a roughly-unit-scale
            // model -- Duck's own raw-scale auto-fit zoom (~424 units) sits well past that, so
            // the model was clipped by the far plane entirely (fully blank viewport, no error)
            // until this scaled it to the model's real size.
            camera.far = camera.zoomMax * 2f
        }
    }

    val entry = Scene3DDemo(
        id = "gltf-viewer",
        title = "glTF viewer",
        renderViewport = {
            // Real geometry is drawn by RenderSystem via this demo's own ECS entity (see
            // onActivate/onUpdate below) -- this viewport column only needs to exist so the
            // shell lays out a center pane for it.
        },
        renderControls = {
            shadcnSurface(id = "gltf-controls-panel", modifier = Modifier.fillMaxWidth()) {
                autoRotate = shadcnSwitch(id = "gltf-auto-rotate", checked = autoRotate, label = "Auto-rotate")
                val previousNormalizeScale = normalizeScale
                normalizeScale = shadcnSwitch(id = "gltf-normalize-scale", checked = normalizeScale, label = "Normalize scale")
                if (normalizeScale != previousNormalizeScale) applyScaleMode()
                scaleMultiplier = shadcnFieldSliderWithValue(
                    id = "gltf-scale",
                    label = "Scale",
                    min = 0.25f,
                    max = 4f,
                    value = scaleMultiplier,
                    enabled = normalizeScale
                )
            }
            renderOrbitCameraControls(camera, idPrefix = "gltf", targetLabel = "model")
        },
        onActivate = { ensureSpawned(this) },
        onDeactivate = { world ->
            rig.destroy(world)
            spawned = false
        },
        onUpdate = { delta ->
            ensureSpawned(this)
            if (mesh != null && meshIsNormalized != normalizeScale) rebindMesh(this)
            if (autoRotate) camera.orbitDegrees = (camera.orbitDegrees + delta * ORBIT_DEGREES_PER_SECOND) % 360f
            // Only scaleMultiplier here now -- the 1/modelRadius normalization is already
            // baked into normalizedInterleaved (see rebindMesh/scalePositions), not
            // recombined with raw ~170-unit coordinates in this per-frame matrix anymore.
            val worldScale = if (normalizeScale) scaleMultiplier else 1f
            rig.entity?.let { world.get<Transform>(it)?.worldMatrix = Mat4().scale(worldScale, worldScale, worldScale) }
            rig.refresh(world, MODEL_CENTER)
        }
    )

    private fun ensureSpawned(runtime: SceneGameRuntime) {
        if (spawned) return
        if (loadedMesh == null) return
        mesh = createMeshForCurrentScaleMode(runtime)
        material = runtime.renderer.createMaterial(texture = textureAsset)
        meshIsNormalized = normalizeScale
        rig.spawn(runtime.world, MODEL_CENTER)
        runtime.world.add(rig.entity!!, MeshRenderer(mesh!!, material!!))
        spawned = true
    }

    /** Destroys [mesh]'s current GPU mesh and rebuilds it from whichever of
     * [rawInterleaved]/[normalizedInterleaved] now matches [normalizeScale] -- called from
     * [onUpdate] the first tick after the controls panel flips the switch. Keeps the existing
     * [material] (color/shading is scale-independent, no reason to churn it), only [mesh] and
     * [meshIsNormalized] change -- the entity's [MeshRenderer] is re-added to reference the
     * new [mesh] (`world.add` replaces the existing component, see [World.add][io.github
     * .ronjunevaldoz.awake.ecs.World.add]'s own doc comment). */
    private fun rebindMesh(runtime: SceneGameRuntime) {
        mesh?.destroy()
        mesh = createMeshForCurrentScaleMode(runtime)
        meshIsNormalized = normalizeScale
        runtime.world.add(rig.entity!!, MeshRenderer(mesh!!, material!!))
    }

    private fun createMeshForCurrentScaleMode(runtime: SceneGameRuntime) =
        runtime.renderer.createMesh(
            MeshGeometry(
                if (normalizeScale) normalizedInterleaved!! else rawInterleaved!!,
                loadedMesh!!.indices,
                format = VertexFormat.PositionNormalColorUv
            )
        )

    private val MODEL_CENTER = Vec3(0f, 0f, 0f)

    private const val ORBIT_DEGREES_PER_SECOND = 15f

    /** How many bounding-sphere radii away the camera sits by default in raw-scale mode --
     * far enough that a roughly-spherical model (like Duck) doesn't clip the near plane at
     * default pitch/orbit. */
    private const val ZOOM_FIT_FACTOR = 2.5f

    /** [GltfMesh.toInterleavedPositionNormalColorUv]'s own stride (position vec3 + normal vec3 +
     * color vec3 + uv vec2 = 11 floats/vertex) -- re-stated here since that constant is private to
     * [GltfMesh], for [scalePositions] to walk position components only. */
    private const val TEXTURED_VERTEX_STRIDE_COMPONENTS = 11
}
