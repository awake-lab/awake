/*
 * Awake
 * Awake.awake-scene.commonMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.core.renderer.DrawCall
import io.github.ronjunevaldoz.awake.core.renderer.Renderer
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.Transform

class RenderSystem(
    private val renderer: Renderer
) : System {
    override fun update(world: World, delta: Float) {
        val camera = primaryCamera(world) ?: return
        val drawCalls = mutableListOf<DrawCall>()
        world.queryEach<Transform, MeshRenderer> { _, transform, meshRenderer ->
            drawCalls += DrawCall(
                mesh = meshRenderer.mesh,
                material = meshRenderer.material,
                model = transform.worldMatrix
            )
        }
        renderer.draw(camera.camera, drawCalls)
    }

    private fun primaryCamera(world: World): Camera? {
        var primary: Camera? = null
        world.queryEach<Camera> { _, camera ->
            if (primary == null && camera.isPrimary) {
                primary = camera
            }
        }
        return primary
    }
}
