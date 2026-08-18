// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.graphics.createBitmap
import io.github.ronjunevaldoz.awake.core.graphics.toRgba8Bytes
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.boundingCenter
import io.github.ronjunevaldoz.awake.core.math.boundingRadius
import io.github.ronjunevaldoz.awake.asset.gltf.GltfMesh
import io.github.ronjunevaldoz.awake.asset.gltf.GltfParser
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.TexturedUniformLayout
import io.github.ronjunevaldoz.awake.render.texture.PbrTextureSet
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.Modifier
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.camera
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.meshRenderer
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.scene
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.transform
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.rendering.components.PbrMaterial
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSwitch
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera

/**
 * Real glTF viewer -- loads Khronos's own `Duck.gltf` reference sample.
 */
internal object GltfViewerDemo {
    private var cameraEntity: Entity? = null
    private var duckEntity: Entity? = null

    private var loadedMesh: GltfMesh? = null

    private var followTargetEntity: Entity? = null
    private var panningEntity: Entity? = null
    private var showAimMarkers = false
    private var wireframeEnabled = false

    private var normalizedInterleaved: FloatArray? = null
    private var modelRadius = 1f
    private var modelCenter: Vec3 = Vec3(0f, 0f, 0f)
    private var textureAsset: TextureAsset? = null
    private var pbrTextures: PbrTextureSet? = null

    private var spawned = false
    private var mesh: Mesh? = null
    private var material: Material? = null

    suspend fun preload() {
        if (loadedMesh != null) return
        val bytes = readResourceBytes("assets/models/Duck.gltf")
        val gltfMesh = GltfParser.parse(bytes.decodeToString())
        loadedMesh = gltfMesh
        modelRadius = boundingRadius(gltfMesh.positions)
        normalizedInterleaved =
            scalePositions(gltfMesh.toInterleavedPositionNormalColorUv(), 1f / modelRadius)

        // Scale by the same 1/modelRadius the vertices got -- boundingCenter measures the
        // original positions.
        val center = boundingCenter(gltfMesh.positions)
        modelCenter = Vec3(center.x / modelRadius, center.y / modelRadius, center.z / modelRadius)

        textureAsset = decodeTexture(requireNotNull(gltfMesh.baseColorImageBytes))
        pbrTextures = PbrTextureSet(
            metallicRoughness = gltfMesh.metallicRoughnessImageBytes?.let { decodeTexture(it) },
            normal = gltfMesh.normalImageBytes?.let { decodeTexture(it) },
            occlusion = gltfMesh.occlusionImageBytes?.let { decodeTexture(it) },
            emissive = gltfMesh.emissiveImageBytes?.let { decodeTexture(it) },
        )
    }

    private suspend fun decodeTexture(imageBytes: ByteArray): TextureAsset {
        val bitmap = createBitmap(imageBytes)
        return TextureAsset(bitmap.toRgba8Bytes(), bitmap.width, bitmap.height)
    }

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

    val entry = Scene3DDemo(
        id = "gltf-viewer",
        title = "glTF viewer",
        renderViewport = { },
        renderControls = { scope ->
            val config = cameraEntity?.let { world.get<CameraComponent>(it) }
            if (config != null) {
                scope.renderCameraModeToggle(config.mode) { config.mode = it }
            }
            // Before the projection sliders: the column scrolls, and three slider rows push
            // these below the fold.
            showAimMarkers = scope.shadcnSwitch(
                id = "gltf-show-aim-markers",
                checked = showAimMarkers,
                label = "Show aim markers",
            )
            wireframeEnabled = scope.shadcnSwitch(
                id = "gltf-wireframe",
                checked = wireframeEnabled,
                label = "Wireframe",
            )
            // Shared across every demo. The duck itself draws unlit, so the visible effect is
            // on the Rotating cube demo's ground plane.
            renderer.shadowsEnabled = scope.shadcnSwitch(
                id = "gltf-shadows",
                checked = renderer.shadowsEnabled,
                label = "Shadows",
            )
            cameraEntity?.let { scope.renderProjectionControls(world, it, idPrefix = "gltf") }
        },
        onActivate = { ensureSpawned(this) },
        onDeactivate = { world ->
            cameraEntity?.let { world.destroy(it) }
            cameraEntity = null
            duckEntity?.let { world.destroy(it) }
            duckEntity = null
            followTargetEntity?.let { world.destroy(it) }
            followTargetEntity = null
            panningEntity?.let { world.destroy(it) }
            panningEntity = null
            spawned = false
        },
        onUpdate = { delta ->
            ensureSpawned(this)

            renderer.wireframe = wireframeEnabled
            renderer.drawReferenceGrid()

            val fte = followTargetEntity ?: world.create().also {
                followTargetEntity = it
                world.add(it, Transform().apply { position.set(modelCenter) })
            }

            updateDemoCamera(
                world = world,
                cameraEntity = cameraEntity!!,
                targetEntity = fte,
                panningEntity = panningEntity,
                onPanningEntityCreated = { panningEntity = it },
            )

            if (showAimMarkers) {
                val markers = mutableListOf<LineSegment>()
                val mc = modelCenter
                markers += LineSegment(
                    mc,
                    mc + Vec3(0.1f, 0f, 0f),
                    floatArrayOf(0.9f, 0.15f, 0.15f, 1f),
                )
                markers += LineSegment(
                    mc,
                    mc + Vec3(0f, 0.1f, 0f),
                    floatArrayOf(0.15f, 0.75f, 0.15f, 1f),
                )
                markers += LineSegment(
                    mc,
                    mc + Vec3(0f, 0f, 0.1f),
                    floatArrayOf(0.15f, 0.35f, 0.9f, 1f),
                )
                val ft = world.get<Transform>(fte)?.position ?: modelCenter
                markers += LineSegment(ft, ft + Vec3(0.1f, 0f, 0f), floatArrayOf(1f, 1f, 0f, 1f))
                markers += LineSegment(ft, ft + Vec3(0f, 0.1f, 0f), floatArrayOf(1f, 1f, 0f, 1f))
                markers += LineSegment(ft, ft + Vec3(0f, 0f, 0.1f), floatArrayOf(1f, 1f, 0f, 1f))
                renderer.drawDebugLines(markers)
            }
        },
    )

    private fun ensureSpawned(runtime: SceneGameRuntime) {
        if (spawned) return
        if (loadedMesh == null) return
        mesh = createNormalizedMesh(runtime)
        material = runtime.renderer.createMaterial(
            texture = textureAsset,
            uniformFloatCount = TEXTURED_UNIFORM_FLOAT_COUNT,
            pbrTextures = pbrTextures,
        )

        runtime.world.scene {
            cameraEntity = entity(
                "Camera",
                Modifier().camera(
                    target = null,
                    // The mesh is normalized to unit radius above, so frame it at a few radii
                    // rather than a fixed 10 -- that was ~10x too far and read as "zoomed out".
                    lens = CoreCamera.perspective(
                        eye = Vec3(
                            modelCenter.x,
                            modelCenter.y,
                            modelCenter.z + FRAMING_DISTANCE_RADII,
                        ),
                        center = modelCenter,
                    ),
                ),
            )

            duckEntity = entity("Duck", Modifier().transform().meshRenderer(mesh!!, material!!))
        }
        val gltfMesh = requireNotNull(loadedMesh)
        runtime.world.add(
            duckEntity!!,
            PbrMaterial(
                metallic = gltfMesh.metallicFactor,
                roughness = gltfMesh.roughnessFactor,
                baseColorFactor = gltfMesh.baseColorFactor,
                emissiveFactor = gltfMesh.emissiveFactor,
            ),
        )

        spawned = true
    }

    private fun createNormalizedMesh(runtime: SceneGameRuntime) =
        runtime.renderer.createMesh(
            MeshGeometry(
                normalizedInterleaved!!,
                loadedMesh!!.indices,
                format = VertexFormat.PositionNormalColorUv,
            ),
        )

    private const val TEXTURED_VERTEX_STRIDE_COMPONENTS = 11

    /** Camera distance in model radii. The mesh is normalized to unit radius, so ~3 fills the
     * viewport without clipping at the default 45-degree FOV. */
    private const val FRAMING_DISTANCE_RADII = 3f
}

/** `textured.wgsl`'s Uniforms size -- taken from the shared layout rather than re-summed
 * here, so adding a field to that shader can't leave this call site silently short. */
internal val TEXTURED_UNIFORM_FLOAT_COUNT = TexturedUniformLayout.total
