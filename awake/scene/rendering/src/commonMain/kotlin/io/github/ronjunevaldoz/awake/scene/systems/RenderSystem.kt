// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.render.renderer.DEFAULT_SCENE_LIGHT
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.Light
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.SkinnedPose
import io.github.ronjunevaldoz.awake.scene.components.Transform
import kotlin.collections.ArrayList

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
            drawCalls.add(
                if (pose != null) {
                    DrawCall(
                        mesh = meshRenderer.mesh,
                        material = meshRenderer.material,
                        model = transform.worldMatrix,
                        extraUniformFloats = pose.jointPalette,
                    )
                } else {
                    DrawCall(mesh = meshRenderer.mesh, material = meshRenderer.material, model = transform.worldMatrix)
                },
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
