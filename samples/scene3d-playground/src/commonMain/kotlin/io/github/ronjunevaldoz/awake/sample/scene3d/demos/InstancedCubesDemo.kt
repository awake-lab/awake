// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.sample.scene3d.Scene3DDemo
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.Modifier
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.camera
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.scene
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.rendering.components.InstancedMeshRenderer
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera

/**
 * Thousands of copies of ONE cube mesh in a single GPU draw call -- the only demo that exercises
 * [InstancedMeshRenderer] (and therefore both backends' instanced pipeline + instance buffer).
 * Every copy reads its own model matrix from an instance-rate vertex buffer, so the cube count
 * costs one extra `mat4` each rather than a whole draw call + uniform write each.
 *
 * Needs the `instanced` shader set wired into the bootstrap (see
 * `Scene3DPlaygroundVulkanBootstrap`/`...WebGpuBootstrap`); without it the backend has no
 * instanced pipeline for this format and the whole draw call is skipped, drawing nothing.
 */
internal object InstancedCubesDemo {
    private var cameraEntity: Entity? = null
    private var cubesEntity: Entity? = null
    private var panningEntity: Entity? = null

    // InstancedMeshRenderer carries no Transform of its own (each copy's matrix lives in the
    // transform list), but updateDemoCamera follows an entity's Transform -- so the camera
    // tracks this stand-in at the lattice's center instead.
    private var anchorEntity: Entity? = null

    private var mesh: Mesh? = null
    private var material: Material? = null

    private var gridSide = DEFAULT_GRID_SIDE
    private var builtGridSide = 0

    val entry = Scene3DDemo(
        id = "instanced-cubes",
        title = "Instanced cubes",
        renderViewport = { },
        renderControls = { scope ->
            val config = cameraEntity?.let { world.get<CameraComponent>(it) }
            if (config != null) {
                scope.renderCameraModeToggle(config.mode) { config.mode = it }
            }
            cameraEntity?.let { scope.renderProjectionControls(world, it, idPrefix = "instanced") }
            gridSide = scope.shadcnFieldSliderWithValue(
                id = "instanced-grid-side",
                label = "Grid side",
                min = MIN_GRID_SIDE.toFloat(),
                max = MAX_GRID_SIDE.toFloat(),
                value = gridSide,
            )
            scope.shadcnText(
                label = "${cubeCount()} cubes, one draw call. Side N renders N^3 copies of the same mesh.",
            )
        },
        onActivate = { ensureSpawned(this) },
        onDeactivate = { world ->
            cubesEntity?.let { world.destroy(it) }
            cubesEntity = null
            cameraEntity?.let { world.destroy(it) }
            cameraEntity = null
            anchorEntity?.let { world.destroy(it) }
            anchorEntity = null
            panningEntity?.let { world.destroy(it) }
            panningEntity = null
            builtGridSide = 0
        },
        onUpdate = {
            ensureSpawned(this)
            renderer.drawReferenceGrid()

            // Rebuilt only when the slider actually moved -- building N^3 matrices every frame
            // would allocate thousands of Mat4s per frame for a list that rarely changes.
            val side = gridSide.toInt()
            if (side != builtGridSide) {
                cubesEntity?.let { entity ->
                    world.get<InstancedMeshRenderer>(entity)?.let { existing ->
                        world.add(entity, existing.copy(transforms = gridTransforms(side)))
                    }
                }
                builtGridSide = side
            }

            updateDemoCamera(
                world = world,
                cameraEntity = cameraEntity!!,
                targetEntity = anchorEntity!!,
                panningEntity = panningEntity,
                onPanningEntityCreated = { panningEntity = it },
            )
        },
    )

    private fun cubeCount(): Int = gridSide.toInt().let { it * it * it }

    /** An `side^3` lattice of unit cubes centered on the origin, [SPACING] apart. Deterministic
     * (not randomized) so a pixel comparison of the same slider value is reproducible. */
    private fun gridTransforms(side: Int): List<Mat4> {
        val offset = (side - 1) * SPACING / 2f
        val total = side * side * side
        val transforms = ArrayList<Mat4>(total)
        // One flat loop with the lattice index decomposed, not three nested ones -- same cubes,
        // and it keeps this readable at a glance.
        for (index in 0 until total) {
            val x = index / (side * side)
            val y = (index / side) % side
            val z = index % side
            // Written directly rather than via translate()/scale(), whose chained convention is
            // easy to get backwards -- this is a plain scale-then-translate TRS: mRC accessors,
            // column-major, translation in column 3.
            transforms += Mat4().apply {
                m00 = CUBE_SCALE
                m11 = CUBE_SCALE
                m22 = CUBE_SCALE
                m03 = x * SPACING - offset
                m13 = y * SPACING - offset + LIFT
                m23 = z * SPACING - offset
            }
        }
        return transforms
    }

    private fun ensureSpawned(runtime: SceneGameRuntime) {
        if (cubesEntity != null) return
        val cubeMesh = mesh ?: runtime.renderer.createMesh(rotatingCubeGeometry).also { mesh = it }
        val cubeMaterial = material
            ?: runtime.renderer.createMaterial(uniformFloatCount = INSTANCED_UNIFORM_FLOAT_COUNT)
                .also { material = it }

        val side = gridSide.toInt()
        runtime.world.scene {
            cameraEntity = entity(
                "Camera",
                Modifier().camera(
                    target = null,
                    lens = CoreCamera.perspective(
                        eye = Vec3(0f, CAMERA_HEIGHT, CAMERA_DISTANCE),
                        center = Vec3(0f, LIFT, 0f),
                    ),
                ),
            )
        }
        anchorEntity = runtime.world.create().also { entity ->
            runtime.world.add(entity, Transform().apply { position.set(Vec3(0f, LIFT, 0f)) })
        }
        cubesEntity = runtime.world.create().also { entity ->
            runtime.world.add(
                entity,
                InstancedMeshRenderer(cubeMesh, cubeMaterial, gridTransforms(side)),
            )
        }
        builtGridSide = side
    }

    /** `instanced.wgsl`'s Uniforms: viewProjection(16) + lightDirection(4) + lightColor(4). No
     * per-draw model matrix -- that's what the instance buffer carries. */
    private const val INSTANCED_UNIFORM_FLOAT_COUNT = 24

    private const val DEFAULT_GRID_SIDE = 10f
    private const val MIN_GRID_SIDE = 2
    private const val MAX_GRID_SIDE = 12
    private const val SPACING = 1.5f
    private const val CUBE_SCALE = 0.6f
    private const val LIFT = 0.5f
    private const val CAMERA_HEIGHT = 8f
    private const val CAMERA_DISTANCE = 24f
}
