// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.Transform
import kotlin.collections.ArrayList

class RenderSystem(
    private val renderer: Renderer
) : System {
    override val frequency: io.github.ronjunevaldoz.awake.ecs.SystemFrequency = io.github.ronjunevaldoz.awake.ecs.SystemFrequency.Infrastructure

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
        renderer.draw(camera.camera, drawCalls)
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
}
