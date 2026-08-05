// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.render.renderer.DEFAULT_SCENE_LIGHT
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.Light
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.Transform
import kotlin.collections.ArrayList

class RenderSystem(
    private val renderer: Renderer
) : System {
    private val drawCalls = ArrayList<DrawCall>()

    override fun update(world: World, delta: Float) {
        val camera = primaryCamera(world) ?: return
        val family = world.family<Transform, MeshRenderer>()
        val transforms = family.componentsA()
        val meshRenderers = family.componentsB()
        drawCalls.clear()
        var index = 0
        val count = family.size
        while (index < count) {
            val transform = transforms[index]
            val meshRenderer = meshRenderers[index]
            drawCalls.add(
                DrawCall(
                    mesh = meshRenderer.mesh,
                    material = meshRenderer.material,
                    model = transform.worldMatrix
                )
            )
            index += 1
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
