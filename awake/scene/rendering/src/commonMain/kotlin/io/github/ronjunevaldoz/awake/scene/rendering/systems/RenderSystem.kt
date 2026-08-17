// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.systems

import io.github.ronjunevaldoz.awake.core.math.Frustum
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.intersects
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.render.renderer.DEFAULT_SCENE_LIGHT
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.rendering.components.Camera
import io.github.ronjunevaldoz.awake.scene.rendering.components.InstancedMeshRenderer
import io.github.ronjunevaldoz.awake.scene.rendering.components.InstancedSkinnedMeshRenderer
import io.github.ronjunevaldoz.awake.scene.rendering.components.Light
import io.github.ronjunevaldoz.awake.scene.rendering.components.LodGroup
import io.github.ronjunevaldoz.awake.scene.rendering.components.MeshBounds
import io.github.ronjunevaldoz.awake.scene.rendering.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.rendering.components.PbrMaterial
import io.github.ronjunevaldoz.awake.scene.rendering.components.SkinnedPose

class RenderSystem(
    private val renderer: Renderer,
) : System {
    private val drawCalls = ArrayList<DrawCall>()

    override fun update(world: World, delta: Float) {
        val camera = primaryCamera(world) ?: return
        drawCalls.clear()
        world.family<Transform, MeshRenderer>().forEach { entity, transform, meshRenderer ->
            // An entity's mesh format decides which pipeline draws it (see Renderer
            // .pipelinesByFormat) -- extraUniformFloats only matters for a format whose
            // shader reads it (a skinned mesh's joint palette); every other format ignores
            // an empty array the same way it always has.
            val pose = world.get<SkinnedPose>(entity)
            val pbr = world.get<PbrMaterial>(entity)
            val extras = when {
                pose != null -> pose.jointPalette
                // [metallic, roughness, pad, pad, baseColorFactor.rgba, emissiveFactor.rgb,
                // pad] -- the primary lit pipeline only reads the first 4 (see
                // RendererDraw3D.pbrMaterialFloats), the textured/glTF pipeline reads all 12
                // (see that backend's own pbr-factor helper). One packing serves both since
                // which pipeline a given mesh format resolves to is a backend concern, not
                // this system's.
                pbr != null -> floatArrayOf(
                    pbr.metallic, pbr.roughness, 0f, 0f,
                    pbr.baseColorFactor[0], pbr.baseColorFactor[1], pbr.baseColorFactor[2], pbr.baseColorFactor[3],
                    pbr.emissiveFactor[0], pbr.emissiveFactor[1], pbr.emissiveFactor[2], 0f,
                )
                else -> EMPTY_EXTRAS
            }
            val bounds = world.get<MeshBounds>(entity)
            if (bounds != null) {
                val worldBounds = bounds.localBounds.transformed(transform.worldMatrix)
                if (!Frustum.intersects(camera.camera, CONSERVATIVE_ASPECT, worldBounds)) return@forEach
            }
            drawCalls.add(
                DrawCall(
                    mesh = meshRenderer.mesh,
                    material = meshRenderer.material,
                    model = transform.worldMatrix,
                    extraUniformFloats = extras,
                ),
            )
        }
        // One DrawCall per entity here too, but instanceModels (not model) carries every
        // copy's transform -- a backend with an instanced pipeline for this mesh's format
        // draws all of them in one GPU call. See InstancedMeshRenderer's own doc comment for
        // why this is a separate opt-in component/query rather than folding into the
        // MeshRenderer loop above.
        world.family<InstancedMeshRenderer>().forEach { _, instanced ->
            drawCalls.add(
                DrawCall(
                    mesh = instanced.mesh,
                    material = instanced.material,
                    instanceModels = instanced.transforms,
                ),
            )
        }
        // Animated counterpart to the InstancedMeshRenderer loop above -- instanceModels AND
        // instanceJointPalettes both carry one entry per instance, index-for-index. See
        // InstancedSkinnedMeshRenderer's own doc comment for why this is a separate component
        // rather than folding into InstancedMeshRenderer.
        world.family<InstancedSkinnedMeshRenderer>().forEach { _, instanced ->
            drawCalls.add(
                DrawCall(
                    mesh = instanced.mesh,
                    material = instanced.material,
                    instanceModels = instanced.instances.map { it.transform },
                    instanceJointPalettes = instanced.instances.map { it.jointPalette },
                ),
            )
        }
        // LodGroup picks ONE level's mesh/material by distance to the camera eye -- see that
        // component's own doc comment for why an entity carries this instead of MeshRenderer,
        // not both. LOD selects detail, it doesn't cull -- MeshBounds/frustum culling still
        // applies on top when present.
        world.family<Transform, LodGroup>().forEach { entity, transform, lodGroup ->
            val worldPosition = Vec3(transform.worldMatrix.m03, transform.worldMatrix.m13, transform.worldMatrix.m23)
            val distance = (worldPosition - camera.camera.eye).length3()
            val level = lodGroup.levels.firstOrNull { distance <= it.maxDistance } ?: lodGroup.levels.last()

            val bounds = world.get<MeshBounds>(entity)
            if (bounds != null) {
                val worldBounds = bounds.localBounds.transformed(transform.worldMatrix)
                if (!Frustum.intersects(camera.camera, CONSERVATIVE_ASPECT, worldBounds)) return@forEach
            }
            drawCalls.add(DrawCall(mesh = level.mesh, material = level.material, model = transform.worldMatrix))
        }
        renderer.draw(camera.camera, drawCalls, sceneLight(world))
    }

    /** The first [Light] entity in the world, converted to render-api's backend-neutral
     * [SceneLight] -- [DEFAULT_SCENE_LIGHT] (the same direction/color every lit shader
     * hardcoded before this existed) when a scene has no `Light` entity at all, so an
     * un-lit scene renders exactly as it always did. */
    private fun sceneLight(world: World): SceneLight {
        val family = world.family<Light>()
        val light = family.components().firstOrNull() ?: return DEFAULT_SCENE_LIGHT
        return SceneLight(direction = light.direction, color = light.color * light.intensity)
    }
}

private val EMPTY_EXTRAS = FloatArray(0)
