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

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer
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
    private var paused = false
    private var spaceWasDown = false

    init {
        val scene = SceneLoader.loadFromResource(SCENE_PATH).instantiate(world)
        scene.attachRenderableComponents(resolveRenderable)
        cubeTransform = scene.root("cube")
            ?: error("MVP scene is missing a root node named 'cube'.")
    }

    /** Runs at a fixed [delta] (see [io.github.ronjunevaldoz.awake.core.application
     * .FixedTimestepLoop]), zero or more times per frame -- anything that mutates scene
     * state (today: the demo cube's own animation, plus [TransformSystem]'s world-matrix
     * recompute) belongs here, not in [render], so it stays framerate-independent once
     * something in this scene actually integrates velocity/force (physics, Phase 8). */
    fun fixedUpdate(delta: Float) {
        // Space toggles pause, on the rising edge only (not "while held") -- otherwise a
        // single held keypress would flip `paused` on/off dozens of times per second at a
        // 60Hz fixed step. A tiny, real, hardware-verifiable demonstration that Input
        // actually reaches the simulation, not just an isolated unit test.
        val spaceIsDown = Input.isKeyDown(Key.Space)
        if (spaceIsDown && !spaceWasDown) {
            paused = !paused
        }
        spaceWasDown = spaceIsDown

        if (paused) {
            return
        }
        elapsedSeconds += delta
        cubeTransform.rotation.y = elapsedSeconds
        cubeTransform.rotation.x = elapsedSeconds * 0.5f
        transformSystem.update(world, delta)
    }

    /** Runs exactly once per frame, after zero or more [fixedUpdate] calls. Only draws --
     * [RenderSystem] reads whatever [Transform.worldMatrix] [fixedUpdate] last wrote via
     * [TransformSystem], it never mutates simulation state itself. */
    fun render() {
        renderSystem.update(world, 0f)
    }

    private fun io.github.ronjunevaldoz.awake.scene.runtime.SceneInstance.root(name: String): Transform? {
        val node = roots.firstOrNull { it.name == name } ?: return null
        return world.get(node.entity)
    }

    private companion object {
        const val SCENE_PATH = "scenes/mvp.scene.json"
    }
}
