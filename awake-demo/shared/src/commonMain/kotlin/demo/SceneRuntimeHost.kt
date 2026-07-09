/*
 * Awake
 * Awake.awake-demo.shared.commonMain
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

package demo

import io.github.ronjunevaldoz.awake.core.renderer.Renderer
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.runtime.SceneLoader
import io.github.ronjunevaldoz.awake.scene.runtime.SceneRenderableRequest
import io.github.ronjunevaldoz.awake.scene.runtime.instantiate
import io.github.ronjunevaldoz.awake.scene.runtime.attachRenderableComponents
import io.github.ronjunevaldoz.awake.scene.systems.RenderSystem
import io.github.ronjunevaldoz.awake.scene.systems.TransformSystem

internal class SceneRuntimeHost(
    renderer: Renderer,
    private val resolveRenderable: (SceneRenderableRequest) -> MeshRenderer
) {
    private val world = World()
    private val transformSystem = TransformSystem()
    private val renderSystem = RenderSystem(renderer)
    private val cubeTransform: Transform
    private var elapsedSeconds = 0f

    init {
        val scene = SceneLoader.loadFromResource(SCENE_PATH).instantiate(world)
        scene.attachRenderableComponents(resolveRenderable)
        cubeTransform = scene.root("cube")
            ?: error("MVP scene is missing a root node named 'cube'.")
    }

    fun update(delta: Float) {
        elapsedSeconds += delta
        cubeTransform.rotation.y = elapsedSeconds
        cubeTransform.rotation.x = elapsedSeconds * 0.5f
        transformSystem.update(world, delta)
        renderSystem.update(world, delta)
    }

    private fun io.github.ronjunevaldoz.awake.scene.runtime.SceneInstance.root(name: String): Transform? {
        val node = roots.firstOrNull { it.name == name } ?: return null
        return world.get(node.entity)
    }

    private companion object {
        const val SCENE_PATH = "scenes/mvp.scene.json"
    }
}
