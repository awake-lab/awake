// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.systems

import io.github.ronjunevaldoz.awake.core.math.Frustum
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
import io.github.ronjunevaldoz.awake.scene.rendering.components.Light
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
                pbr != null -> floatArrayOf(pbr.metallic, pbr.roughness, 0f, 0f)
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
        renderer.draw(camera.camera, drawCalls, sceneLight(world))
    }

    private fun primaryCamera(world: World): Camera? {
        val family = world.family<Camera>()
        val cameras = family.components()
        var index = 0
        val count = family.size
        while (index < count) {
            val camera = cameras[index]
            if (camera.isPrimary) {
                return camera
            }
            index += 1
        }
        return null
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

/** ponytail: `RenderSystem` doesn't know the renderer's actual viewport aspect ratio (`System
 * .update(world, delta)` takes no viewport size, and `Renderer` doesn't expose one) -- widening
 * the frustum's horizontal extent past any real device's aspect ratio (ultra-wide monitors
 * included) keeps this a false-negative-only approximation: it may under-cull on a narrow
 * screen, never cull something actually visible. Upgrade path: expose the renderer's real
 * aspect ratio and pass it through here once that's a free contract change to make. */
private const val CONSERVATIVE_ASPECT = 3f
